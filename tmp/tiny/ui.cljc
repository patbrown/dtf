(ns tiny.ui
  (:require [clojure.spec.alpha :as s]
            [tiny.jessica.specs :as spec]))

(s/fdef inject
  :args (s/cat :ctx :ui.component/context)
  :ret :ui.component/context)

(defn inject-dt-traits [ctx]
  (let [self (get-in ctx [:focus :self :dt/traits])]
    ))



(inject {:action {:inject :ui/toggle?}
         :focus {:self {:dt/id :dt/boolean
                        :instance/traits 
                        :dt/traits #{:ui/toggle?}}}})
