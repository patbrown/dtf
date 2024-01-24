(ns tiny.db.sql-statements
  (:require [tiny.db.details :refer [kw->storage-name]]))

;; SQL Statements as functions that take a ctx and return a string
(defn store-exists-sql-statement [{:keys [store]}]
  (format "SELECT EXISTS (SELECT relname FROM pg_class WHERE relname = '%s');" (kw->storage-name store)))
(defn create-store-sql-statement [{:keys [store]}]
  (format "CREATE TABLE %s(locker varchar(256) unique, contents text, bom text, log text);" (kw->storage-name store)))
(defn delete-store-sql-statement [{:keys [store]}]
  (str "DROP TABLE " (kw->storage-name store) ";"))
(defn list-lockers-sql-statement [{:keys [store]}]
  (format "SELECT locker FROM %s;" (kw->storage-name store)))
(defn locker-exists-sql-statement [{:keys [store locker]}]
  (format "SELECT locker FROM %s WHERE locker='%s';" (kw->storage-name store) (kw->storage-name locker)))
(defn create-locker-sql-statement [{:keys [store locker contents bom log]}]
  (format "INSERT INTO %s (locker, contents, bom, log) VALUES ('%s', '%s', '%s', '%s');"
          (kw->storage-name store)
          (kw->storage-name locker)
          (str (or contents (str {})))
          (or (str bom) (str {}))
          (or (str log) (str []))))
(defn delete-locker-sql-statement [{:keys [store locker]}]
  (format "DELETE FROM %s WHERE locker = '%s'" (kw->storage-name store) (kw->storage-name locker)))
(defn get-locker-sql-statement [{:keys [store locker]}]
  (str "SELECT * FROM " (kw->storage-name store) " WHERE locker = '" (kw->storage-name locker) "' LIMIT 1"))
(defn set-locker-sql-statement [{:keys [store locker datastructure contents]}]
  (format "UPDATE %s SET %s = '%s' WHERE locker = '%s'" (kw->storage-name store) (name datastructure) contents (kw->storage-name locker)))

(def manifest
  {:list-lockers list-lockers-sql-statement
   :store-exists? store-exists-sql-statement
   :locker-exists? locker-exists-sql-statement
   :create-store create-store-sql-statement
   :create-locker create-locker-sql-statement
   :delete-store delete-store-sql-statement
   :delete-locker delete-locker-sql-statement
   :get-locker get-locker-sql-statement
   :set-locker set-locker-sql-statement})
