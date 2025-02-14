(defproject json-parser "0.1.0-SNAPSHOT"
  :description "simple json parser written in clojure"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [midje "1.10.9"]
                 [criterium "0.4.6"]
                 [cheshire "5.11.0"]]
  :plugins [[lein-midje "3.2.1"]]
  :main json-parser.core
  :repl-options {:init-ns json-parser.core})



