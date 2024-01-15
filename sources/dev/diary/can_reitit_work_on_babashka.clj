;; # `DIARY:` 

;; ---
(ns diary.can-reitit-work-on-babashka
  {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [nextjournal.clerk :as clerk]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY: Well mother fucker? Code something.
(require '[reitit.core :as r])

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
