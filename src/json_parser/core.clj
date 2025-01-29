(ns json-parser.core
  (:require
   [clojure.string :refer [blank? trim]]
   [clojure.string :as string]
   [json-parser.util :refer :all]))
   ;[cheshire.core :as json]
   ;[criterium.core :as crit]

;NOTE: first rest solve a lot of problems, when project finished, try to implement them. Mostly in parse-char 
;NOTE/TODO: no string escaping support
(declare json-value)
(declare json-value-element)

;(defn read-json-with-cheshire [file]
;  (json/parse-string (slurp file) true))
;###
(defn parse-jsonNull [string-val]
  (let [[output remaining-string] (parse-string string-val "null")]
    (if (nil? output)
      nil
      [json-null remaining-string])))
;###
(defn parse-jsonBool [string-val]
  (let [[output-for-true remaining-string-for-true] (parse-string string-val "true")]
    (if (nil? output-for-true)
      (do (let [[output-for-false remaining-string-for-false] (parse-string string-val "false")]
            (if (nil? output-for-false)
              nil
              [json-bool-false remaining-string-for-false])))
      [json-bool-true remaining-string-for-true])))
;###
(defn parse-jsonString [string-val]
  (let [[_ remaining-string] (parse-char string-val "\"")]
    (if (nil? remaining-string)
      nil
      (do
        (let [[output-string remaining] (parse-string-until remaining-string "\"")]
          (if (nil? output-string)
            nil
            [(create-json-string output-string) remaining]))))))

;###
(defn parse-jsonNumber [string-val]
  (let [[output remaining-string] (span-string (trim string-val))]
    (if (nil? output)
      nil
      (do
        [(create-json-number (read-string output)) (trim remaining-string)]))))

(defn separate-pair [string-val]
  (if (= (first (trim string-val)) \")
    (do
      (let [[_ string-without-first-quote] (parse-char (trim string-val) \")]
        (let [[key rest-of-string] (parse-string-until string-without-first-quote \")]
          (if (and key (= (first (trim rest-of-string)) \:))
            (do
              (let [[_ parsable-string] (parse-char (trim rest-of-string) \:)]
                (let [[value rest-of-string-after-parsing] (json-value-element (trim parsable-string))]
                  (if value
                    (do
                      (let [rest-of-string-after-removing-separator
                            (remove-separator (trim rest-of-string-after-parsing))]
                        [[key value] rest-of-string-after-removing-separator])) nil)))) nil)))) nil))
;###
(defn parse-jsonObject [string-val]
  (when-not (nil? string-val)
    (let [[_ remaining-string] (parse-char string-val \{)]
      (if (nil? remaining-string)
        nil
        (do
          (loop [string-to-parse (trim remaining-string)
                 parsed-elements []]
            (cond
            ;; valid JSON object end
              (and (not (blank? string-to-parse)) (= (first string-to-parse) \}))
              [(create-json-object parsed-elements) (subs string-to-parse 1)]

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
  (let [[_ remaining-string] (parse-char string-val "[")]
    (if (nil? remaining-string)
      nil
      (loop [string-to-parse (trim remaining-string)
             parsed-elements []]
        (cond
          ; valid JSON array end
          (and (not (blank? string-to-parse)) (= (first string-to-parse) \]))
          [(create-json-array (vec parsed-elements)) (subs string-to-parse 1)]

          ;if string blank or false error
          (or (blank? string-to-parse) (nil? string-to-parse))
          nil

          :else
          (let [[parsed-value rest-of-string] (json-value string-to-parse)
                remaining-string (remove-separator (or rest-of-string string-to-parse))]
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
          remaining-string (remove-separator (or rest-of-string string-to-parse))]
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
          output-json)))))

;(defn benchmark []
;  (let [file "largejsontext.txt"]
;    (println "Benchmarking custom parser:")
;    (crit/quick-bench (read-json-from-file file))
;    (println "Benchmarking Cheshire parser:")
;    (crit/quick-bench (read-json-with-cheshire file))))
