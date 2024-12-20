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
    nil
    (do
      (println "Not an empty string!")
      (let [first-char (first string-val)]
        (if (= expected-char first-char)
          (do
            (let [rest-of-string (subs (str string-val) 1)]
              [first-char rest-of-string]))
          (do
            nil))))))
;NOTE: DOES NOT WORK FOR: "falseasdasd" "false", but works for "trueasdasd" "true"
;TODO: Find a fix for a bug
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
          (recur rest-of-string (subs expected-string 1) (conj output parsed-char)))))))
