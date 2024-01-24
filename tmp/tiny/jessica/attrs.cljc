(ns tiny.jessica.attrs)

(def attrs
  {:attr/id [:one :keyword]
   :attr/card [:one :keyword]
   :attr/spec [:one :ref]
   :attr/traits [:many :ref]
   :attr/tags [:many :ref]})

(def dt
  {:dt/id :dt/attr
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :attr/id] [:attr/id :attr/card] [:attr/id :attr/spec]]
   :dt/opt [[:attr/id :attr/traits] [:attr/id :attr/tags]
            [:attr/id :dt/traits] [:attr/id :dt/tags]
            [:attr/id :instance/traits] [:attr/id :instance/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
