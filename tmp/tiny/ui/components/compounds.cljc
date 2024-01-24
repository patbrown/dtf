(ns tiny.ui.components.compounds)

(def sample {:component/variant :component/compound
             :compound/variant :compound/on-off
             :compound/molecule :molecule/radio-button
             :compound/data-attributes [[:attr/id :well/name]]
             :instance/dt [:dt/id :dt/compound]
             :instance/traits #{}
             :instance/tags {"a" "v"}})



(def manifest {:compound/on-off sample})
