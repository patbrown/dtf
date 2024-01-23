(ns patbrown.smallfoot.db.impl
  (:require [clojure.edn]
            [clojure.string])
  #?(:bb (:import [java.io Writer Closeable])
     :clj (:import [clojure.lang IAtom IDeref IRef IMeta IObj IReference Atom]
                   [java.util.concurrent.atomic AtomicBoolean]
                   [java.util.concurrent.locks ReentrantLock Lock]
                   [java.io Writer Closeable])))

(defn kw->locker-name [n]
  (if (map? n) (kw->locker-name (:locker n))
      (if (string? n)
        n
        (let [n0 (str (namespace n) "_NDNMSP_" (name n))
              n1 (if (clojure.string/starts-with? n0 "_") (clojure.string/replace-first n0 #"\_" "") n0)
              n2 (clojure.string/replace  n1 #"\-" "_NDDASH_")
              n3 (clojure.string/replace n2 #"\?" "_NDQMARK_")
              n4 (clojure.string/replace n3 #"\$" "_NDDOLLAR_")
              n5 (clojure.string/replace n4 #"\!" "_NDBANG_")
              table-name (clojure.string/replace n5 #"\." "_NDDOT_")]
          table-name))))

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

;; Storage Protocol is the same across platforms
(defprotocol IStorageBackend
  (init [this])
  (snapshot [this])
  (commit   [this] [this x])
  (cleanup  [this]))
(defprotocol ICommiter
  (commit! [this f ef]))

;; The only difference is the js/Exception
#?(:clj (defn- sync-commit*
          [c f handle-error]
          (try
            (f c)
            (catch Exception e
              (when handle-error
                (handle-error e)))))

   :cljs (defn- sync-commit*
           [c f handle-error]
           (try
             (f c)
             (catch js/Exception e
               (when handle-error
                 (handle-error e))))))


;; Just JVM, these are only macros, because of supatom's long history.
;; The locks are superhelpful, but they only work JVM, in the end I didn't share so much code
;; REWRITE THIS WHOLE THING NEXT YEAR
#?(:bb nil
   :clj (defmacro assert-not-released!
          [release-fn]
          `(when (~release-fn)
             (throw (IllegalStateException. "supatom/duragent has been released!")))))
#?(:bb nil
   :clj (defmacro with-locking
          "Like `locking`, but expects a `java.util.concurrent.locks.Lock` <lock>."
          [lock & body]
          `(try
             (.lock ~lock)
             ~@body
             (finally (.unlock ~lock)))))
#?(:bb nil
   :clj (defmacro  with-read-location
          [loc mem-expr storage-backend f args]
          `(if (or (=  ~loc :mem)
                   (nil? ~loc))
             ~mem-expr
             (apply ~f (some-> ~storage-backend snapshot) ~args))))
#?(:bb nil
   :clj (defmacro lock? [l]
          `(instance? Lock ~l)))

#?(:bb nil
   :clj (defn releaser []
          (let [released? (AtomicBoolean. false)]
            (fn release
              ([]
               (.get released?))
              ([value]
               (.set released? value)
               value)))))

;; Why is CLJS just enough different to suck?
(extend-protocol ICommiter
  #?(:cljs IAtom :bb clojure.lang.IAtom :clj Atom)
  (commit! [this f handle-error]
    (sync-commit* this f handle-error))
  Object
  (commit! [this f handle-error]
    (sync-commit* this f handle-error)))

#?(:bb (deftype Supastore [backend atom-ref]
         clojure.lang.IAtom
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
         clojure.lang.IDeref
         (deref [_]
           (deref atom-ref)))

   :clj (deftype Supastore [backend atom-ref ^Lock lock release]
          IAtom
          (swap [_ f]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (swap! atom-ref f)
                    result @atom-ref]
                (commit backend result)
                result)))
          (swap [_ f arg]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (swap! atom-ref f arg)
                    result @atom-ref]
                (commit backend result)
                result)))
          (swap [_ f arg1 arg2]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (swap! atom-ref f arg1 arg2)
                    result @atom-ref]
                (commit backend result)
                result)))
          (swap [_ f arg1 arg2 more]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (apply swap! atom-ref f arg1 arg2 more)
                    result @atom-ref]
                (commit backend result)
                result)))
          (compareAndSet [_ oldval newval]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (compare-and-set! atom-ref oldval newval)
                    commit? (= oldval newval)]
                (when commit?
                  (commit backend newval))
                commit?)))
          (reset [_ newval]
            (assert-not-released! release)
            (with-locking lock
              (let [_ (reset! atom-ref newval)
                    result @atom-ref]
                (commit backend result)
                result)))
          IRef
          (setValidator [_ validator]
            (.setValidator ^IRef atom-ref validator))
          (getValidator [_]
            (.getValidator ^IRef atom-ref))
          (addWatch [this watch-key watch-fn]
            (.addWatch ^IRef atom-ref watch-key watch-fn)
            this)
          (removeWatch [this watch-key]
            (.removeWatch ^IRef atom-ref watch-key)
            this)
          (getWatches [_]
            (.getWatches ^IRef atom-ref))
          IDeref
          (deref [_]
            (deref atom-ref))
          IObj
          (withMeta [_ meta-map]
            (Supastore. backend (with-meta atom-ref meta-map) ^Lock lock release))
          IMeta
          (meta [_]
            (meta atom-ref))
          IReference
          (resetMeta [_ meta-map]
            (reset-meta! atom-ref meta-map))
          (alterMeta [_ f args]
            (alter-meta! atom-ref f args))
          Closeable
          (close [_]
            (when-not (release)
              (with-locking lock
                (cleanup backend)
                (release true)))))

   :cljs (deftype Supastore [backend atom-ref]
           ISwap
           (-swap! [_ f]
             (let [_ (swap! atom-ref f)
                   result @atom-ref]
               (commit backend result)
               result))
           (-swap! [_ f arg]
             (let [_ (swap! atom-ref f arg)
                   result @atom-ref]
               (commit backend result)
               result))
           (-swap! [_ f arg1 arg2]
             (let [_ (swap! atom-ref f arg1 arg2)
                   result @atom-ref]
               (commit backend result)
               result))
           (-swap! [_ f arg1 arg2 more]
             (let [_ (apply swap! atom-ref f arg1 arg2 more)
                   result @atom-ref]
               (commit backend result)
               result))
           IReset
           (-reset! [_ newval]
             (let [_ (reset! atom-ref newval)
                   result @atom-ref]
               (commit backend result)
               result))
           IWatchable
           (-add-watch [this key f]
             (add-watch atom-ref [(:locker this) key]
                        (fn [_ _ old-value new-value]
                          (when-not (= old-value new-value)
                            (f key this old-value new-value))))
             this)
           (-remove-watch [this key]
             (remove-watch atom-ref [(:locker this) key])
             this)
           IDeref
           (-deref [_]
             (deref atom-ref))
           IMeta
           (-meta [_] meta)
           IWithMeta
           (-with-meta [o meta]
             (Supastore. backend atom-ref))))

#?(:bb nil
   :clj (defmethod print-method Supastore [^Supastore supastore ^Writer w]
          (.write w "#")
          (.write w (-> supastore class .getName))
          (.write w (format " 0x%x " (System/identityHashCode supastore)))
          (.write w " {:status :ready, :val ")
          (.write w (-> (.-atom_ref supastore) deref pr-str))
          (.write w "}")))

#?(:bb
   (defn generic-locker
     [{:keys [make-backend init]}]
     (let [raw-atom (atom nil)
           backend (make-backend raw-atom)
           supastore (Supastore. backend raw-atom)
           contents (snapshot backend)]
       (if (nil? contents)
         (reset! supastore init)
         (reset! supastore contents))))

   :clj
   (defn generic-locker
     [{:keys [make-backend lock init] :or {init {}
                                           lock (ReentrantLock.)}}]
     (assert (lock? lock) "The <lock> provided is NOT a valid implementation of `java.util.concurrent.locks.Lock`!")
     (let [atom-ref (atom nil)
           backend (make-backend {:atom-ref atom-ref})
           supastore (Supastore. backend atom-ref lock (releaser))
           _ (commit backend (str {}))
           contents (snapshot backend)]
       (if (nil? contents)
         (reset! atom-ref init)
         (reset! atom-ref contents))
       supastore))

   :cljs (defn generic-locker
           [{:keys [make-backend init] :or {init {}}}]
           (let [raw-atom (atom nil)
                 backend (make-backend raw-atom)
                 supastore (Supastore. backend raw-atom)
                 _ (commit backend (str {}))
                 contents (snapshot backend)]
             (if (nil? contents)
               (reset! supastore init)
               (reset! supastore contents)))))

(defmulti locker :backing)
