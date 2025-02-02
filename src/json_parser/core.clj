(ns json-parser.core
  (:require
   [cheshire.core :as json]
   [clojure.string :refer [blank? trim]]
   [criterium.core :as crit]
   [json-parser.util :as util]))

;NOTE/TODO: no string escaping support
(declare json-value)
(declare json-value-element)
(declare convert-to-correct-format)

(defn read-json-with-cheshire [file]
  (json/parse-string (slurp file) true))
;###
(defn parse-jsonNull [string-val]
  (let [[output remaining-string] (util/parse-string string-val "null")]
    (if (nil? output)
      nil
      [util/json-null remaining-string])))
;###
(defn parse-jsonBool [string-val]
  (let [[output-for-true remaining-string-for-true] (util/parse-string string-val "true")]
    (if (nil? output-for-true)
      (do (let [[output-for-false remaining-string-for-false] (util/parse-string string-val "false")]
            (if (nil? output-for-false)
              nil
              [util/json-bool-false remaining-string-for-false])))
      [util/json-bool-true remaining-string-for-true])))
;###
(defn parse-jsonString [string-val]
  (let [[_ remaining-string] (util/parse-char string-val "\"")]
    (if (nil? remaining-string)
      nil
      (do
        (let [[output-string remaining] (util/parse-string-until remaining-string "\"")]
          (if (nil? output-string)
            nil
            [(util/create-json-string output-string) remaining]))))))

;###
(defn parse-jsonNumber [string-val]
  (let [[output remaining-string] (util/span-string (trim string-val))]
    (if (nil? output)
      nil
      (do
        [(util/create-json-number (read-string output)) (trim remaining-string)]))))

(defn separate-pair [string-val]
  (if (= (first (trim string-val)) \")
    (do
      (let [[_ string-without-first-quote] (util/parse-char (trim string-val) \")]
        (let [[key rest-of-string] (util/parse-string-until string-without-first-quote \")]
          (if (and key (= (first (trim rest-of-string)) \:))
            (do
              (let [[_ parsable-string] (util/parse-char (trim rest-of-string) \:)]
                (let [[value rest-of-string-after-parsing] (json-value-element (trim parsable-string))]
                  (if value
                    (do
                      (let [rest-of-string-after-removing-separator
                            (util/remove-separator (trim rest-of-string-after-parsing))]
                        [[key value] rest-of-string-after-removing-separator])) nil)))) nil)))) nil))
;###
(defn parse-jsonObject [string-val]
  (when-not (nil? string-val)
    (let [[_ remaining-string] (util/parse-char string-val \{)]
      (if (nil? remaining-string)
        nil
        (do
          (loop [string-to-parse (trim remaining-string)
                 parsed-elements []]
            (cond
            ;; valid JSON object end
              (and (not (blank? string-to-parse)) (= (first string-to-parse) \}))
              [(util/create-json-object parsed-elements) (subs string-to-parse 1)]

            ;; Invalid or blank string
              (or (blank? string-to-parse) (nil? string-to-parse))
              (do
                nil)

            ;; parse key-value pairs
              :else
              (let [[key-value-output string-to-parse-after-pair]
                    (separate-pair string-to-parse)]
                (if (nil? key-value-output)
                  (do
                    nil)
                  (recur string-to-parse-after-pair
                         (conj parsed-elements key-value-output)))))))))))
;###
(defn parse-jsonArray [string-val]
  (let [[_ remaining-string] (util/parse-char string-val "[")]
    (if (nil? remaining-string)
      nil
      (loop [string-to-parse (trim remaining-string)
             parsed-elements nil]
        (cond
          ; valid JSON array end
          (and (not (blank? string-to-parse)) (= (first string-to-parse) \]))
          [(util/create-json-array parsed-elements) (subs string-to-parse 1)]

          ;if string blank or false error
          (or (blank? string-to-parse) (nil? string-to-parse))
          nil

          :else
          (let [[parsed-value rest-of-string] (json-value string-to-parse)
                remaining-string (util/remove-separator (or rest-of-string string-to-parse))]
            ;if parsing failed error
            (if (or (nil? parsed-value) (= string-to-parse remaining-string))
              nil
              (recur remaining-string (conj parsed-elements parsed-value)))))))))

(def parser-list (hash-map
                  :array parse-jsonArray,
                  :object parse-jsonObject,
                  :string parse-jsonString,
                  :number parse-jsonNumber,
                  :boolean parse-jsonBool,
                  :null parse-jsonNull))

(defn try-parser [string-val]
  (loop [parsers (vals parser-list)]
    (if (empty? parsers)
      nil
      (let [parser (first parsers)
            [output,rest-of-string] (parser string-val)]
        (if (some? output)
          (do
            [output rest-of-string])
          (do
            (recur (rest parsers))))))))

(defn json-value-element [string-val]
  (let [[parsed-value rest-of-string] (try-parser (trim string-val))]
    (if (= (first rest-of-string) "]")
      [parsed-value (rest rest-of-string)]
      [parsed-value rest-of-string])))

(defn json-value [string-val]
  (loop [string-to-parse (trim string-val)
         parsed-output []]
    (if (or (nil? string-to-parse) (blank? string-to-parse))
      parsed-output)
    (let [[parsed-value rest-of-string] (try-parser string-to-parse)
          remaining-string (util/remove-separator (or rest-of-string string-to-parse))]
      (cond
        (or (nil? parsed-value) (= (trim string-to-parse) (trim remaining-string)) (= (first remaining-string) "]"))
        (if (= (first remaining-string) "]")
          [parsed-output (rest remaining-string)]
          [parsed-output remaining-string])
        :else
        (recur (trim remaining-string) (conj parsed-output parsed-value))))))
(defn clean-string [s]
  (-> s
      (clojure.string/replace #"\s+" " ") ;; Zamenjuje višestruke razmake jednim
      (clojure.string/trim)))             ;; Uklanja vodeće i prateće razmake
(defn read-json-from-file
  [file]
  (let [string-from-file (clean-string (slurp file))]
    (if (nil? string-from-file)
      nil
      (do
        (let [[output-json,_] (json-value (trim string-from-file))]
          (let [final (convert-to-correct-format output-json)]
            final))))))
(defn convert-to-correct-format [json-ast]
  (cond

    (and (vector? json-ast) (= 1 (count json-ast)))
    (convert-to-correct-format (first json-ast))

    (vector? json-ast)
    (mapv convert-to-correct-format json-ast)

    (= :json-object (:type json-ast))
    (into {} (map (fn [[k v]] [(keyword k) (convert-to-correct-format v)]) (:value json-ast)))

    (= :json-array (:type json-ast))
    (mapv convert-to-correct-format (first (:value json-ast)))

    (= :json-string (:type json-ast))
    (:value json-ast)

    (= :json-number (:type json-ast))
    (:value json-ast)

    (= :json-bool (:type json-ast))
    (if (= (:value json-ast) true) true false)

    (= :json-null (:type json-ast))
    nil

    :else
     nil)) 


