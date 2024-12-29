(ns json-parser.core
  (:require
   [clojure.string :refer [blank? trim]]
   [clojure.string :as string]
   [json-parser.util :refer :all]))
;NOTE: first rest solve a lot of problems, when project finished, try to implement them. Mostly in parse-char 
;NOTE/TODO: no string escaping support
(declare json-value)
(declare json-value-element)
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
    (println output remaining-string)
    (if (nil? output)
      nil
      [(create-json-number (read-string output)) remaining-string])))

(defn separate-pair [string-val]
  (println "in separate-pair! " string-val)
  (if (= (first (trim string-val)) \")
    (do
      (let [[_ string-without-first-quote] (parse-char (trim string-val) \")]
        (println "After removing first quote: " string-without-first-quote)
        (let [[key rest-of-string] (parse-string-until string-without-first-quote \")]
          (println "Parsed key: " key " Remaining string: " rest-of-string)
          (if (and key (= (first (trim rest-of-string)) \:))
            (do
              (let [[_ parsable-string] (parse-char (trim rest-of-string) \:)]
                (let [[value rest-of-string-after-parsing] (json-value-element (trim parsable-string))]
                  (println "Parsed value: " value " Remaining string: " rest-of-string-after-parsing)
                  (if value
                    (do
                      (let [rest-of-string-after-removing-separator
                            (remove-separator (trim rest-of-string-after-parsing))]
                        (println "After removing separator: " rest-of-string-after-removing-separator)
                        [[key value] rest-of-string-after-removing-separator])) nil))))
                     (do
                       (println "Key or ':' is missing.") nil))))) 
                      (do (println "String does not start with a quote.") nil)))

;###
(defn parse-jsonObject [string-val]
  (when-not (nil? string-val)
    (let [[_ remaining-string] (parse-char string-val \{)]
      (println "After removing opening brace: " remaining-string)
      (if (nil? remaining-string)
        nil
        (loop [string-to-parse (trim remaining-string)
               parsed-elements []]
          (println "Current string: " string-to-parse)
          (println "Parsed elements so far: " parsed-elements)
          (cond
            ;; valid JSON object end
            (and (not (blank? string-to-parse)) (= (first string-to-parse) \}))
            [(create-json-object parsed-elements) (subs string-to-parse 1)]

            ;; Invalid or blank string
            (or (blank? string-to-parse) (nil? string-to-parse))
            (do
              (println "Invalid or blank string.")
              nil)

            ;; parse key-value pairs
            :else
            (let [[key-value-output string-to-parse-after-pair]
                  (separate-pair string-to-parse)]
              (println "Key-value pair output: " key-value-output)
              (println "Remaining string after pair: " string-to-parse-after-pair)
              (if (nil? key-value-output)
                (do
                  (println "Key-value pair parsing failed.")
                  nil)
                (recur string-to-parse-after-pair
                       (conj parsed-elements key-value-output))))))))))
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
          [(create-json-array parsed-elements) (subs string-to-parse 1)]

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
          [output rest-of-string]
          (recur (rest parsers)))))))

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


