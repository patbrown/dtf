(ns tiny.focus
  (:require [where.core :refer [where]]
            [medley.core]
            [clojure.set]))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn rand-str-vec [len] (vec (take len (repeatedly #(rand-str (rand-nth (range 2 20)))))))

(defn rm-kw-ns
  [thing]
  (cond
    (keyword? thing) (keyword (name thing))
    (map? thing) (medley.core/map-keys (comp keyword name) thing)
    (or (set? thing)
        (vector? thing)) (map rm-kw-ns thing)
    :else (println "fail")))

(defn add-ns-to-keys
  "Adds the namespace of your desire to the keys in a map"
  [new-namespace m]  
  (medley.core/map-keys (comp keyword #(clojure.string/join "/" [(name new-namespace) %]) name) m))

(defn replace-key-ns
  "Replaces the namespaces on a map's keys with ones you provide."
  [new-namespace m]
  (->> m rm-kw-ns (add-ns-to-keys new-namespace)))

(defn replace-val-ns
  [new-namespace m]
  (->> m clojure.set/map-invert (replace-key-ns new-namespace) clojure.set/map-invert))

(defn merge-with-existing-key
  [m k m-to-merge]
  (assoc m k (merge (k m)
                      m-to-merge)))
(defn conj-in-key
  [m k v]
  (update-in m k conj v))

(defn dissoc-in
  [m ks]
  (let [path-to-dissoc (vec (butlast ks))
         key-to-dissoc (last ks)]
     (update-in m path-to-dissoc dissoc key-to-dissoc)))

(defn filter-nil-and-empty-vals
  [m]
  (medley.core/filter-vals #(not (or (when (coll? %) (empty? %)) (nil? %))) m))


(defn map-vals-as-set [val-coordinates m]
  (medley.core/map-vals #(update-in % val-coordinates set) m))

(defn has-all-keys?
  [m ks]
  (let [ks (if (keyword? ks) [ks] ks)
        cnt (count ks)]
    (= cnt (count (select-keys m ks)))))

(defn m->pairs
  [m]
  (vec (flatten (into '[] m))))

(defn filter-keys-by-ns
  [m ns-k-or-ns-str]
  (if (string? m)
    (medley.core/filter-keys #(= ns-k-or-ns-str (namespace %)) m)
    (medley.core/filter-keys #(= ns-k-or-ns-str (keyword (namespace %))) m)))

(defn only-keys-in-ns [nss m] (medley.core/filter-keys #(= (name nss) (namespace %)) m))

(defn remove-keys-with-nil-vals [m] (medley.core/filter-vals #(not= nil %) m))

(defn remove-nil-and-empty-vals
  [m]
  (medley.core/filter-vals #(not (or (when (coll? %) (empty? %)) (nil? %))) m))

(defn filter-where [where-clause seq]
  (->> seq (filter (where where-clause))))

(defn filter-keys-where [where-clause seq]
  (->> seq (medley.core/filter-keys (where where-clause))))
(defn filter-keys-where-name-is [? seq]
  (filter-keys-where [#(name %) = ?] seq))

(defn filter-vals-where [where-clause seq]
  (->> seq (medley.core/filter-vals (where where-clause))))

(defn filter-by-dt [dt seq]
  (->> seq (filter-where [:dt = dt])))

(defn remove-where [where-clause seq]
  (->> seq (remove (where where-clause))))

(defn remove-keys-where [where-clause seq]
  (->> seq (medley.core/remove-keys (where where-clause))))

(defn remove-vals-where [where-clause seq]
  (->> seq (medley.core/remove-vals (where where-clause))))

(defn remove-nil-keys [k seq]
  (remove-where [k = nil] seq))

(defn remove-keys [ks seq]
  (->> seq (map (fn [m]
                  (let [mks (set (keys m))
                        selection  (clojure.set/difference mks ks)]
                    (select-keys m selection))))))

(defn extract-key [k seq]
  (->> seq (map k)))

(defn extract-in [ks seq]
  (->> seq (map #(get-in % ks))))

(defn extract-key-to-set [k seq]
  (->> (extract-key k seq) flatten (into #{})))

(defn extract-selection [ks seq]
  (->> seq (map #(select-keys % ks))))

(defn change-key-vals-to-set [kset seq]
  (->> seq
       (map #(medley.core/map-kv
               (fn [k v] (if (contains? kset k)
                           [k (into #{} v)]
                           [k v])) %))))

(defn rename-all [ks seq]
  (->> seq (map #(clojure.set/rename-keys % ks))))

(defn as-one-map [seq]
  (apply merge seq))

(defn remove-nils [seq]
  (->> seq (remove nil?)))

(defn extract-selection-as-map [map-key ks seq]
  (->> seq (map (fn [m] {(map-key m) (select-keys m ks)}))))

(defn as-map-with-key [map-key seq]
  (->> seq (map (fn [m] {(map-key m) m}))))

(defn group-as-map-keys [k seq]
  (->> seq
       (group-by k)
       (medley.core/map-vals (fn [v] (map (fn [m] (hash-map (:ident m) m)) v)))
       (medley.core/map-vals #(apply merge %))))

(defn group-counts [k seq]
  (->> (group-as-map-keys k seq)
       (medley.core/map-vals (fn [v] (count v)))))

(defn nss-with-meta-key? [k]
          (let [ns-metas (->> (all-ns) (map (fn [n] {n (meta n)})) (apply merge))
                ns-with-key (keys (medley.core/filter-vals (fn [m] (contains? (set (keys m)) k)) ns-metas))]
            (map (comp symbol str) ns-with-key)))



(defn gm [] (->> (all-ns) (mapcat ns-interns) (keep #(-> % second meta))))
(defn meta-grab
  "Multi-arrity fn taking all meta and threading it through up to 4 filters to narrow results."
  ([] (gm))
  ([k]
   (->> (gm) (filter k)))
  ([k1 k2]
   (->> (gm) (filter k1) (filter k2)))
  ([k1 k2 k3]
   (->> (gm) (filter k1) (filter k2) (filter k3)))
  ([k1 k2 k3 k4]
   (->> (gm) (filter k1) (filter k2) (filter k3) (filter k4))))

