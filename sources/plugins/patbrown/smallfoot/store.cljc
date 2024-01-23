(ns patbrown.smallfoot.store
  (:require [clojure.edn]
            [babashka.fs :as fs])
  (:import [clojure.lang IAtom IDeref IRef IMeta IObj Atom
            IPersistentMap PersistentTreeMap
            IPersistentSet PersistentTreeSet
            PersistentQueue IPersistentVector]
           [java.io Writer]))

(defn- sync-commit*
  [c f handle-error]
  (try
    (f c)
    (catch Exception e
      (when handle-error
        (handle-error e)))))

(defprotocol IStorageBackend
  (snapshot [this])
  (commit   [this] ;; applicable only to supatom re-commits via the error-handler
            [this x])
  (cleanup  [this]))

(defprotocol ICommiter
  (commit! [this f ef]))

(extend-protocol ICommiter
  Atom
  (commit! [this f handle-error]
    (sync-commit* this f handle-error))
  Object
  (commit! [this f handle-error]
    (sync-commit* this f handle-error)))

(deftype Supatom [backend atom-ref]
         IAtom
         (swap [_ f]
           (let [_ (swap! atom-ref f)
                 result @atom-ref]
             (commit backend result)
             result))
         (swap [_ f arg]
           (let [_ (swap! atom-ref f arg)
                 result @atom-ref]
             (commit backend result)
             result))
         (swap [_ f arg1 arg2]
           (let [_ (swap! atom-ref f arg1 arg2)
                 result @atom-ref]
             (commit backend result)
             result))
         (swap [_ f arg1 arg2 more]
           (let [_ (apply swap! atom-ref f arg1 arg2 more)
                 result @atom-ref]
             (commit backend result)
             result))
         (compareAndSet [_ oldval newval]
           (let [_ (compare-and-set! atom-ref oldval newval)
                 commit? (= oldval newval)]
             (when commit?
               (commit backend newval))
             commit?))
         (reset [_ newval]
           (let [_ (reset! atom-ref newval)
                 result @atom-ref]
             (commit backend result)
             result))
         IDeref
         (deref [_]
           (deref atom-ref)))

(defn create-supatom [{:keys [backend init]}]
         (let [atom-ref (atom nil)
               backend (backend atom-ref)
               supatom (Supatom. backend atom-ref)
               contents (snapshot backend)]
           (if (nil? contents)
             (reset! atom-ref init)
             (reset! atom-ref contents))
           supatom))

(defmulti supatom :backing)

