(ns tiny.nkv.util
  (:require [clojure.walk]
            [tiny.inflections :refer [singular]]))

(defn <-vectors [db ent-id]
  (first (filter vector? (vals (get-in db ent-id)))))

(defn entity-id? [thing]
  (and (vector? thing)
       (= 2 (count thing))
       (keyword? (first thing))
       (= "id" (name (first thing)))))

(defn entity-ids? [thing]
  (and (vector? thing)
       (every? entity-id? thing)))

(defn vector-of-vectors? [v]
  (and (vector? v) (every? vector? v)))

(defn normalizable? [thing]
  (let [x (atom [])]
    (clojure.walk/postwalk #(when (and (keyword? %)
                                       (= "id" (name %)))
                              (swap! x conj :true)) thing)
    (when-not (empty? @x) true)))

(defn ensure-id-kw [thing]
  (cond
    (and (qualified-keyword? thing)
           (= "id" (name thing))) thing
    (vector? thing) (update thing 0 #(keyword (singular (name %)) "id"))
    :else (keyword (singular (name thing))  "id")))

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
