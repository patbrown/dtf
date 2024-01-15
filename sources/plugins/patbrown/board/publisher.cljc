(ns patbrown.board.publisher
  #?(:bb (:require [clojure.core.async :as a])
     :clj (:require [com.brunobonacci.mulog.publisher :as p]
                    [com.brunobonacci.mulog.buffer :as rb]
                    [com.brunobonacci.mulog :as mu]
                    [clojure.core.async :as a])))

#?(:bb nil
   :clj (deftype EventBusPublisher [config buffer transform]
          com.brunobonacci.mulog.publisher.PPublisher
          (agent-buffer [_]
            buffer)
          (publish-delay [_]
            500)
          (publish [_ buffer]
            (let [eb (:eb config)]
              (doseq [evt (transform (map second (rb/items buffer)))]
                (a/>!! eb evt)))
            (rb/clear buffer))))

#?(:bb nil
   :clj (defn event-bus-publisher
          [{:keys [transform] :as config}]
          (EventBusPublisher. config (rb/agent-buffer 10000) (or transform identity))))

#?(:bb nil
   :clj (defn default-transform
          "This is just a stub that is essentially identity.
  Don't change it, write a new one."
          [events]
          (map #(identity %) events)))

#?(:bb nil
   :clj (deftype TapPublisher [buffer transform]
          com.brunobonacci.mulog.publisher.PPublisher
          (agent-buffer [_]
            buffer)

          (publish-delay [_]
            200)

          (publish [_ buffer]
            (doseq [item (transform (map second (rb/items buffer)))]
              (tap> item))
            (rb/clear buffer))))

#?(:bb nil
   :clj (defn tap-publisher
          [{:keys [transform] :as _config}]
          (TapPublisher. (rb/agent-buffer 10000) (or transform identity))))
#?(:bb nil
   :clj (def console {:type :console :pretty? true}))
#?(:bb nil
   :clj (def tap {:type :custom :fqn-function (str *ns* "/tap-publisher")}))

#?(:bb nil
   :clj (def additional-publishers [console tap]))
#?(:bb nil
   :clj (defn start-publishers!
          "Starts publishers collection of ids"
          [eb]
          (mu/start-publisher!
           {:type :multi
            :publishers (into additional-publishers
                              {:type :custom
                               :fqn-function
                               "tools.mulog/event-bus-publisher"
                               :transform #(default-transform %)
                               :eb eb})})))

#?(:bb nil
   :clj (defn killable-publisher [m] {:kill-publisher! (mu/start-publisher!
                                                        (merge {:type :custom
                                                                :fqn-function (str *ns* "/event-bus-publisher")
                                                                :transform #(default-transform %)
                                                                :pretty-print true}
                                                               m))}))

#?(:bb nil
   :clj (defn m->pairs
          [m]
          {:pairs (vec (flatten (into '[] m)))}))

;; This is will not be called in event
#?(:bb nil
   :clj (defn log
          ([{:event/keys [ident] :as m}]
           (log (or ident :event/unknown) m)
           ident)
          ([eid m]
           (mu/log eid (assoc (m->pairs m) :event/ident eid)))))

;; Captures what I need to capture by returning a function that grabs it from a return map.
#?(:bb nil
   :clj (defn wrap-capture
          [kset]
          (fn [data]
            (select-keys data kset))))

;; This will do. 3 arrity, not in love with the overload, but I think it's best for integrant runtime.
#?(:bb nil
   :clj (defn trace
          ([{:event/keys [ident] :as m}]
           (mu/trace ident (or (m->pairs m) {})))
          ([m body]
           (trace m {} body))
          ([{:event/keys [ident captures] :as m} added-trace-conf body]
           (if captures
             (mu/trace ident (assoc (m->pairs m) :capture (wrap-capture captures))
                       body)
             (mu/trace ident (m->pairs (merge m added-trace-conf)) body)))))
