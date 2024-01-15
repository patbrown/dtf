(ns patbrown.smallfoot.server
  (:require [org.httpkit.server :as server]
            [patbrown.smallfoot.handler :refer [handler-fn]]))



(defn init
  "Web server instance."
  [{:keys [server] ctx}]
  (let [options server
        server (server/run-server (handler-fn ctx) options)]
    {:options options
     :kill-server! server}))

(defn start
  "Start web server."
  [ctx]
  (when-let [webserver (get-in ctx [:server :kill-server!])] (webserver))
  (when-let [server (init ctx)]
    (assoc ctx :server server)))
