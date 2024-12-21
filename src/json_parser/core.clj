(ns json-parser.core
  (:require
   [clojure.string :refer [blank? join]]))

(defrecord JsonString [value])
(defrecord JsonNumber [value]) ; NOTE/TODO: no support for float values 
(defrecord JsonBool [value])
(defrecord JsonNull [])
(defrecord JsonArray [value]) ; should be an array 
(defrecord JsonObject [value]) ; should be a map

(defrecord Parser [run-parser])
(defn make-parser [parser-fn]
  (->Parser parser-fn))
;takes first letter of the string-value, and checks if the letter is the expected char
;if correct, returns the char and the rest of string, if not returns nil
(defn parse-char [string-val expected-char]
  (if (blank? string-val)
    (do (println (blank? string-val) expected-char) nil)
    (do
      (println "radi..." string-val expected-char)
      (let [first-char (first string-val)]
        (if (= expected-char first-char)
          (do
            (let [rest-of-string (subs (str string-val) 1)]
              [first-char rest-of-string]))
          (do
            (println "ne gadjaju se char-ovi")
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
