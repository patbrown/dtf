(ns patbrown.supareg
  (:require [clojure.walk]
            [medley.core]
            [pyramid.core]
            [clojure.string]
            [patbrown.inflections :refer [singular plural]])
  #?(:bb (:import [java.io Writer])
     :clj (:import [clojure.lang IAtom IDeref IRef IReference Atom]
                   [java.io Writer])))

(defn <-vectors [db ent-id]
  (first (filter vector? (vals (get-in db ent-id)))))
(defn vector-of-vectors? [v]
  (and (vector? v) (every? vector? v)))

(defn entity-id? [thing]
  (and (vector? thing)
       (= 2 (count thing))
       (keyword? (first thing))
       (= "id" (name (first thing)))))

(defn entity-ids? [thing]
  (and (vector? thing)
       (every? entity-id? thing)))

(defn ensure-id-kw [thing]
  (cond
    (and (qualified-keyword? thing)
           (= "id" (name thing))) thing
    (vector? thing) (update thing 0 #(keyword (singular (name %)) "id"))
    :else (keyword (singular (name thing))  "id")))

(defn normalizable? [thing]
  (let [x (atom [])]
    (clojure.walk/postwalk #(when (and (keyword? %)
                                       (= "id" (name %)))
                              (swap! x conj :true)) thing)
    (if-not (empty? @x) true false)))


(defn follow-path
  "Follows normalized entities to return the true value coords of path. Ripped from f.a.n-s"
  [db path]
  (loop [[h & t] path
          new-path []]
     (if h
       (let [np (conj new-path h)
             c  (get-in db np)]
         (if (entity-id? c)
           (recur t c)
           (recur t (conj new-path h))))
       (if (not= path new-path)
         new-path
         path))))

(defn ensured-ks [db ks]
  (let [ks (if (keyword? ks) [(ensure-id-kw ks)] ks)]
    (follow-path db ks)))

(defn as-normalized-paths [data]
  (let [normalized (pyramid.core/add {} data)
        with-paths (keys (apply merge (keys
                                       (medley.core/map-kv
                                        (fn [k v] [(medley.core/map-keys
                                                    (fn [vk]
                                                      [[k vk] (get v vk)]) v)]) normalized))))]
    with-paths))

(defn add-normalized! [atom-ref data]
  (doall (map (fn [[k v]] (swap! atom-ref assoc-in k v)) (as-normalized-paths data))))

(defn add-normalized [m data]
  (first (doall (map (fn [[k v]] (assoc-in m k v)) (as-normalized-paths data)))))

(defn atom? [thing]
  #?(:clj (instance? clojure.lang.Atom thing)
     :cljs (instance? cljs.core.Atom thing)))

(defn add! [atom-ref data]
  (if (vector? data)
    (map #(add! atom-ref %) data)
    (if (atom? atom-ref)
      (if (normalizable? data)
        (add-normalized! atom-ref data)
        (swap! atom-ref merge data))
      (if (normalizable? data)
        (add-normalized atom-ref data)
        (merge atom-ref data)))))

(declare rm!)
(defn aggressive-rm!
  "Removes an entity from the db and all it's referred component entities."
  [atom-ref ent-id]
  (let [a (atom [ent-id])
        ev (<-vectors (if (atom? atom-ref)
                        (get-in @atom-ref [])
                        (get-in atom-ref [])) ent-id)
        _ (when (or (entity-id? ev) (entity-ids? ev))
            (swap! a conj ev))
        for-removal (mapv vec (partition 2 (flatten @a)))]
    (map #(rm! atom-ref %) for-removal)))

(defn rm!
  ([atom-ref ks]
   (if (or (keyword? ks)
           (and (vector? ks) (= 1 (count ks))))
     (swap! atom-ref dissoc (first ks))
     (swap! atom-ref update-in (vec (butlast ks)) dissoc (last ks))))
  ([atom-ref ks opt]
   (when (entity-id? ks) (aggressive-rm! atom-ref ks))))

(defn *rm!
  ([atom-ref ks]
   (if (or (keyword? ks)
           (and (vector? ks) (= 1 (count ks))))
     (dissoc atom-ref (first ks))
     (update-in atom-ref (vec (butlast ks)) dissoc (last ks))))
  ([atom-ref ks opt]
   (when (entity-id? ks) (aggressive-rm! atom-ref ks))))

(defn change!
    ([atom-ref ks f]
     (swap! atom-ref update-in (ensured-ks (get-in @atom-ref []) ks) f))
    ([atom-ref ks f a]
     (swap! atom-ref update-in (ensured-ks (get-in @atom-ref []) ks) f a))
    ([atom-ref ks f a b]
     (swap! atom-ref update-in (ensured-ks (get-in @atom-ref []) ks) f a b))
    ([atom-ref ks f a b c]
     (swap! atom-ref update-in (ensured-ks (get-in @atom-ref []) ks) f a b c))
    ([atom-ref ks f a b c d]
     (swap! atom-ref update-in (ensured-ks (get-in @atom-ref []) ks) f a b c d)))

(defn *change!
    ([atom-ref ks f]
     (update-in atom-ref (ensured-ks (get-in atom-ref []) ks) f))
    ([atom-ref ks f a]
     (update-in atom-ref (ensured-ks (get-in atom-ref []) ks) f a))
    ([atom-ref ks f a b]
     (update-in atom-ref (ensured-ks (get-in atom-ref []) ks) f a b))
    ([atom-ref ks f a b c]
     (update-in atom-ref (ensured-ks (get-in atom-ref []) ks) f a b c))
    ([atom-ref ks f a b c d]
     (update-in atom-ref (ensured-ks (get-in atom-ref []) ks) f a b c d)))

(defn <-
  [atom-ref ks]
  (let [ks (if (keyword? ks) [(ensure-id-kw ks)] ks)]
      (get-in @atom-ref (follow-path (get-in @atom-ref []) ks))))

(defn <<-
  [atom-ref ks]
  (let [ks (if (keyword? ks) [(ensure-id-kw ks)] ks)
          res (get-in @atom-ref (follow-path (get-in @atom-ref []) ks))]
      (clojure.walk/postwalk #(cond
                                (entity-id? %) (get-in @atom-ref %)
                                (entity-ids? %) (map (get-in @atom-ref %))
                                :else %) res)))

(defn <-vectors [db ent-id]
  (first (filter vector? (vals (get-in db ent-id)))))
(defn vector-of-vectors? [v]
  (and (vector? v) (every? vector? v)))

(defn entity-id? [thing]
  (and (vector? thing)
       (= 2 (count thing))
       (keyword? (first thing))
       (= "id" (name (first thing)))))

(defn entity-ids? [thing]
  (and (vector? thing)
       (every? entity-id? thing)))

(defn ensure-id-kw [thing]
  (cond
    (and (qualified-keyword? thing)
           (= "id" (name thing))) thing
    (vector? thing) (update thing 0 #(keyword (singular (name %)) "id"))
    :else (keyword (singular (name thing))  "id")))

(defprotocol RegAccess
  (-init! [this data])
  (-add! [this data])
  (-rm! [this ks]
    [this ks opts])
  (-change! [this ks f]
    [this ks f a]
    [this ks f a b]
    [this ks f a b c]
    [this ks f a b c d])
  (-<- [this ks])
  (-<<- [this ks]))


#?(:bb (defrecord Reg [atom-ref]
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
          RegAccess
          (-init! [this data]
            (when (nil? @atom-ref)
              (reset! atom-ref data)))
          (-add! [this data]
            (add! atom-ref data))
          (-rm! [this ks]
            (if-not (vector-of-vectors? ks)
              (rm! atom-ref ks)
              (map #(rm! atom-ref %) ks)))
          (-rm! [this ks opts]
            (if-not (vector-of-vectors? ks)
              (aggressive-rm! atom-ref ks)
              (map #(aggressive-rm! atom-ref %) ks)))
          (-change! [this ks f]
            (change! atom-ref ks f))
          (-change! [this ks f a]
            (change! atom-ref ks f a))
          (-change! [this ks f a b]
            (change! atom-ref ks f a b))
          (-change! [this ks f a b c]
            (change! atom-ref ks f a b c))
          (-change! [this ks f a b c d]
            (change! atom-ref ks f a b c d))
          (-<- [this ks]
            (<- atom-ref ks))
          (-<<- [this ks]
            (<<- atom-ref ks)))
   
   :clj (defrecord Reg [atom-ref]
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
          (resetMeta [_ meta-map]
            (reset-meta! atom-ref meta-map))
          (alterMeta [_ f args]
            (alter-meta! atom-ref f args))
          RegAccess
          (-init! [this data]
            (when (nil? @atom-ref)
              (reset! atom-ref data)))
          (-add! [this data]
            (add! atom-ref data))
          (-rm! [this ks]
            (if-not (vector-of-vectors? ks)
              (rm! atom-ref ks)
              (map #(rm! atom-ref %) ks)))
          (-rm! [this ks opts]
            (if-not (vector-of-vectors? ks)
              (aggressive-rm! atom-ref ks)
              (map #(aggressive-rm! atom-ref %) ks)))
          (-change! [this ks f]
            (change! atom-ref ks f))
          (-change! [this ks f a]
            (change! atom-ref ks f a))
          (-change! [this ks f a b]
            (change! atom-ref ks f a b))
          (-change! [this ks f a b c]
            (change! atom-ref ks f a b c))
          (-change! [this ks f a b c d]
            (change! atom-ref ks f a b c d))
          (-<- [this ks]
            (<- atom-ref ks))
          (-<<- [this ks]
            (<<- atom-ref ks)))

      :cljs (defrecord Reg [atom-ref]
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
          (-reset! [_ newval]
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
            (-deref atom-ref))
          RegAccess
          (init! [this data]
            (when (nil? @atom-ref)
              (reset! atom-ref data)))
          (add! [this data]
            (add! atom-ref data))
          (rm! [this ks]
            (if-not (vector-of-vectors? ks)
              (rm! atom-ref ks)
              (map #(rm! atom-ref %) ks)))
          (rm! [this ks opts]
            (if-not (vector-of-vectors? ks)
              (aggressive-rm! atom-ref ks)
              (map #(aggressive-rm! atom-ref %) ks)))
          (change! [this ks f]
            (change! atom-ref ks f))
          (change! [this ks f a]
            (change! atom-ref ks f a))
          (change! [this ks f a b]
            (change! atom-ref ks f a b))
          (change! [this ks f a b c]
            (change! atom-ref ks f a b c))
          (change! [this ks f a b c d]
            (change! atom-ref ks f a b c d))
          (<- [this ks]
            (<- atom-ref ks))
          (<<- [this ks]
            (<<- atom-ref ks)))
)

(defn reg-> [a]
  (if (map? a)
     (let [{:keys [atom-ref]} a]
       (reg-> atom-ref))
     (Reg. a)))

#?(:clj (defmethod print-method Reg [^Reg r ^Writer w]
          (.write w "#")
          (.write w (-> r class .getName))
          (.write w " {:status :ready, :val ")
          (.write w (-> (.-atom_ref r) deref pr-str))
          (.write w "}")))

(defn reg? [thing]
  (instance? Reg thing))
