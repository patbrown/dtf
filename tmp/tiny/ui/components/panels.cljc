(ns tiny.ui.components.panels)

(defn simplest [{{:keys [title] :as self} :self :as ctx}]
  nil)

(def manifest {:panels/simplest simplest})
