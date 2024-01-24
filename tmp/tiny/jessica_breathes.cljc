(ns tiny.jessica-breathes
  (:require [tiny.jessica :as jes]
            [tiny.ix :as ix]))



(def life [])

(ix/execute tiny.jessica/manifest chains/manifest :life)
