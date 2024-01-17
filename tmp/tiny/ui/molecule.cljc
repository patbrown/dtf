(ns tiny.ui.molecule
  (:require [tiny.ui :as ui]))

(defmethod ui/create :component/molecule [m]
  (assoc m :component.molecule/smoke? true))
