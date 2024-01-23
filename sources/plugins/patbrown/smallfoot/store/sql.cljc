(ns patbrown.smallfoot.store.sql
  (:require [babashka.pods :as pods]
                   [babashka.deps :as deps]
                   [clojure.string]
                   [clojure.java.io :as io]
                   [clojure.edn]
                   [honeysql.helpers :as h]
                   [honeysql.core :as sql]
                   [patbrown.secrets :as ***]
                   [patbrown.smallfoot.store :refer [supatom commit commit! create-supatom]])
  (:import [patbrown.smallfoot.store IStorageBackend]
                  [java.io Writer PushbackReader BufferedWriter]
                  [clojure.lang IAtom IDeref IRef IMeta IObj Atom
                   IPersistentMap PersistentTreeMap
                   IPersistentSet PersistentTreeSet
                   PersistentQueue IPersistentVector]))

(defn kw->locker-name [n]
  (if (string? n)
    n
    (let [n0 (str (namespace n) "_NDNMSP_" (name n))
          n1 (if (clojure.string/starts-with? n0 "_") (clojure.string/replace-first n0 #"\_" "") n0)
          n2 (clojure.string/replace  n1 #"\-" "_NDDASH_")
          n3 (clojure.string/replace n2 #"\?" "_NDQMARK_")
          n4 (clojure.string/replace n3 #"\$" "_NDDOLLAR_")
          n5 (clojure.string/replace n4 #"\!" "_NDBANG_")
          table-name (clojure.string/replace n5 #"\." "_NDDOT_")]
      table-name)))

(defn locker-name->kw [n]
  (if (keyword? n)
    n
    (let [n0 (clojure.string/replace n #"_NDDASH_" "-")
          n1 (clojure.string/replace n0 #"_NDQMARK_" "?")
          n2 (clojure.string/replace n1 #"_NDDOLLAR_" "$")
          n3 (clojure.string/replace n2 #"_NDBANG_" "!")
          n4 (clojure.string/replace n3 #"_NDDOT_" ".")
          n5 (clojure.string/split n4 #"_NDNMSP_")]
      (apply keyword n5))))

(def db
  {:port     (***/get-secret :patbrown.smallfoot.db/port)
   :dbname   (***/get-secret :patbrown.smallfoot.db/dbname)
   :host     (***/get-secret :patbrown.smallfoot.db/host)
   :dbtype   (***/get-secret :patbrown.smallfoot.db/dbtype)
   :user     (***/get-secret :patbrown.smallfoot.db/user)
   :password (***/get-secret :patbrown.smallfoot.db/password)})

#?(:bb (pods/load-pod 'org.babashka/postgresql "0.1.0"))
#?(:bb (require '[pod.babashka.postgresql :as jdbc]))

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
                       (let [inner-fn (clojure.core/get statement-reg fmt)]
                         [(inner-fn ctx)])))]
    (if-not (nil? return-with)
      (return-with result)
      result)))

(defn store-exists? [{:keys [db] :as ctx}]
  (exec! (assoc ctx :fmt :store-exists? :return-with #(-> % first :exists))))

(comment
(store-exists? {:db db :store "deleteme"})
;false
  (store-exists? {:db db :store "store"})
  ;true
  )

(defn ensure-store-exists!
  [{:keys [db] :as ctx}]
  (when-not (store-exists? ctx)
    (exec! (assoc ctx :fmt :create-store))))
(declare ensure-locker-exists! set-contents)

(comment
  (ensure-store-exists! {:db db :store "deleteme"})
  (store-exists? {:db db :store "deleteme"})
  ; true
  )

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


(defn get-contents [{:keys [store locker] :as ctx}]
  (let [locker-value (if (map? locker) (:locker locker) locker)
        locker (kw->locker-name locker-value)]
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


(comment
  



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
                     :locker (keyword "motherless" (str "noid-" (rand-int 1000000000)))
                     :read-with default-read-with
                     :write-with default-write-with})

(defn postgres-supatom
  [{:keys [connection locker read-with write-with]
    :as provided-config}]
  (let [locker (kw->locker-name locker)]
    (create-supatom (merge default-config
                           provided-config
                           {:make-backend (partial ->PostgresBackend connection locker read-with write-with)}))))

(defmethod supatom :postgres
          [m]
          (postgres-supatom m))

(defn locker
  ([] (locker {}))
  ([m]
   (if-not (map? m)
     (supatom (assoc default-config :locker m))
     (supatom (merge default-config m)))))

)
