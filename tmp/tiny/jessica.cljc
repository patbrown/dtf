(ns tiny.jessica
  (:require [tiny.ix :refer [execute]]
            [tiny.jessica.attrs :as attrs]
            [tiny.jessica.dts :as dts]
            [tiny.jessica.instance :as instance]
            [tiny.jessica.interceptors :as interceptor]
            [tiny.jessica.specs :as specs]
            [tiny.jessica.tags :as tags]
            [tiny.jessica.traits :as traits]
            [tiny.db :refer [link-> backup-> cursor->]]
            [tiny.focus :as focus]))

(def manifest (apply merge (flatten [
                                     attrs/manifest
                                     dts/manifest
                                     instance/manifest
                                     interceptor/manifest
                                     specs/manifest
                                     tags/manifest
                                     traits/manifest
                                     ])))


(comment

  (def manifest (apply merge (flatten [attrs/manifest dts/manifest instance/manifest specs/manifest tags/manifest traits/manifest])))
(def a {:name :a :enter (fn [ctx] (assoc ctx :AAA 999))})
(def ^:dynamic *manifest* (atom manifest))

(def normalize-specs
  {:name :normalize-specs
   :enter (fn [ctx]
            (let [specs (medley.core/map-kv (fn [k v]
                                              [k {:spec/id k
                                                  :spec/form v}]) specs/specs-map)]
              (assoc ctx :spec/id specs)))})

(def normalize-attrs
  {:name :normalize-attrs
   :enter (fn [ctx]
            (let [attrs-raw (focus/filter-keys-where-name-is "attrs" ctx)
                  attrs-map (apply merge (vals attrs-raw))
                  attrs-map-expanded
                  (medley.core/map-kv (fn [k v]
                                        [k {:attr/id k
                                            :attr/card (first v)
                                            :attr/vt (second v)}]) attrs-map)]
              (assoc ctx :attr/id attrs-map-expanded)))})

(def normalize-dts
  {:name :normalize-dts
   :enter (fn [ctx]
            (let [dts-raw (focus/filter-keys-where-name-is "dt" ctx)
                  dts-map (apply merge  (focus/as-map-with-key :dt/id (vals dts-raw)))]
              (assoc ctx :dt/id dts-map)))})

(def chains [{:chain/id :manifest-destiny/jessica
              :chain/links [normalize-specs
                            normalize-attrs
                            normalize-dts]}])



(defn Breathe! []
  (execute @*manifest* chains :manifest-destiny/jessica))

(Breathe!)



(defn get-attribute-spec [{:keys [attribute] :as ctx}]
  (let [sid (get-in ctx [:spec/id attribute])
        fallback (get-in ctx [:attr/id attribute :attr/vt])]))

(defn validate-attribute [{:keys [attribute instance] :as ctx}]
  (let [spec (get-attribute-spec ctx)])
  (s/valid? ))

(def actions {:validate-attribute validate-attribute})

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


(comment
  (def a (atom {:a {:b {:c 100}}}))
  (def hey (cursor-> a [:a :b]))
  (swap! hey update-in [:c] inc)
  @a
  (reset! path-a (inc @path-a))
  (swap! path-a inc)
  (def b (atom nil))
  (def c (backup-> a b))
  @c
  @a
  @b

;
  )



  )

