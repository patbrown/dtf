(ns patbrown.smallfoot.server
  (:require [org.httpkit.server :as server]
            [patbrown.smallfoot.handler :refer [handler-fn]]))

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

(def routes [{:path "/"
              :method :get
              :action (fn [req] {:status 200
                                 :body "Hi there!"})}
             {:path "/hello/:who"
              :method :get
              :action (fn [req]
                        {:status 200
                         :body (str "Hello, " (:who (:params req)))})}
             {:path "/chain/:who"
              :method :get
              :chain [{:name :dude
                       :enter (fn [state]
                                (assoc state :response {:status 200
                                                      :body (str "Hello, " (-> state
                                                                               :focus
                                                                               :params
                                                                               :who))}))}]}])

(comment )

(def ctx {:server {:port  3000
                   :host "0.0.0.0"
                   :join? false}
          :routes routes
          #_#_:chains {:router []}})

(comment (def d (start! ctx))
         d
         ((-> d :server :kill!))
         (tap> d)
;
         )
