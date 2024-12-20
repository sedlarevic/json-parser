(ns json-parser.core 
  (:require
   [clojure.string :refer [blank? join]
    ]))

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
(defn parse-char [stringVal expectedChar]
  (if (blank? stringVal)
    nil
    (do
      (println "Not an empty string!")
      (let [first-char (subs (str stringVal) 0 1)]
        (if (= expectedChar first-char)
          (do
            (let [rest-of-string (subs (str stringVal) 1)]
              [first-char rest-of-string]))
          (do
            nil))))))
;recursively goes through expected-string-value
;when expected-string-value is blank, we return expected value, and the rest of the string-val 
(defn parse-string [string-val expected-string-val]
  (loop [expected-string expected-string-val
         remaining-string string-val
         output []]
    (if (blank? expected-string)
      [(join "" output) remaining-string]
      (let [[parsed-char rest-of-string] (parse-char remaining-string (subs expected-string 0 1))]
        (if(nil? parsed-char)
        nil
        (recur rest-of-string (subs expected-string 1) (conj output parsed-char))
        )
     )
    )))
