(ns tiny.jessica.traits)

(def attrs
  {:trait/id [:one :uuid],
   :trait/label [:one :keyword],
   :trait/ln [:one :ref],
   :trait/active? [:one :boolean],
   :trait/variant [:one :keyword],})

(def dt
  {:dt/id :dt/trait
   :dt/parent [:dt/id :dt/dt]
   :dt/req [[:attr/id :trait/id] [:attr/id :trait/label] [:attr/id :trait/active?]]
   :dt/opt [[:attr/id :dt/opt] [:attr/id :dt/spec] [:attr/id :dt/traits] [:attr/id :dt/tags]]})

(def manifest
  {::attrs attrs
   ::dt dt})
