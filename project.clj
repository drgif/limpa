(defproject limpa "0.1.0-SNAPSHOT"
  :description "A Clojure library for implementing Clean Architecture"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :repl-options {:init-ns limpa.core}
  :profiles
  {:dev {:source-paths ["env/dev"]
         :repl-options {:init-ns user
                        :timeout 120000}}})
