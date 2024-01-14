(ns net.drilling.plugins.lockers.postgres
  (:refer-clojure :exclude [get set keys exists?])
  #?(:bb (:require [babashka.pods :as pods]
                   [babashka.deps :as deps]
                   [honeysql.helpers :as h]
                   [honeysql.core :as sql]
                   [clojure.string]
                   [clojure.edn]
                   [net.drilling.plugins.secrets :as ***]
                   [net.drilling.plugins.supastore :refer [commit commit! supastore create-supastore
                                                           kw->locker-name locker-name->kw]])
     :clj (:require [clojure.string]
                    [clojure.edn]
                    [honeysql.helpers :as h]
                    [honeysql.core :as sql]
                    [clojure.java.io :as io]
                    [net.drilling.plugins.secrets :as ***]
                    [net.drilling.plugins.supastore :refer [commit commit! supastore create-supastore
                                                            kw->locker-name locker-name->kw]]))
  #?(:bb (:import [net.drilling.plugins.supastore IStorageBackend]
                  [java.io Writer PushbackReader BufferedWriter]
                  [clojure.lang IAtom IDeref IRef IMeta IObj Atom
                   IPersistentMap PersistentTreeMap
                   IPersistentSet PersistentTreeSet
                   PersistentQueue IPersistentVector])
     :clj (:import [net.drilling.plugins.supastore IStorageBackend]
                   [java.util.concurrent.locks ReentrantLock]
                   [java.io Writer PushbackReader BufferedWriter])))

#?(:bb (pods/load-pod 'org.babashka/postgresql "0.1.0"))
#?(:bb (require '[pod.babashka.postgresql :as jdbc])
   :clj (require '[next.jdbc :as jdbc]))

(def db
  {:port     (***/get-secret ::port)
   :dbname   (***/get-secret ::dbname)
   :host     (***/get-secret ::host)
   :dbtype   (***/get-secret ::dbtype)
   :user     (***/get-secret ::user)
   :password (***/get-secret ::password)
   })

(def sanity? (jdbc/execute! db ["Select 1"]))

(defn create-store
  ([] (create-store "store"))
  ([store] (jdbc/execute! db [(format "CREATE TABLE %s(id SERIAL, locker TEXT, contents TEXT, metamap TEXT)" store)])))

(defn exec! [sql] (jdbc/execute! db (sql/format sql)))
(defn exec1! [sql] (jdbc/execute-one! db (sql/format sql)))

(defn get [locker]
  (let [locker (if (map? locker) (:locker locker) locker)]
    (exec1! {:select [:*]
          :from [:store]
          :where [:= :locker locker]})))

(defn set
  ([{:keys [locker contents metamap]}]
   (if (nil? metamap)
     (set locker contents)
     (set locker contents metamap)))
  ([k v] (let [res (exec1! {:insert-into :store
                            :columns [:locker :contents]
                            :values [[k v]]})]
           res))
  ([k v m]
   (let [res (exec1! {:insert-into :store
                      :columns [:locker :contents :metamap]
                      :values [[k v m]]})]
     res)))

(defn delete [locker]
  (jdbc/execute-one! db ["DELETE FROM  WHERE locker = ?" locker]))

(defn keys [] (let [res (exec! {:select [:*]
                                :from [:store]})]
                (set (map :store/locker res))))

(defn exists? [locker]
  (contains? (keys) (locker-name->kw locker)))

(defrecord PostgresBackend [connection locker read-with write-with atom-ref]
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

(def default-config {:backing :postgres
                     :connection db
                     :init {}
                     :read-with default-read-with
                     :write-with default-write-with })

(defn postgres-supastore
  [{:keys [connection locker read-with write-with]
    :as provided-config}]
  (let [locker (kw->locker-name locker)]
    (create-supastore (merge default-config
                           provided-config
                           {:make-backend (partial ->PostgresBackend connection locker read-with write-with)}))))

(defmethod supastore :postgres
          [m]
          (postgres-supastore m))

(defn locker [m]
  (if-not (map? m)
    (supastore (assoc default-config :locker m))
    (supastore (merge default-config m))))
