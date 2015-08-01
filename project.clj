(defproject turing "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.9.6"]]
  :ring {:port 8080
         :init turing.service/init
         :handler turing.service/handler
         :nrepl {:start? true
                 :port 1234}
         :destroy turing.service/destroy}
  :dependencies [[org.clojure/clojure "1.8.0-alpha3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.codec "0.1.0"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [org.xerial/sqlite-jdbc "3.8.11"]
                 [org.clojure/java.jdbc "0.4.1"]])
