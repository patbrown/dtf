(ns tiny.db
  (:require [babashka.pods :as pods]
            [babashka.deps :as deps]
            [tiny.secrets :refer [get-secret]]
            [tiny.db.details :refer [kw->storage-name storage-name->kw]]
            [tiny.db.sql-statements :as statements]
            [honeysql.helpers :as h]
            [honeysql.core :as sql]
            [medley.core]))

;; Am I connected?
#_(def sanity? (jdbc/execute! db ["Select 1"]))

;; Loading sql pod
(pods/load-pod 'org.babashka/postgresql "0.1.0")
(require '[pod.babashka.postgresql :as jdbc])

(defn exec! [{:keys [db statement return-with] :as ctx}]
  (let [raw-statement (clojure.core/get statements/manifest statement)
        sql (raw-statement ctx)
        result (jdbc/execute! db [sql])]
    (if-not (nil? return-with)
      (return-with result)
      result)))

(defn ezexec! [statement ctx]
  (exec! (assoc ctx :statement statement)))

(declare store-exists? create-store)
(defn create-store [ctx]
  (when-not (store-exists? ctx)
    (ezexec! :create-store ctx)))
(defn store-exists? [ctx]
  (exec! (assoc ctx :statement :store-exists? :return-with #(-> % first :exists))))
(defn delete-store [ctx]
  (when (store-exists? ctx) (ezexec! :delete-store ctx)))

(declare locker-exists? create-locker)

(defn list-lockers [{:keys [store] :as ctx}]
  (let [results (flatten (map vals (ezexec! :list-lockers ctx)))]
    (set (map storage-name->kw results))))
(defn create-locker [ctx]
  (when-not (store-exists? ctx)
    (create-store ctx))
  (when-not (locker-exists? ctx)
    (ezexec! :create-locker ctx)))
(defn locker-exists? [ctx]
  (let [resp (ezexec! :locker-exists? ctx)]
    (if (empty? resp) false true)))
(defn delete-locker [ctx]
  (ezexec! :delete-locker ctx))

(defn get-locker [ctx]
  (if (locker-exists? ctx)
    (ezexec! :get-locker ctx)
    nil))

(defn set-locker [ctx]
  (if (locker-exists? ctx)
    (ezexec! :set-locker ctx)
    false))

(defn prep-locker [{:keys [atom-ref] :as ctx}]
  (let [_ (create-locker ctx)
        locker-contents (get-locker ctx)
        current-value (or locker-contents @atom-ref " ")]
    (when (not= locker-contents current-value)
      (set-locker current-value))))

(defprotocol ISimpleStorage
  (init [this])
  (commit [this])
  (snapshot [this])
  (destroy [this]))

(defrecord StorageLocker [db store locker datastructure read-with write-with atom-ref lock]
  clojure.lang.IAtom
  (swap [this f]
    (locking lock
      (let [original-value (read-with @atom-ref)
            new-value (write-with (f original-value))
            result (reset! atom-ref new-value)
            _ (commit this)]
        result)))
  (swap [this f a]
    (locking lock
      (let [original-value (read-with @atom-ref)
            new-value (write-with (f original-value a))
            result (reset! atom-ref new-value)]
        (commit this)
        result)))
  (swap [this f a b]
    (locking lock
      (let [original-value (read-with @atom-ref)
            new-value (write-with (f original-value a b))
            result (reset! atom-ref new-value)]
        (commit this)
        result)))
  (swap [this f a b c]
    (locking lock
      (let [original-value (read-with @atom-ref)
            new-value (write-with (f original-value a b))
            result (reset! atom-ref new-value)]
        (commit this)
        result)))  
  (reset [this new-value]
    (locking lock
      (let [result (reset! atom-ref new-value)]
        (commit this)
        result)))
  clojure.lang.IDeref
  (deref [_] @atom-ref)
  ISimpleStorage
  (init [this]
    (prep-locker (assoc this :contents @atom-ref)))
  (commit [this]
    (set-locker (assoc this :contents @atom-ref)))
  (snapshot [this]
    (get-locker (assoc this :content @atom-ref)))
  (destroy [this]
    (delete-locker this)))

(defmethod print-method StorageLocker [x, ^Writer w]
  ((get-method print-method clojure.lang.IRecord) x w))

(defn *storage-locker [default-locker]
  (fn [ctx]
    (let [a (map->StorageLocker (merge default-locker ctx))
        _ (init a)]
      a)))

(def actions {:create-store create-store
              :store-exists? store-exists?
              :delete-store delete-store
              :list-lockers list-lockers
              :create-locker create-locker
              :locker-exists? locker-exists?
              :delete-locker delete-locker
              :get-locker get-locker})

(defn- *access-layer [defaults]
  (fn [{:keys [action] :as ctx}]
    (let [f (get-in actions [action])]
      (f (medley.core/deep-merge defaults ctx)))))
(def ^:dynamic *access-point* nil)
(def access-layer (*access-layer *access-point*))

(defn *!
  ([action] (access-layer {:action action}))
  ([action store] (access-layer {:action action :store store}))
  ([action store locker] (access-layer {:action action :store store :locker locker}))
  ([action store locker path] (access-layer {:action action :store store :locker locker :path path}))
  ([action store locker path function]
   (access-layer {:action action :store store :locker locker :path path :function function}))
  ([action store locker path function args]
   (access-layer {:action action :store store :locker locker :path path :function function :args args})))

(defn ! [v]
  (if (and (vector? v) (every? vector? v))
    (doall (map ! v))
    (apply *! v)))

(comment
(access-layer {:action :list-lockers})
  (! [:list-lockers])
(! [[:list-lockers]
     [:list-lockers]])  
                                        ;
  )


(deftype Link [atom-ref meta validate-with watches read-with write-with deref-with check-with id]
          Object
          (equals [this other]
            (and (instance? Link other)
                 (= atom-ref (.-atom-ref other))))
          clojure.lang.IDeref
          (deref [this]
            (let [derefer (or deref-with (fn [_ a getf]
                                           (getf (deref a))))]
              (derefer this atom-ref read-with)))
          clojure.lang.IAtom
          (swap [this f]
            (reset! this (f (deref this))))
          (swap [this f x]
            (reset! this (f (deref this) x)))
          (swap [this f x y]
            (reset! this (f (deref this) x y)))
          (swap [this f x y z]
            (reset! this (apply f (deref this) x y z)))
          (compareAndSet [_ old-value new-value]
            (let [_ (compare-and-set! atom-ref old-value new-value)
                  commit? (= old-value new-value)]
              (when commit?
                (swap! atom-ref new-value))))
          (reset [this new-value]
            (if write-with
              (let [validate (.-validate-with this)]
                (when-not (nil? validate)
                  (assert (validate new-value) "Value you are trying to set is invalid"))
                (do (swap! atom-ref write-with new-value)
                    new-value))
              (throw (Exception. "READ ONLY ATOM: Cannot be reset, no setter present.")))))











(defn link->
  ([{:keys [atom-ref get-with set-with meta validate-with watches deref-with check-with id]}]
   (Link. atom-ref meta validate-with watches get-with set-with deref-with check-with id))
  ([atom-ref get-with] (link-> atom-ref get-with nil {}))
  ([atom-ref get-with set-with] (link-> atom-ref get-with set-with {}))
  ([atom-ref get-with set-with {:keys [meta validate-with watches deref-with check-with id]}]
   (Link. atom-ref meta validate-with watches get-with set-with deref-with check-with id)))

(defn cursor->
  ([{:keys [atom-ref path]}] (cursor-> atom-ref path))
  ([atom-ref path]
   (if-not (seq path)
     atom-ref
     (link-> atom-ref
             #(get-in % path)
             #(assoc-in %1 path %2)
             {:id [::cursor atom-ref path]}))))

(defn xform->
  ([{:keys [atom-ref xform dest]}] (xform-> atom-ref xform dest))
  ([atom-ref xform dest]
   (link-> atom-ref
           (fn [x] (reset! (or dest atom-ref) (xform x)))
           (fn [x] (xform x)))))

(defn count->
  ([{:keys [atom-ref path]}]
   (link-> atom-ref path))
  ([atom-ref path] (link-> atom-ref #(let [v (get-in % (or path []))]
                                       (count v)))))

(defn head->
  ([{:keys [atom-ref amount]}]
   (link-> atom-ref amount))
  ([atom-ref amount] (link-> atom-ref #(take amount (reverse %)))))

(defn backup->
  ([{:keys [atom-ref dest]}]
   (backup-> atom-ref dest))
  ([atom-ref dest] (link-> atom-ref #(let [v (get-in % [])]
                                       (reset! dest v)))))

#?(:clj (defmethod print-method Link [l w]
          (.write w "#")
          (.write w (-> l class .getName))
          (.write w " {:type :link, :val ")
          (.write w (-> (.-atom_ref l) deref pr-str))
          (.write w "}")))

(defn link? [thing]
  (instance? Link thing))
