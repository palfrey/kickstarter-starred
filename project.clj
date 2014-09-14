(defproject kickstarter-starred "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
				 [org.clojure/clojure "1.6.0"]
				 [org.clojure/data.json "0.2.3"]
				 [http-kit "2.1.16"]
				 [ring "1.2.1"]
				 [ithayer/clj-ical "1.2"]
				 [compojure "1.1.6"]]
  :profiles {:uberjar {:aot :all}}
	:plugins [[lein-ring "0.8.7"]]
	:ring {:handler kickstarter-starred.core/app :port 8080}
	:min-lein-version "2.0.0"
)
