(ns tiny.jessica.interceptors)

(def attrs
  {:interceptor/id [:one :keyword]
   :interceptor/requires [:many :vt/v-of-qkws]
   :interceptor/provides [:many :vt/v-of-qkws]
   :interceptor/enter [:one :string]
   :interceptor/exit [:one :string]
   :interceptor/error [:one :string]})

(def dt
  {:dt/id :dt/interceptor
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :interceptor/id]]
   :dt/opt [[:attr/id :interceptor/requires] [:attr/id :interceptor/provides]
            [:attr/id :interceptor/enter] [:attr/id :interceptor/leave] [:attr/id :interceptor/error]
            [:attr/id :dt/traits] [:attr/id :dt/tags]
            [:attr/id :attr/traits] [:attr/id :attr/tags]
            [:attr/id :instance/traits] [:attr/id :instance/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
