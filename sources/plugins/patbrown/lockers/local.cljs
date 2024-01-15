(ns patbrown.lockers.local
  (:refer-clojure :exclude [get keys set exists?])
  (:require [clojure.edn]
            [patbrown.supastore :refer [commit cleanup commit! supastore create-supastore
                                                    kw->locker-name locker-name->kw]])
  (:use [patbrown.supastore :only [IStorageBackend ICommiter]]))

(defn kw->locker-name [n]
  (let [s (if (string? n) n (str n))]
    (clojure.string/replace s #"\." "_")))

(defn locker-name->kw [n]
  (if (keyword? n) n
      (clojure.edn/read-string (clojure.string/replace n #"\_" "."))))

(defn ezdest [locker] (if (map? locker) (:locker locker) locker))

(defn get
  ([{:keys [connection locker]}] (get connection locker))
  ([connection locker]
   (let [locker (ezdest locker)]
    (.getItem connection locker))))

(defn set
  ([{:keys [connection locker contents]}] (set connection locker contents))
  ([connection locker contents]
   (.setItem connection locker contents)))

(defn delete!
  ([{:keys [connection locker]}] (delete! connection locker))
  ([connection locker]
   (let [locker (ezdest locker)]
     (.removeItem connection locker))))

(defn keys [connection]
  (let [conn (if (map? connection) (:connection connection) connection)]
    (clojure.core/set (js->clj (js-keys conn)))))

(defn exists?
  ([{:keys [connection locker]}] (exists? connection locker))
  ([connection locker]
   (let [locker (ezdest locker)]
     (contains? (keys connection) locker))))

(defrecord LocalBackend [connection locker read-with write-with atom-ref]
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
    (delete! connection locker)))

(def default-read-with  identity)
(def default-write-with identity)

(def default-local-config {:backing :local
                           :connection js/localStorage
                           :init {}
                           :read-with default-read-with
                           :write-with default-write-with})

(def default-session-config {:backing :session
                             :connection js/sessionStorage
                             :init {}
                             :read-with default-read-with
                             :write-with default-write-with})

(defn local-supastore
  [{:keys [backing connection locker read-with write-with] :as provided-config}]
  (let [locker (kw->locker-name locker)]
    (create-supastore (merge (if (= backing :local)
                               default-local-config
                               default-session-config)
                             provided-config
                             {:make-backend (partial ->LocalBackend connection locker read-with write-with)}))))

(defn local-locker [m]
  (if (map? m)
    (create-supastore (update (merge default-local-config m) :locker kw->locker-name))
    (create-supastore (assoc default-local-config :locker (kw->locker-name m)))))

(defn session-locker [m]
  (if (map? m)
    (create-supastore (update (merge default-session-config m) :locker kw->locker-name))
    (create-supastore (assoc default-local-config :locker (kw->locker-name m)))))
