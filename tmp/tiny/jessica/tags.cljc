(ns tiny.jessica.tags)

(def attrs
  {:tag/id [:one :string],
   :tag/value [:one :string]})

(def dt
  {:dt/id :dt/tag
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :tag/id]]
   :dt/opt [[:attr/id :tag/value] [:attr/id :attr/vt] [:attr/id :attr/traits] [:attr/id :attr/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
