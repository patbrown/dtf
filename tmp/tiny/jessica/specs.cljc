(ns tiny.jessica.specs
  (:require [clojure.spec.alpha :as s]))

(def attrs
  {:spec/id [:one :keyword]
   :spec/form [:one :string]})

(def dt
  {:dt/id :dt/spec
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :spec/id] [:attr/id :spec/form]]
   :dt/opt [[:attr/id :dt/traits] [:attr/id :dt/tags]]})

(def manifest {::attrs attrs ::dt dt})

;; VT
(s/def :vt/qkw qualified-keyword?)
(s/def :vt/m map?)
(s/def :vt/keyword keyword?)
(s/def :vt/uuid uuid?)
(s/def :vt/boolean boolean?)
(s/def :vt/string string?)
(s/def :vt/one-ref #(and (vector? %) (every? qualified-keyword? %) (= 2 (count %))))
(s/def :vt/many-refs (fn [v] (and (vector? v) (every? #(s/valid? :one-ref %) v))))
(s/def :vt/function fn?)
(s/def :vt/card #{:one :many})
(s/def :vt/v-of-qkws #(and (vector? %) (every? qualified-keyword? %)))
(s/def :vt/s-of-qkws #(and (set? %) (every? qualified-keyword? %)))
(s/def :vt/v-of-vs #(and (vector? %) (every? vector? %)))
(s/def :vt/spec (s/or :vt/keyword qualified-keyword?
                      :vt/function fn?))
(s/def :vt/vt :vt/qkw)
(s/def :vt/traits :vt/s-of-qkws)
(s/def :vt/m-of-strs #(and (map? %) (every? string? (keys %)) (every? string? (vals %))))
(s/def :vt/tags :vt/m-of-strs)

;; ATTR
(s/def :attr/id :vt/qkw)
(s/def :attr/card :vt/card)
(s/def :attr/vt :vt/vt)
(s/def :attr/spec :vt/spec)
(s/def :attr/traits :vt/traits)
(s/def :attr/tags :vt/tags)
(s/def :dt/id :vt/qkw)
(s/def :dt/parent :vt/one-ref)
(s/def :dt/req :vt/many-refs)
(s/def :dt/opt :vt/many-refs)
(s/def :dt/spec :vt/spec)
(s/def :dt/traits :vt/traits)
(s/def :dt/tags :vt/tags)
(s/def :spec/id :vt/qkw)
(s/def :spec/body :vt/string)
(s/def :trait/label :vt/qkw)
(s/def :trait/ln :vt/one-ref)
(s/def :trait/id :vt/uuid)
(s/def :trait/active? :vt/boolean)
(s/def :tag/label :vt/string)
(s/def :tag/id :vt/uuid)
(s/def :tag/value :vt/string)
(s/def :ui.component/context :vt/m)

(def specs (set (keys (s/registry))))
(def specs-map
  (apply merge (map (fn [s] {s (s/form s)}) specs)))

