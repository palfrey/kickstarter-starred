(ns kickstarter-starred.core
  (:gen-class)
  (:use [clojure.walk :only [keywordize-keys]]
		[clj-ical.format :only [write-object media-type]]
		[org.httpkit.server :only [run-server]]
		[ring.middleware.params :only [wrap-params]]
		[ring.middleware.keyword-params :only [wrap-keyword-params]]
		[ring.middleware.stacktrace :only [wrap-stacktrace]]
		[compojure.core :only [defroutes GET POST]]
	)
  (:require [org.httpkit.client :as http]
			[clojure.data.json :as json]
			[clj-time.coerce :as c])
)

(defn get-access-token [email password]
	(-> @(http/post "https://api.kickstarter.com/xauth/access_token?client_id=2II5GGBZLOOZAA5XBU1U0Y44BU57Q58L8KOGM7H0E0YFHP3KTG"
			   {:form-params {"password" password "email" email}})
		:body
		json/read-str
		keywordize-keys
		:access_token)
)

(defn events [access-token]
	(->> @(http/get "https://api.kickstarter.com/v1/users/self/projects/starred"
			   {:query-params {"oauth_token" access-token}})
		:body
		json/read-str
		keywordize-keys
		:projects
		((partial filter #(= (:state %) "live")))
		(map #(select-keys % [:name :deadline :urls]))
		 (map #(let [when (c/from-long (* 1000 (:deadline %)))]
					[:vevent
						[:summary (:name %)]
						[:dtstart when]
						[:dtend when]
						[:description (-> % :urls :web :project)]
					]
			)
		)
		vec
	)
)


(defn generate-calendar [access-token]
	(with-out-str (write-object (cons :vcalendar (events access-token))))
)


(defn main-page [req]
	(let
		[access_token (-> req :params :access_token)]
		{:status  200
		   :headers {"Content-Type" media-type}
		   :body   (generate-calendar access_token)
		}
	)
)

(defroutes all-routes
	(GET "/" [] main-page)
)

(def app (-> #'all-routes wrap-keyword-params wrap-params wrap-stacktrace))

(defn -main [port]
	(run-server app {:port (Integer. port)})
)
