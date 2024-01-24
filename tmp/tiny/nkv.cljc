(ns tiny.nkv
  (:require [tiny.nkv.util :as u]
            [clojure.walk]
            [pyramid.core :as p]))

(declare rm!)

(defn *aggressive-rm
  "Takes a entity id vector of [dt id]"
  [{:keys [contents instance] :as ctx}]
  (let [a (atom [instance])
        ev (u/<-vectors contents instance)
        _ (when (or (u/entity-id? ev) (u/entity-ids? ev))
            (swap! a conj ev))
        for-removal (mapv vec (partition 2 (flatten @a)))]
    (map #(rm! (assoc ctx :instance %)) for-removal)))

(defn *rm
  [{:keys [contents instance] :as ctx}]
  (let [new-contents (if (or (keyword? instance)
                  (and (vector? instance) (= 1 (count instance))))
            (dissoc contents (first instance))
            (update-in contents (vec (butlast instance)) dissoc (last instance)))]
    (assoc ctx :contents new-contents)))

(defn rm!
  "Calls rm* or aggressive-rm* for one or multiple ks"
  [{:keys [instance] :as ctx}]
  (let [new-contents (if-not (u/vector-of-vectors? instance)
                       (*rm ctx)
                       (map #(*rm (assoc ctx :instance %)) instance))]
    (assoc ctx :contents new-contents)))

(defn *add-normalized [{:keys [contents instance] :as ctx}]
  (let [normalized (p/add {} instance)
        holder (atom contents)
        with-paths (keys (apply merge (keys
                                       (medley.core/map-kv
                                        (fn [k v] [(medley.core/map-keys
                                                    (fn [vk]
                                                      [[k vk] (get v vk)]) v)]) normalized))))
        new-contents (doall (map (fn [[k v]] (swap! holder assoc-in k v)) with-paths))]
    (assoc ctx :contents new-contents)))

(defn add!
  [{:keys [contents instance] :as ctx}]
  (let [new-contents (if (vector? instance)
                       (map #(add! contents %) instance)
                       (when (u/normalizable? instance)
                         (*add-normalized ctx)))]
    (assoc ctx :contents new-contents)))

(defn *get
  "Simple get-in to return normalized data located at the ks coords or hydrated with opts."
  [{:keys [contents path] :as ctx}]
  (let [path (if (keyword? path)
               [(u/ensure-id-kw path)]
               path)
        new-contents (get-in contents (u/follow-path contents path))]
    (assoc ctx :contents new-contents)))

(defn extract [{:keys [contents path] :as ctx}]
  (let [ks (if (keyword? path) [(u/ensure-id-kw path)] path)
        res (get-in contents (u/follow-path contents ks))
        extracted-contents (clojure.walk/postwalk #(cond
                                                     (u/entity-id? %) (get-in contents %)
                                                     (u/entity-ids? %) (map (get-in contents %)) :else %) res)]
    (assoc ctx :contents extracted-contents)))

(defn change!
  [{:keys [contents path function] :as ctx}]
  (assoc ctx :contents (update-in contents (u/ensured-ks contents path) function)))

(def actions
  {:add! add!
   :rm! rm!
   :change! change!
   :get *get
   :extract extract})

(defn- *access-layer [defaults]
  (fn [{:keys [action] :as ctx}]
    (let [f (get-in actions [action])]
      (f (medley.core/deep-merge defaults ctx)))))

(def ^:dynamic *access-point* nil)
(def access-layer (*access-layer *access-point*))

(defn ! [ctx]
  (access-layer ctx))

(comment
(access-layer {:action :list-lockers})
  (! [:list-lockers])
(! [[:list-lockers]
     [:list-lockers]])  
                                        ;
  )
