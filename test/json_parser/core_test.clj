(ns json-parser.core-test
  (:require
   [json-parser.core :refer :all]
   [midje.sweet :refer [fact facts]]))


;; Test 1: Simple Key-Value Pair
(fact "Parsing a simple key-value JSON string"
  (parse-json "{\"key\": \"value\"}")
  => {:key "value"})

;; Test 2: Nested Object
(fact "Parsing a nested JSON object"
  (parse-json "{\"person\": {\"name\": \"Jane\", \"details\": {\"age\": 25, \"city\": \"New York\"}}}")
  => {:person {:name "Jane" :details {:age 25 :city "New York"}}})


;; Test 3: Mixed Data Types
(fact "Parsing JSON with mixed data types"
  (parse-json "{\"id\": 123, 
                \"active\": true, 
                \"tags\": [\"clojure\", null, \"json\"],
                \"metadata\": {\"version\": \"1.0\",
                \"deprecated\": false}}")
  => {:id 123
      :active true
      :tags ["clojure" nil "json"]
      :metadata {:version "1.0" :deprecated false}})

;; Test 4: Invalid Keys
(facts "Testing invalid JSON keys"
  (fact "Unquoted key"
    (parse-json "{key: \"value\"}") => (throws Exception))
  (fact "Special characters in key"
    (parse-json "{\"@key\": \"value\"}") => (throws Exception)))

;; Test 5: Edge Cases
(fact "Parsing edge cases in JSON"
  (parse-json "{\"description\": \"Special characters: \\\"quotes\\\", \\\\backslashes\\\\.\", 
                \"emptyArray\": [],
                \"emptyObject\": {},
                \"deepNest\": {\"level1\": {\"level2\": {\"level3\": \"value\"}}}}")
  => {:description "Special characters: \"quotes\", \\backslashes\\."
      :emptyArray []
      :emptyObject {}
      :deepNest {:level1 {:level2 {:level3 "value"}}}})

;; Test 6: Invalid Parameters and Keys
(facts "Handling invalid JSON parameters"
  (fact "Missing closing brace"
    (parse-json "{\"key\": \"value\"") => (throws Exception))
  (fact "Extra comma in JSON array"
    (parse-json "{\"key\": [1, 2, 3,]}") => (throws Exception)))
