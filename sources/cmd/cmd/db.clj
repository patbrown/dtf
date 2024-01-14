(ns cmd.db
  (:require [clojure.string]
            [clojure.edn]
            [babashka.fs]
            [babashka.pods :as pods]))
(pods/load-pod 'org.babashka/buddy "0.3.4")
(require '[pod.babashka.buddy.core.codecs :as codecs])
(require '[pod.babashka.buddy.sign.jwe :as jwe])

(def db "resources/.config/store")
(defn salt-with [] (clojure.string/join (take 32 (System/getenv "SALT"))))

(defn add! [k v]
  (let [m (clojure.edn/read-string (slurp db))]
    (spit db (assoc m k v))))
(defn rm! [k]
  (let [m (clojure.edn/read-string (slurp db))]
    (spit db (dissoc m k))))
(defn view [k]
  (println (str (get (clojure.edn/read-string (slurp db)) k))))
