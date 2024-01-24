(ns tiny.ui.components.molecules)

(comment
;; This is how the destructuring should work typically
  (def t {:self {:title "this is life"}
                 :the-rest [:happened]}))

(defn action-button [{{:keys [title] :as self} :self :as ctx}]
  {:title title :self self :ctx ctx})

(def manifest {:molecule/action-button action-button})
