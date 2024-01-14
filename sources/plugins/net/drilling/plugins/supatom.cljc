(ns net.drilling.plugins.supatom
  #?(:bb (:require [net.drilling.plugins.supalink :refer [link-> cursor-> xform-> count-> head-> backup->]]
                   [net.drilling.plugins.supastore.fs :as fs]
                   [net.drilling.plugins.supastore.s3 :as s3]
                   [net.drilling.plugins.supastore.postgres :as postgres]
                   [net.drilling.plugins.supalog :refer [log->]]
                   [net.drilling.plugins.supareg :refer [reg->]])
     :clj (:require [net.drilling.plugins.supalink :refer [link-> cursor-> xform-> count-> head-> backup->]]
                    [net.drilling.plugins.supalog :refer [log->]]
                    [net.drilling.plugins.supareg :refer [reg->]]
                    [net.drilling.plugins.lockers.redis :as redis]
                    [net.drilling.plugins.lockers.s3 :as s3]
                    [net.drilling.plugins.lockers.postgres :as postgres]
                    [robertluo.fun-map :as fm])
     :cljs (:require [net.drilling.plugins.supalink :refer [link-> cursor-> xform-> count-> head-> backup->]]
                     [net.drilling.plugins.supalog :refer [log->]]
                     [net.drilling.plugins.supareg :refer [reg->]]
                     [net.drilling.plugins.lockers.local :as local]
                     [robertluo.fun-map :as fm])))

(def default-map
  #?(:bb {}
     :clj (fm/fun-map {})
     :cljs (fm/fun-map {})))

;; ### SUPATOM
(defn supatom [{:keys [atom-ref backing datastructure init] :as m}]
  (let [default-atom-ref #?(:bb (case backing
                                  :mem (atom (or init default-map))
                                  :fs (fs/locker m)
                                  :postgres (postgres/locker m)
                                  :s3 (s3/locker m)
                                  (atom default-map))
                            :clj (case backing
                                   :mem (atom default-map)
                                   :fs (fs/locker m)
                                   :postgres (postgres/locker m)
                                   :redis (redis/locker m)
                                   :s3 (s3/locker m)
                                   (atom default-map))
                            :cljs (case backing
                                    :local (local/local-locker m)
                                    :session (local/session-locker m)
                                    (atom default-map)))
        atom-ref (or atom-ref default-atom-ref)]
    (case datastructure
      :reg (reg-> (assoc m :atom-ref atom-ref))
      :log (let [_ (reset! atom-ref [])] (log-> (assoc m :atom-ref atom-ref)))
      :link (link-> (assoc m :atom-ref atom-ref))
      :cursor (cursor-> (assoc m :atom-ref atom-ref))
      :xform (xform-> (assoc m :atom-ref atom-ref))
      :count (count-> (assoc m :atom-ref atom-ref))
      :head (head-> (assoc m :atom-ref atom-ref))
      :backup (backup-> (assoc m :atom-ref atom-ref)))))

(comment
;; This is a place where things can be extended into real life. The thought is that the atoms exist outside of the fun map and the fun map that is deps is inside an atom. These atoms are then synced

  ;; - fun-map is deps/reg
  ;; - deps/reg is inside an atom
  ;; - realtime updates are assoc'ed into the smart map via supalinks

  ;; This is the best of all worlds. 
  ;; The side-effects exist outside the system!
  ;; Everything with the exception of the side effects is inside the fun-map
  ;; The fun-map is part of every request.
  )
