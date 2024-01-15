(ns router-core-match-babashka
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]
            [clojure.set :as set]
            [hiccup2.core :refer [html]]
            [org.httpkit.server :as server]))

;; # So this is what the data looks like for the handler.
{:authority/role #{:resource/action}}
{:leam/mwd-field-hand #{:report/edit}}
;; # And this is what it looks like at the resource level
{:action-name/permissions #{:user-space/role}}
{:report/edit #{:leam/mwd-field-hand}}

(defn router [req]
  (let [paths (vec (rest (str/split (:uri req) #"/")))]
    (match [(:request-method req) paths]
           [:get ["users" id]] {:body (str (html [:div id]))}
           :else {:body (str (html [:html "Welcome!"]))})))

(server/run-server router {:port 8090})
@(promise)
