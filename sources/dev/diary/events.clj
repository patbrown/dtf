;; # `DIARY:` Event System Take #1
;; ---
(ns diary.events
 {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [nextjournal.clerk :as clerk]
            [tools.eventing :as E]
            [clojure.core.async :as a]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY: Events System fast, easy, but not simple.
(require '[integrations.upstash.redis :as redis])
(require '[tools.eventing :as E])
(require '[tick.core :as t])
(require '[clojure.core.async :as a])
(defn empty-message-fn [msg] (println (:message/type msg)))
(redis/subscribe :message/id #'empty-message-fn)

#_(redis/message! {:message/id (t/now)
                   :message/type :message/event
                   :message/context {}
                   :message/body {:event/ident :attribute/add
                                  :event/notify [:your-mom]
                                  :event/t (t/now)
                                  :event/source :repl/pat
                                  :event/metadata {}
                                  :event/data {}}})

(def event-bus (a/chan))
(redis/subscribe-to-events event-bus)

;; ---
;; ### RICH COMMENTS:
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
