(ns patbrown.smallfoot.locker
  #?(:bb (:require [babashka.pods :as pods]
                   [babashka.deps :as deps]
                   [honeysql.helpers :as h]
                   [honeysql.core :as sql]
                   [clojure.string]
                   [clojure.edn]
                   [patbrown.secrets :as ***]
                   [patbrown.smallfoot.db.impl :refer [commit commit! supastore generic-locker locker
                                                       kw->locker-name locker-name->kw ]])
     :clj (:require [clojure.string]
                    [clojure.edn]
                    [honeysql.helpers :as h]
                    [honeysql.core :as sql]
                    [clojure.java.io :as io]
                    [next.jdbc :as jdbc]
                    [patbrown.secrets :as ***]
                    [patbrown.smallfoot.db.impl :refer [commit commit! supastore generic-locker locker
                                                        kw->locker-name locker-name->kw]]))

  #?(:bb (:import [patbrown.smallfoot.db.impl IStorageBackend]
                  [java.io Writer PushbackReader BufferedWriter]
                  [clojure.lang IAtom IDeref IRef IMeta IObj Atom
                   IPersistentMap PersistentTreeMap
                   IPersistentSet PersistentTreeSet
                   PersistentQueue IPersistentVector])
     :clj (:import [patbrown.smallfoot.db.impl IStorageBackend]
                   [java.util.concurrent.locks ReentrantLock]
                   [java.io Writer PushbackReader BufferedWriter])))

#?(:bb (pods/load-pod 'org.babashka/postgresql "0.1.0"))
#?(:bb (require '[pod.babashka.postgresql :as jdbc]))

(def db
  {:port (***/get-secret ::port)
   :dbname (***/get-secret ::dbname)
   :host (***/get-secret ::host)
   :dbtype (***/get-secret ::dbtype)
   :user (***/get-secret ::user)
   :password (***/get-secret ::password)})

(def sanity? (jdbc/execute! db ["Select 1"]))

(defn store-exists-sql-statement [{:keys [store]}]
  (format
   "SELECT EXISTS (
   SELECT FROM pg_tables
   WHERE  tablename = '%s');" store))
(defn create-store-sql-statement [{:keys [store]}]
  (format "CREATE TABLE %s(id SERIAL, locker TEXT, contents TEXT, metamap TEXT)" store))
(defn delete-locker-sql-statement [{:keys [store locker]}]
  (format "DELETE FROM %s WHERE locker = %s") store locker)

(def statement-reg
  {:store-exists? store-exists-sql-statement
   :create-store create-store-sql-statement
   :delete-locker delete-locker-sql-statement})

(defn exec! [{:keys [db sql fmt one? return-with] :as ctx}]
  (let [f (if one? jdbc/execute-one! jdbc/execute!)
        result (f db (if-not (nil? sql)
                       (sql/format sql)
                       (let [inner-fn (get statement-reg fmt)]
                         [(inner-fn ctx)])))]
    (if-not (nil? return-with)
      (return-with result)
      result)))

(defn store-exists? [{:keys [db] :as ctx}]
  (exec! (assoc ctx :fmt :store-exists? :return #(-> % first :exists))))

(defn ensure-store-exists!
  [{:keys [db] :as ctx}]
  (when-not (store-exists? ctx)
    (exec! (assoc ctx :fmt :create-store))))
(declare ensure-locker-exists!)

(defn get-contents [{:keys [store locker] :as ctx}]
  (let [locker (if (map? locker) (:locker locker) locker)]
    (exec! (assoc ctx
                  :locker locker
                  :sql {:select [:*]
                        :from [(keyword store)]
                        :where [:= :locker locker]}))))

(defn set-contents
  ([{:keys [db store locker contents metamap] :as ctx}]
   (let [_ (ensure-locker-exists! ctx)]
     (if-not (nil? metamap)
       (exec! (assoc ctx
                     :db db :sql {:insert-into (keyword store)
                                  :columns [:locker :contents :metamap]
                                  :values [[locker contents metamap]]}))
       (exec! (assoc ctx :db db :sql {:insert-into (keyword store)
                                      :columns [:locker :contents]
                                      :values [[locker contents]]})))))
  ([ctx contents] (set-contents (assoc ctx :contents contents)))
  ([ctx contents metamap] (set-contents (assoc ctx :contents contents :metamap metamap))))

(defn delete-locker [{:keys [locker] :as ctx}]
  (exec! (assoc ctx :fmt :delete-locker :locker (kw->locker-name locker))))

(defn list-lockers [{:keys [db store] :as ctx}]
  (let [store-kw (keyword store)
        res (exec! (assoc ctx
                          :sql {:select [:*]
                                :from [store-kw]}
                          :return-with #(as-> (keyword (str store "/locker")) locker-name
                                          (map locker-name %)
                                          (set locker-name))))]
    res))

(defn locker-exists? [ctx]
  (contains? (list-lockers ctx) (locker-name->kw ctx)))

(defn ensure-locker-exists! [ctx]
  (ensure-store-exists! ctx)
  (when-not (locker-exists? ctx) (set-contents (assoc ctx :contents " "))))

(defrecord PostgresBackend [db store locker read-with write-with atom-ref]
  IStorageBackend
  (init [_] (set-contents {:db db :store store :locker locker :contents (write-with " ")}))
  (snapshot [_]
    (read-with (get-contents {:db db :store store :locker locker})))
  (commit [this]
    (commit this :deref))
  (commit [this x]
    (let [f (fn [state]
              (set-contents {:db db :store store :locker locker :contents (write-with (if (= x :deref) @state x))})
              state)]
      (commit! atom-ref f (fn [e] (println e)))))
  (cleanup [_]
    (delete-locker {:db db :store store :locker locker})))


(def default-read-with  identity)
(def default-write-with identity)

(def default-config {:backing :postgres
                     :db db
                     :store "store"
                     :locker (keyword "motherless" (str "x-" (rand-int 1000000000)))
                     :init {}
                     :read-with default-read-with
                     :write-with default-write-with})
;
(defn postgres-backend [m]
  (map->PostgresBackend (merge default-config m)))

(defmethod locker :postgres [ctx]
  (let [{:keys [locker] :as ctx} (merge default-config ctx)
        locker (kw->locker-name locker)
        _ (ensure-locker-exists! ctx)]
    (generic-locker (assoc ctx
                           :make-backend postgres-backend
                           :locker locker))))

#_(def b (generic-locker (merge default-config {:backing :postgres
                                              :make-backend postgres-backend})))

