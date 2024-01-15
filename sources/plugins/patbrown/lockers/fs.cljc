(ns patbrown.lockers.fs
  (:refer-clojure :exclude [keys set get])
  (:require [babashka.fs :as fs]
            [clojure.string]
            [clojure.edn]
            [patbrown.supastore :refer [commit cleanup commit! supastore create-supastore
                                                    kw->locker-name locker-name->kw]])
  #?(:bb (:import [patbrown.supastore IStorageBackend IStorage])
     :clj (:import [java.util.concurrent.locks ReentrantLock]
                   [patbrown.supastore IStorageBackend IStorage])))

(defn to-path [connection locker]
  (str connection "/" locker))
(def as-file fs/file)

(defn get
  ([{:fs/keys [connection locker]}]
   (get connection locker))
  ([connection locker]
   (slurp (to-path connection (kw->locker-name locker)))))

(defn set
  ([{:fs/keys [connection locker contents]}]
   (set connection locker contents))
  ([connection locker contents]
   (spit (to-path connection locker) contents)))

(defn delete
  ([{:fs/keys [connection locker]}]
   (delete connection locker))
  ([connection locker]
   (fs/delete-if-exists (to-path connection locker))))

(defn keys [connection]
  (let [f (fn [conn] (into #{} (map str (fs/list-dir conn))))]
    (if (map? connection)
      (f (:fs/connection connection))
      (f connection))))

(defn exists?
  ([{:fs/keys [connection locker]}]
   (exists? connection locker))
  ([connection locker]
   (contains? (keys connection) (to-path connection locker))))

(defrecord FileBackend [connection locker read-with write-with atom-ref]
  IStorageBackend
  (snapshot [_]
    (when-not (zero? (.length (as-file (str connection "/" locker))))
      (try (read-with (get connection locker))
           (catch Exception e
             (throw (ex-info (str "Unable to read data from file " (str connection "/" locker)  "!")
                             {:file-path (str connection "/" locker)}
                             e))))))
  (commit [this]
    (commit this :deref))
  (commit [this data]
    (let [f (fn [state]
              (set connection locker (write-with (if (= data :deref) @state data)))
              state)]
      (commit! atom-ref f data)))
  (cleanup [_]
    (or (delete! connection locker)
        (throw (Exception. (str "Could not delete " (str connection "/" locker)))))))

(def default-read-with  clojure.edn/read-string)
(def default-write-with str)
(def default-config {:backing :file
                     :connection "resources/code-as-data"
                     :init {}
                     :read-with default-read-with
                     :write-with default-write-with })

(defn file-supastore
  [{:keys [connection locker read-with write-with]
    :as provided-config}]
  (let [locker (kw->locker-name locker)]
    (create-supastore (merge default-config
                           provided-config
                           {:make-backend (partial ->FileBackend connection locker read-with write-with)}))))

(defmethod supastore :file
          [m]
          (file-supastore m))

(defn locker [m]
  (if-not (map? m)
    (supastore (assoc default-config :locker m))
    (supastore (merge default-config m))))
