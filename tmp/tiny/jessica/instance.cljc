(ns tiny.jessica.instance)

(def attrs
  {:instance/id [:one :string]
   :instance/dt [:one :ref]
   :instance/traits [:many :ref]
   :instance/tags [:many :ref]})

(def dt
  {:dt/id :dt/instance
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :instance/id] [:attr/id :instance/dt]]
   :dt/opt [[:attr/id :instance/traits] [:attr/id :instance/tags]
            [:attr/id :attr/traits] [:attr/id :attr/tags]
            [:attr/id :dt/traits] [:attr/id :dt/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
