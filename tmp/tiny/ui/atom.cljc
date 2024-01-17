(ns tiny.ui.atom
  (:require [tiny.ui :as ui]))

;; :component/variant Dispatch
(defmethod ui/create :component/atom [ctx]
  (assoc-in ctx [:focus :focus?] true))
(defmethod ui/focus :component/atom [ctx]
  (assoc-in ctx [:focus :focus?] true))
(defmethod ui/self :component/atom [ctx]
  (assoc-in ctx [:self :smoke?] true))
(defmethod ui/i-require :component/atom [ctx]
  (assoc-in ctx [:self  :component?] true))
(defmethod ui/i-provide :component/atom [ctx]
  (assoc-in ctx [:self  :component?] true))
(defmethod ui/load :component/atom [ctx]
  (assoc-in ctx [:focus  :load?] true))
(defmethod ui/unload :component/atom [ctx]
  (assoc-in ctx [:focus  :unload?] true))



