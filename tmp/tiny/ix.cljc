;; Straight outta xiana
(ns tiny.ix
  (:require [tiny.focus :refer [filter-where fv]]))

(defn- -concat
  [{except-interceptors :except
    around-interceptors :around
    inside-interceptors :inside
    :as                 interceptors}
   default-interceptors]
  (if (map? interceptors)
    (remove (set except-interceptors)
            (concat around-interceptors
                    default-interceptors
                    inside-interceptors))
    (or interceptors default-interceptors)))

(defn action->try
  [action]
  (fn [state]
    (try (action state)
         (catch #?(:clj Exception :cljs js/Exception) e
           (assoc state :error e)))))

(defn looper
  [state interceptors action]
  (loop [state        state
         interceptors interceptors
         backwards    '()
         action       action
         direction    :enter]
    (let [direction-error?    (= :error direction)
          direction-enter?    (= :enter direction)
          exception           (:error state)]
      (cond
        (and exception (not direction-error?))
        (recur state
               (if direction-enter? backwards interceptors)
               '()
               action
               :error)
        (seq interceptors)
        (let [direction (if (and direction-error? (not exception))
                          :leave ; error was "handled", executing remaining interceptors (:leave direction)
                          direction)
              act       (-> interceptors
                            first
                            (get direction identity)
                            action->try)
              state     (act state)
              next-interceptors (if (and (:error state) (= :leave direction))
                                  interceptors
                                  (rest interceptors))]
          (recur state
                 next-interceptors
                 (when direction-enter? (conj backwards (first interceptors)))
                 action
                 direction))
        direction-enter?
        (recur ((action->try action) state)
               backwards
               '()
               identity
               :leave)
        :else state))))

(defn execute
  ([state chain] nil)
  ([state chains chain]
   (let [interceptors (-concat
                       (get-in state [:request-data :interceptors])
                       (-> (filter-where [:chain/id = chain] chains)
                           first :chain/links))
         action       (action->try (get-in state [:request-data :action] identity))]
     (looper state interceptors action))))

(defn ezex
  [ctx]
  (let [chain (-> ctx :action :ezex :chain)
        chain (get-in ctx (fv [chain :chain]))]
    (execute ctx chain)))

