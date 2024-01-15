(ns patbrown.supalog
  #?(:bb (:import [java.io Writer])
     :clj (:import [clojure.lang IAtom IDeref IRef IReference Atom]
                   [java.io Writer])))

(defprotocol AppendOnlyLog
  (commit! [this data])
  (playback [this][this offset])
  (head [this])
  (empty! [this]))

#?(:bb (defrecord Supalog [atom-ref]
          clojure.lang.IAtom
          (swap [_ f]
            (swap! atom-ref f))
          (swap [_ f a]
            (swap! atom-ref f a))
          (swap [_ f a b]
            (swap! atom-ref f a b))
          (swap [_ f a b c]
            (apply swap! atom-ref f a b c))
          (compareAndSet [_ old-value new-value]
            (compare-and-set! atom-ref old-value new-value))
          (reset [_ new-value]
            (reset! atom-ref new-value))
          clojure.lang.IDeref
          (deref [_]
            (deref atom-ref))
          AppendOnlyLog
          (commit! [_ data]
            (swap! atom-ref conj data))
          (playback [this]
            (reverse (deref atom-ref)))
          (playback [this offset]
            (if (pos-int? offset)
              (take offset (reverse (deref atom-ref)))
              (reverse (take offset (reverse (deref atom-ref))))))
          (head [this]
            (last (deref atom-ref)))
          (empty! [_]
            (reset! atom-ref {})))

   :clj (defrecord Supalog [atom-ref]
          IAtom
          (swap [_ f]
            (swap! atom-ref f))
          (swap [_ f a]
            (swap! atom-ref f a))
          (swap [_ f a b]
            (swap! atom-ref f a b))
          (swap [_ f a b c]
            (apply swap! atom-ref f a b c))
          (compareAndSet [_ old-value new-value]
            (compare-and-set! atom-ref old-value new-value))
          (reset [_ new-value]
            (reset! atom-ref new-value))
          IRef
          (setValidator [_ validator]
            (.setValidator atom-ref validator))
          (getValidator [_]
            (.getValidator atom-ref))
          (addWatch [this watch-key watch-fn]
            (.addWatch atom-ref watch-key watch-fn)
            this)
          (removeWatch [this watch-key]
            (.removeWatch atom-ref watch-key)
            this)
          (getWatches [_]
            (.getWatches atom-ref))
          IDeref
          (deref [_]
            (deref atom-ref))
          IReference
          (resetMeta [_ meta-map]
            (reset-meta! atom-ref meta-map))
          (alterMeta [_ f args]
            (alter-meta! atom-ref f args))
          AppendOnlyLog
          (commit! [_ data]
            (swap! atom-ref conj data))
          (playback [this]
            (reverse (deref atom-ref)))
          (playback [this offset]
            (if (pos-int? offset)
              (take offset (reverse (deref atom-ref)))
              (reverse (take offset (reverse (deref atom-ref))))))
          (head [this]
            (last (deref atom-ref)))
          (empty! [_]
            (reset! atom-ref {})))
   :cljs
   (defrecord Supalog [atom-ref]
     IAtom
     ISwap
     (-swap! [_ f]
       (swap! atom-ref f))
     (-swap! [_ f a]
       (swap! atom-ref f a))
     (-swap! [_ f a b]
       (swap! atom-ref f a b))
     (-swap! [_ f a b c]
       (apply swap! atom-ref f a b c))
     IReset
     (-reset! [_ new-value]
       (reset! atom-ref new-value))
     IWatchable
     (-add-watch [this watch-key watch-fn]
       (add-watch atom-ref watch-key watch-fn)
       this)
     (-remove-watch [this watch-key]
       (remove-watch atom-ref watch-key)
       this)
     IDeref
     (-deref [_]
       (deref atom-ref))
     AppendOnlyLog
     (commit! [_ data]
       (swap! atom-ref conj data))
     (playback [this]
       (reverse (deref atom-ref)))
     (playback [this offset]
       (if (pos-int? offset)
         (take offset (reverse (deref atom-ref)))
         (reverse (take offset (reverse (deref atom-ref))))))
     (head [this]
       (last (deref atom-ref)))
     (empty! [_]
       (reset! atom-ref {}))))

(defn log->
  ([a]
   (if (map? a)
     (let [{:keys [atom-ref]} a]
       (Supalog. atom-ref))
     (Supalog. a))))

#?(:clj (defmethod print-method Supalog [^Supalog supalog ^Writer w]
          (.write w "#")
          (.write w (-> supalog class .getName))
          (.write w " {:type :log, :val ")
          (.write w (-> (.-atom_ref supalog) deref pr-str))
          (.write w "}")))

(defn supalog? [thing]
  (instance? Supalog thing))
