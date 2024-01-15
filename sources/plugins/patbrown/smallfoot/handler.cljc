(ns patbrown.smallfoot.handler
  (:require
   [patbrown.ix :refer [execute]]
   [xiana.route :as route]
   [xiana.state :as state]))

(defrecord State
  [request request-data response session-data deps])

(defn ->state
  "Create an empty state structure."
  [ctx req]
  (->
    {:ctx ctx
     :request  req
     :response {}}
    map->State (conj {})))

(defn handler-fn
  [ctx]
  (fn handle*
    ([req]
     (let [state (->state ctx req)
           queue (list #(execute % (:router-interceptors ctx))
                       #(route/match %)
                       #(execute % (:controller-interceptors ctx)))
           result (reduce (fn [s f] (f s)) state queue)]
       (:response result)))
    ([request respond _]
     (respond (handle* request)))))
