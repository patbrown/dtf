(ns net.drilling.plugins.supalink
  (:require [clojure.edn]
            [medley.core]
            [pyramid.core])
  #?(:bb (:import [java.io Writer Closeable])
     :clj (:import [clojure.lang IAtom IDeref IRef IMeta IObj IReference Atom]
                   [java.io Writer])))

#?(:bb (deftype Supalink [atom-ref meta validate-with watches read-with write-with deref-with check-with id]
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
             (throw (Exception. "READ ONLY ATOM: Cannot be reset, no setter present."))))
         clojure.lang.IDeref
         (deref [this]
           (let [derefer (or deref-with (fn [_ a getf]
                                          (getf (deref a))))]
             (derefer this atom-ref read-with))))

   :clj (deftype Supalink [atom-ref meta validate-with watches read-with write-with deref-with check-with id]
          Object
          (equals [this other]
            (and (instance? Supalink other)
                 (= atom-ref (.-atom-ref other))))
          (hashCode [this]
            (when (== hash -1)
              (set! hash (.hashCode (.getName this))))
            hash)
          IDeref
          (deref [this]
            (let [derefer (or deref-with (fn [_ a getf]
                                           (getf (deref a))))]
              (derefer this atom-ref read-with)))
          IObj
          (meta [this] meta)
          (withMeta [this meta]
            (Supalink. atom-ref meta validate-with watches
                       read-with write-with deref-with check-with id))
          IAtom
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
              (throw (Exception. "READ ONLY ATOM: Cannot be reset, no setter present."))))
          IRef
          (setValidator [_ validator]
            (.setValidator atom-ref validator))
          (getValidator [_]
            (.getValidator atom-ref))
          (addWatch [this watch-key watch-fn]
            (add-watch atom-ref [this watch-key]
                       (fn [_ _ old-value new-value]
                         (when-not (= old-value new-value)
                           (let [o (read-with old-value)
                                 n (read-with new-value)]
                             (when-not (= o n)
                               (watch-fn key this o n))))))
            this)
          (removeWatch [this watch-key]
            (remove-watch atom-ref [this watch-key])
            this)
          (getWatches [_]
            (.getWatches atom-ref)))

   :cljs (deftype Supalink [atom-ref meta validate-with watches
                            read-with write-with deref-with check-with id]
           Object
           (equiv [this other]
             (-equiv this other))

           IAtom

           IEquiv
           (-equiv [this other]
             (and (instance? Supalink other)
                  (= identical? this other)))
           IDeref
           (-deref [this]
             (let [derefer (or deref-with (fn [_ a rw]
                                            (rw (-deref a))))]
               (derefer this atom-ref read-with)))
           IMeta
           (-meta [_] meta)
           IWithMeta
           (-with-meta [o meta]
             (Supalink. atom-ref meta validate-with watches
                        read-with write-with deref-with check-with id))
           IWatchable
           (-add-watch [this key f]
             (add-watch atom-ref [(or check-with this) key]
                        (fn [_ _ old-value new-value]
                          (when-not (= old-value new-value)
                            (let [old (read-with old-value)
                                  new (read-with new-value)]
                              (when-not (= old new)
                                (f key this old new))))))
             this)
           (-remove-watch [this key]
             (remove-watch atom-ref [(or check-with this) key])
             this)
           IReset
           (-reset! [a new-value]
             (if write-with
               (let [validate (.-validate-with a)]
                 (when-not (nil? validate)
                   (assert (validate new-value) "Validator rejected reference state"))
                 (do (swap! atom-ref write-with new-value)
                     new-value))
               (throw (js/Error. "READ ONLY ATOM: no set fn provided for this atom."))))
           ISwap
           (-swap! [a f]
             (reset! a (f (-deref a))))
           (-swap! [a f x]
             (reset! a (f (-deref a) x)))
           (-swap! [a f x y]
             (reset! a (f (-deref a) x y)))
           (-swap! [a f x y more]
             (reset! a (apply f (-deref a) x y more)))
           IPrintWithWriter
           (-pr-writer [a writer opts]
             (-write writer "#<Supatom: ")
             (pr-writer (-deref a) writer opts) ;; the current value
             (-write writer ">"))
           IHash
           (-hash [this] (goog/getUid this))))

(defn link->
  ([{:keys [atom-ref get-with set-with meta validate-with watches deref-with check-with id]}]
   (Supalink. atom-ref meta validate-with watches get-with set-with deref-with check-with id))
  ([atom-ref get-with] (link-> atom-ref get-with nil {}))
  ([atom-ref get-with set-with] (link-> atom-ref get-with set-with {}))
  ([atom-ref get-with set-with {:keys [meta validate-with watches deref-with check-with id]}]
   (Supalink. atom-ref meta validate-with watches get-with set-with deref-with check-with id)))

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

#?(:clj (defmethod print-method Supalink [^Supalink l ^Writer w]
          (.write w "#")
          (.write w (-> l class .getName))
          (.write w " {:type :link, :val ")
          (.write w (-> (.-atom_ref l) deref pr-str))
          (.write w "}")))

(defn supalink? [thing]
  (instance? Supalink thing))
