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
    (parse-jsonString "\"\"") => ["" ""])

  (fact "it parses a singleton string"
    (parse-jsonString "\"a\"") => ["a" ""])

  (fact "it parses longer strings"
    (parse-jsonString "\"hi how are you?\"") => ["hi how are you?" ""])

  (fact "it parses escaped quotes"
    (parse-jsonString "\"Speak \\\"friend\\\" and enter\"")
    => ["Speak \"friend\" and enter" ""]))

