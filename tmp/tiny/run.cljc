(ns tiny.run
  (:gen-class)
  (:require [tiny.server :refer [start!]]
            [tiny.app.v1 :as SELECTED]))

(defn -main [& args]
  (start! SELECTED/manifest))



(comment (def d (start! ctx))
         d
         ((-> d :server :kill!))
         (tap> d)
;
         )
