;; # `DIARY:` 

;; ---
(ns diary.blank
  {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [nextjournal.clerk :as clerk]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY:

;; ---
^{:nextjournal.clerk/visibility {:code :hide}}
(j/show! #'self)
^{:nextjournal.clerk/visibility {:code :hide}}
(j/instrument)
(comment
  (j/cold-dev!)
  (j/dev-without-portal)
  (j/serve!)

;
  )
