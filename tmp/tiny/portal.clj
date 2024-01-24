(ns tiny.portal
  (:require [babashka.deps :as deps]))

(deps/add-deps '{:deps {djblue/portal {:mvn/version "0.43.0"}}})
(require '[portal.api :as p])
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. (fn [] (p/close))))
(def portal-config {:port 6789
                            :window-title "PORTAL"
                            :app false
                            :host "0.0.0.0"})
(defn start-portal [] (p/open portal-config))
(def submit-to-portal #?(:clj portal.api/submit
                         :cljs portal.shadow.remote/submit))
(def submit-datafied-to-portal (comp submit-to-portal clojure.datafy/datafy))
(defn target-portal [] (add-tap #'submit-to-portal))

