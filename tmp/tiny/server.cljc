(ns tiny.server
  (:require [org.httpkit.server :as server]
            [tiny.handler :refer [handler-fn]]))

(defn load
  [{:keys [server] :as ctx}]
  (let [options server
        server (server/run-server (handler-fn ctx) options)]
    {:options options :kill! server}))

(defn kill! [ctx]
  (when-let [webserver (get-in ctx [:server :kill!])] (webserver)))

(defn start!
  "Start web server."
  [ctx]
  (when-let [server (load ctx)]
    (assoc ctx :server server)))
