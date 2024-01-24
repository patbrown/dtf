(ns tiny.handler
  (:require
   [tiny.ix :refer [execute]]
   [tiny.route :as route]))

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
           queue (list #(execute % (-> ctx :chains :router))
                       #(execute % [route/focus-on-route-interceptor])
                       #_#(route/match-route-interceptor %)
                       #(execute % (-> % :focus :route :chain)))
           results (reduce (fn [s f] (f s)) state queue)
           {:keys [response focus]} results
           _ (tap> results)]
       (if-not (nil? response)
         response
         ((:action focus) results))))
    ([request respond _]
     (respond (handle* request)))))
