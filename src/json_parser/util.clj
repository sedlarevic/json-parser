(ns json-parser.util
  (:require
   [clojure.string :refer [blank? join trim]]))

;records
(defrecord JsonString [value]
  Object
  (toString [this]
    (str "JsonString: \"" value "\"")))

(defmethod print-method JsonString [v ^java.io.Writer w]
  (.write w (str v)))

(defrecord JsonNumber [value]
  Object
  (toString [this]
    (str "JsonNumber: " value)))

(defmethod print-method JsonNumber [v ^java.io.Writer w]
  (.write w (str v)))

(defrecord JsonBool [value]
  Object
  (toString [this]
    (str "JsonBool: " value)))

(defmethod print-method JsonBool [v ^java.io.Writer w]
  (.write w (str v)))

(defrecord JsonNull [value]
  Object
  (toString [this]
    (str "JsonNull: nil")))

(defmethod print-method JsonNull [v ^java.io.Writer w]
  (.write w (str v)))

(defrecord JsonArray [value]
  Object
  (toString [this]
    (str "JsonArray: " (clojure.string/join ", " value) "")))

(defmethod print-method JsonArray [v ^java.io.Writer w]
  (.write w (str v)))

(defrecord JsonObject [value]
  Object
  (toString [this]
    (str "JsonObject: " (clojure.string/join ", " value) "")))

(defmethod print-method JsonObject [v ^java.io.Writer w]
  (.write w (str v)))
; should be a map

;instances
(def json-bool-true (->JsonBool true))
(def json-bool-false (->JsonBool false))
(def json-null (->JsonNull nil))
(defn create-json-number [num]
  (->JsonNumber num))
(defn create-json-string [str]
  (->JsonString str))
(defn create-json-array [array]
  (->JsonArray array))
(defn create-json-object [object]
  (->JsonObject object))
(def escape-sequences (hash-map
                       :quote "\""
                       :backslash "\\"))
;takes first letter of the string-value, and checks if the letter is the expected char
;if correct, returns the char and the rest of string, if not returns nil
(defn parse-char [string-val expected-char]
  (if (blank? string-val)
    (do (println (blank? string-val) expected-char) nil)
    (do
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
    (if (or (nil? remaining-string) (blank? remaining-string))
      nil
      (let [[parsed-char rest-of-string] (parse-char remaining-string (first remaining-string))]
        ; if escape seq should be handled here, better to do with cond, 1. parsed-char = until 2. hit \ 3. :else
               (if (= (str until) (str parsed-char))
          [(apply str output) rest-of-string]
          (recur rest-of-string (conj output parsed-char)))))))

(defn span-string [string-val]
  (let [[_ number rest-of-string] (re-matches #"^(-?(?:0|[1-9]\d*)(?:\.\d+)?)(.*)$" (trim string-val))]
    (println "in span string, number..." number "rest of string" rest-of-string)
    (if (nil? number)
      nil
      [number (trim rest-of-string)])))

(defn remove-separator [string-val]
  (let [[_,rest-of-string] (parse-char (trim string-val) ",")]
    (if (nil? rest-of-string)
      (trim string-val)
      (trim rest-of-string))))

