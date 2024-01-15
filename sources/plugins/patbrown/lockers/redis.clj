(ns patbrown.lockers.redis
  (:refer-clojure :exclude [get set])
  (:require [taoensso.carmine :as car]
            [patbrown.secrets :as ***]
            [clojure.core.async :as a]
            [patbrown.supastore :refer [commit cleanup commit! supastore create-supastore
                                                    kw->locker-name locker-name->kw]])
  (:import [patbrown.supastore IStorageBackend]
           [java.util.concurrent.locks ReentrantLock]))

(def config
  {:pool (car/connection-pool {})
   :spec {:uri (***/get-secret ::connection-string)
          :ssl-fn :default}})
 
(defmacro with-redis [& body] `(car/wcar config ~@body))

(defn get
  ([{:redis/keys [connection locker]}] (get connection locker))
  ([connection locker]
   (car/wcar connection (car/get (kw->locker-name locker)))))

(defn set
  ([{:redis/keys [connection locker contents]}] (set connection locker contents))
  ([connection locker contents]
   (car/wcar connection (car/set (kw->locker-name locker) contents))))

(defn delete
  ([{:redis/keys [connection locker]}] (delete connection locker))
  ([connection locker]
   (car/wcar connection (car/del (kw->locker-name locker)))))

(defn exists?
  ([{:redis/keys [connection locker]}] (exists? connection locker))
  ([connection locker]
   (= 1 (car/wcar connection (car/exists (kw->locker-name locker))))))

(defn keys
  ([{:redis/keys [connection locker]}] (keys connection (if-not (nil? locker)
                                                          (kw->locker-name locker)
                                                          "")))
  ([connection locker] (car/wcar connection (car/keys locker))))

(defrecord RedisBackend [connection locker read-with write-with atom-ref]
  IStorageBackend
  (snapshot [_]
    (read-with (get connection locker)))
  (commit [this]
    (commit this :deref))
  (commit [this x]
    (let [f (fn [state]
              (set connection locker (write-with (if (= x :deref) @state x)))
              state)]
      (commit! atom-ref f this)))
  (cleanup [_]
    (delete connection locker)))

(def default-read-with  identity)
(def default-write-with identity)

(def default-config {:backing :redis
                     :connection config
                     :init {}
                     :read-with default-read-with
                     :write-with default-write-with })

(defn redis-supastore
  [{:keys [connection locker read-with write-with]
    :as provided-config}]
  (let [locker (kw->locker-name locker)]
    (create-supastore (merge default-config
                           provided-config
                           {:make-backend (partial ->RedisBackend connection locker read-with write-with)}))))

(defmethod supastore :redis
          [m]
          (redis-supastore m))

(defn locker [m]
  (if-not (map? m)
    (supastore (assoc default-config :locker m))
    (supastore (merge default-config m))))

(comment
(def t7 (supatom {:backing :redis :k :test/k :connection config :init {:a 1}}))
@t7)

(comment
  (defn empty-message-fn [msg] (println msg))
  (subscribe :message/id #'empty-message-fn)

;;
  )


