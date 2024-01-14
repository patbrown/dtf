;; # `NOTEBOOK:` The Almighty `J`
;; ---
(ns j
  {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:refer-clojure :exclude [get get-in select-keys assoc! assoc-in! update! update-in! apply])
  (:require [clojure.datafy]
            [clojure.spec.test.alpha :as st]
            #?(:clj [portal.api])
            #?(:clj [nextjournal.clerk :as clerk])
            #?(:cljs [portal.web])
            #?(:cljs [portal.shadow.remote :as psr])
            #?(:cljs [applied-science.js-interop :as jsi])
            #?(:cljs [cljs-bean.core :as bean])))

;; # PROJECT SPECIFIC


;; ## CLJ ONLY
;; ---
;; ### Utilities to add libraries to classpath while in REPL
;; This stuff can be very annoying and helpful, use it, but keep in mind it's a common source of inexplicable answers and restart the repl when it may be an issue. It's cheaper than searching for problems that aren't there.
;; ---

;; ### CLERK
;; Works on a var of *ns*, commonly put at the end of namespaces to show on compile.
(defn show! [thing]
  (:file (meta thing)))

;; Notice the loopback addy
(def default-clerk-config {
                           :host "0.0.0.0"
                           :port 5678
                           :watch-paths ["plugins/dev"]})


#?(:clj (defn serve!
          ([] (clerk/serve! default-clerk-config))
          ([m] (clerk/serve! (merge default-clerk-config m)))))
;; ---

;; ## CLJC code
;; *Datafy*
(def datafy clojure.datafy/datafy)
(def nav clojure.datafy/nav)
;; *Portal*
(def clear-portal #?(:clj portal.api/clear
                     :cljs portal.web/clear))
(def close-portal #?(:clj portal.api/close
                     :cljs portal.web/close))
(def open-portal #?(:clj portal.api/open
                    :cljs portal.web/open))
(def tap-portal #?(:clj portal.api/tap
                   :cljs portal.web/tap))
#?(:clj (def start-portal portal.api/start))
(def submit-to-portal #?(:clj portal.api/submit
                         :cljs portal.shadow.remote/submit))
(def submit-datafied-to-portal (comp submit-to-portal clojure.datafy/datafy))
(defn target-portal [] (add-tap #'submit-to-portal))
(defn untarget-portal [] (remove-tap #'submit-to-portal))
(defn <-portal [p-var] (prn (deref p-var)))
#?(:clj (def instrument st/instrument))
;; ---

;; ## CLJS
;; *JS Interop: These functions are always available as j/*
#?(:cljs (def get jsi/get))
#?(:cljs (def get-in jsi/get-in))
#?(:cljs (def select-keys jsi/select-keys))
#?(:cljs (def assoc! jsi/assoc!))
#?(:cljs (def assoc-in! jsi/assoc-in!))
#?(:cljs (def update! jsi/update!))
#?(:cljs (def update-in! jsi/update-in!))
#?(:cljs (def call jsi/call))
#?(:cljs (def apply jsi/apply))
#?(:cljs (def call-in jsi/call-in))
#?(:cljs (def apply-in jsi/apply-in))
#?(:cljs (def obj jsi/obj))
#?(:cljs (def bean bean/bean))
#?(:cljs (def bean? bean/bean?))
#?(:cljs (def ->clj bean/->clj))
#?(:cljs (def ->js bean/->js))

;; ## DEV
;; ### This is how I instantiate dev-mode.
;; Commonly found in a comment in every namespace.
(defn cold-dev!
  []
  (tap-portal)
  (def p (open-portal {:port 6789
                       :window-title "PORTAL"
                       :app false
                       :host "0.0.0.0"}))
  (add-tap #'submit-datafied-to-portal)
  #?(:clj (serve!)))

#?(:clj (defn dev-without-portal
          []
          (serve!)))
