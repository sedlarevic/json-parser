(ns json-parser.core
  (:require
   [clojure.string :refer [blank? join trim]]
   [clojure.string :as string]))
;NOTE: first rest solve a lot of problems, when project finished, try to implement them. Mostly in parse-char 
;NOTE/TODO: no string escaping support
;records
(defrecord JsonString [value])
(defrecord JsonNumber [value]) 
(defrecord JsonBool [value])
(defrecord JsonNull [value])
(defrecord JsonArray [value]) ; should be an array 
(defrecord JsonObject [value]) ; should be a map

;instances
(def json-bool-true (->JsonBool true))
(def json-bool-false (->JsonBool false))
(def json-null (->JsonNull nil))

(defn create-json-number [num]
  (->JsonNumber num))
(defn create-json-string [str]
  (->JsonString str))
;takes first letter of the string-value, and checks if the letter is the expected char
;if correct, returns the char and the rest of string, if not returns nil
(defn parse-char [string-val expected-char]
  (if (blank? string-val)
    (do (println (blank? string-val) expected-char) nil)
    (do
      (println "parsing char..." string-val expected-char)
      (let [first-char (first string-val)]
        (if (= (str expected-char) (str first-char))
          (do
            (let [rest-of-string (subs (str string-val) 1)]
              [first-char rest-of-string]))
          (do
            (println "chars are not same" first-char expected-char)
            nil))))))
;recursively goes through expected-string-value
;when expected-string-value is blank, we return expected value, and the rest of the string-val 
(defn parse-string [string-val expected-string-val]
  (loop [expected-string expected-string-val
         remaining-string string-val
         output []]
    (if (blank? expected-string)
      [(join "" output) remaining-string]
      (let [[parsed-char rest-of-string] (parse-char remaining-string (first expected-string))]
        (if (nil? parsed-char)
          nil
          (recur (subs expected-string 1) rest-of-string (conj output parsed-char)))))))
(defn parse-string-until [string-val until]
  (loop [remaining-string string-val
         output []]
    (if (blank? remaining-string)
      nil
      (let [[parsed-char rest-of-string] (parse-char remaining-string (first remaining-string))]
        (if (= (str until) (str parsed-char))
          [(apply str output) rest-of-string]
          (recur rest-of-string (conj output parsed-char)))))))

(defn parse-jsonNull [string-val]
  (let [[output remaining-string] (parse-string string-val "null")]
    (if (nil? output)
      nil
      [json-null remaining-string])))

(defn parse-jsonBool [string-val]
  (let [[output-for-true remaining-string-for-true] (parse-string string-val "true")]
    (if (nil? output-for-true)
      (do (let [[output-for-false remaining-string-for-false] (parse-string string-val "false")]
            (if (nil? output-for-false)
              nil
              [json-bool-false remaining-string-for-false])))
      [json-bool-true remaining-string-for-true])))

(defn parse-jsonString [string-val]
  (let [[_ remaining-string] (parse-char string-val "\"")]
    (if (nil? remaining-string)
      nil
      (do
        (let [[output-string remaining] (parse-string-until remaining-string "\"")]
          (if (nil? output-string)
            nil
            [(create-json-string output-string) remaining]))))))

(defn span-string [string-val]
  (let [[_ number rest-of-string] (re-matches #"^(-?(?:0|[1-9]\d*)(?:\.\d+)?)(.*)$" string-val)]
    (println _ number rest-of-string string-val)
    (if (nil? number)
      nil
      [number rest-of-string])))

(defn parse-jsonNumber [string-val]
  (let [[output remaining-string] (span-string (trim string-val))]
    (println output remaining-string)
    (if (nil? output)
      nil
      [(create-json-number (read-string output)) remaining-string])))


