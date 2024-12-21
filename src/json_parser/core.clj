(ns json-parser.core
  (:require
   [clojure.string :refer [blank? join]]))

;records
(defrecord JsonString [value])
(defrecord JsonNumber [value]) ; NOTE/TODO: no support for float values 
(defrecord JsonBool [value])
(defrecord JsonNull [value])
(defrecord JsonArray [value]) ; should be an array 
(defrecord JsonObject [value]) ; should be a map

;instances
(def json-bool-true (->JsonBool true))
(def json-bool-false (->JsonBool false))
(def json-null (->JsonNull nil))
;takes first letter of the string-value, and checks if the letter is the expected char
;if correct, returns the char and the rest of string, if not returns nil
(defn parse-char [string-val expected-char]
  (if (blank? string-val)
    (do (println (blank? string-val) expected-char) nil)
    (do
      (println "parsing char..." string-val expected-char)
      (let [first-char (first string-val)]
        (if (= expected-char first-char)
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


