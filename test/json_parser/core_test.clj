(ns json-parser.core-test
  (:require
   [json-parser.core :refer :all]
   [midje.sweet :refer :all]
   [clojure.test :refer :all]
   [json-parser.util :refer :all]))

(facts "about parse-char"
  (fact "it correctly parses a matching char"
    (parse-char "true" \t) => [\t "rue"])
  (fact "it returns nil if the char doesn't match"
    (parse-char "true" \f) => nil)
  (fact "it returns nil for an empty string"
    (parse-char "" \t) => nil))

(facts "about parse-string"
  (fact "it correctly parses a matching string"
    (parse-string "trueasd" "true") => ["true" "asd"])
  (fact "it returns nil if the string doesn't match"
    (parse-string "falseasd" "true") => nil))

(facts "about parse-jsonBool"
  (fact "it parses 'true' as JsonBool with value true"
    (parse-jsonBool "trueasdf") => [(->JsonBool true) "asdf"])
  (fact "it parses 'false' as JsonBool with value false"
    (parse-jsonBool "falseasdf") => [(->JsonBool false) "asdf"])
  (fact "it returns nil for non-boolean strings"
    (parse-jsonBool "null") => nil))

(facts "about parse-jsonNull"
  (fact "it parses 'null' as JsonNull"
    (parse-jsonNull "nullasd") => [(->JsonNull nil) "asd"])
  (fact "it returns nil for non-null strings"
    (parse-jsonNull "true") => nil))

(facts "about parse-jsonString"
  (fact "it parses the empty string"
    (parse-jsonString "\"\"") => [(->JsonString "") ""])
  (fact "it parses a singleton string"
    (parse-jsonString "\"a\"") => [(->JsonString "a") ""])
  (fact "it parses longer strings"
    (parse-jsonString "\"hi how are you?\"") => [(->JsonString "hi how are you?") ""]))

(facts "about parse-jsonNumber"
  (fact "it parses an integer"
    (parse-jsonNumber "123asdf") => [(->JsonNumber 123) "asdf"])
  (fact "it parses a negative number"
    (parse-jsonNumber "-456rest") => [(->JsonNumber -456) "rest"])
  (fact "it parses a floating-point number"
    (parse-jsonNumber "78.90extra") => [(->JsonNumber 78.90) "extra"])
  (fact "it returns nil for invalid numbers"
    (parse-jsonNumber "abc") => nil))

(facts "about parse-jsonArray"
  (fact "it parses an empty array"
    (parse-jsonArray "[]") => [(->JsonArray []) ""])
  (fact "it parses an array with mixed values"
    (parse-jsonArray "[true, false, null, 123, \"hello\"]")
    => [(->JsonArray [[(->JsonBool true)
                       (->JsonBool false)
                       (->JsonNull nil)
                       (->JsonNumber 123)
                       (->JsonString "hello")]]) ""]))

(facts "about parse-jsonObject"
  (fact "it parses an empty object"
    (parse-jsonObject "{}") => [(->JsonObject []) ""])
  (fact "it parses an object with key-value pairs"
    (parse-jsonObject "{\"key\": true, \"another\": 123}")
    => [(->JsonObject [["key" (->JsonBool true)]
                       ["another" (->JsonNumber 123)]]) ""])
  (fact "it returns nil for invalid JSON objects"
    (parse-jsonObject "{\"key\":}") => nil))

(facts "about JSON parsing"
  (fact "Custom parser should correctly parse small JSON"
    (let [json-string (slurp "jsontext.txt")]
      (my-parser/parse-json json-string) => (cheshire/parse-string json-string true)))
  (fact "Custom parser should correctly parse large JSON"
    (let [json-string (slurp "largejsontext.txt")]
      (my-parser/parse-json json-string) => (cheshire/parse-string json-string true))))

