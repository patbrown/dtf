(ns tiny.jessica.dts)

(def attrs
  {:dt/id [:one :keyword]
   :dt/parent [:one :ref]
   :dt/req [:many :ref]
   :dt/opt [:many :ref]
   :dt/spec [:one :keyword]
   :dt/traits [:many :keyword]
   :dt/tags [:many :ref]})

(def dt
  {:dt/id :dt/dt
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :dt/id] [:attr/id :dt/parent] [:attr/id :dt/req]]
   :dt/opt [[:attr/id :dt/opt] [:attr/id :dt/spec] [:attr/id :dt/traits] [:attr/id :dt/tags]
            [:attr/id :attr/traits] [:attr/id :attr/tags]
            [:attr/id :instance/traits] [:attr/id :instance/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
