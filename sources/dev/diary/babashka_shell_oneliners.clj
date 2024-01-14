;; # `DIARY:` Can I do some quick scripting
;; ---
(ns diary.blank
   (:require [babashka.process :as !]))

(def cmd  
{;; ### task
:task ["tb" "-t"]
;; ### note
:note ["tb" "-n"]
;; ### board
:board ["tb" "-t"]
;; ### complete
:complete ["tb -c"]
;; ### start
:focus ["tb -b"]
;; ### mark
:mark ["tb -s"]
;; ### copy
:copy ["tb -y"]
;; ### view
:view ["tb"]
;; ### timeline
:timeline ["tb -i"]
;; ### prioritize
:prioritize ["tb -p @"]
;; ### move
:move ["tb -m"]
;; ### delete
:delete ["tb -d"]
;; ### archive
:kill ["tb --clear"]
;; ### history
:graveyard ["tb -a"]
;; ### restore
:restore ["tb -r"]
;; ### search
:search ["tb -f"]}
)
;; ---
;; ### DIARY:


(defn -main [& args]
  (let [cmd (first args)
        args (rest args)
        all (flatten (into (:cmd cmd) args))]
    (println (vec all))))
(!/sh "tb" "-t" "This is my test from diary.blank")

;; ---


(comment
  (j/cold-dev!)
  (j/dev-without-portal)
  (j/serve!)

                                        ;
  )
