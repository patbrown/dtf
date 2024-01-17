(ns tiny.ui.components.pages)

(defn simplest [{{:keys [title] :as self} :self :as ctx}]
  nil)

(def manifest {:pages/simplest simplest})
