;; # `DIARY:` Streams
;; ---
(ns diary.streams
 {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [carmine-streams.core]
            [nextjournal.clerk :as clerk]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY: redis streams for event stream

(require '[taoensso.carmine :as car])
(require '[integrations.upstash.redis :refer [config with-redis]])

^{:nextjournal.clerk/visibility {:result :show}}
(def stream (cs/stream-name "sensor-readings"))
^{:nextjournal.clerk/visibility {:result :show}}
(def group (cs/group-name "persist-readings"))
^{:nextjournal.clerk/visibility {:result :show}}
(def consumer (cs/consumer-name "persist-readings" 0))


(with-redis (cs/xadd-map stream "*" {:event/ident :event/another}))
(let [[[_stream messages]] (with-redis (car/xread :count 1 :streams stream "0"))]
  (map (fn [[_id kvs]] (cs/kvs->map kvs))
       messages))



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
