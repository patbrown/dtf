(ns cmd
  (:require ["ink" :refer [render Text Box Newline Spacer Static Transform]]
            [reagent.core :as r]))

(defonce state (r/atom 0))

(def count
  (js/setInterval
   #(if (< @state 10)
      (swap! state inc)
      (js/clearInterval count))
   500))

(defn hello []
  [:> Box {:width "100%" :backgroundColor "blue"}
   [:> Box {:width "50%"
            :padding 1}
    [:> Text {:backgroundColor "blue"
              :color           "green"
              
              } "Hello, world! " @state]]
   [:> Box {:width "50%"
            :padding 1
            :borderStyle {:topLeft     "↘"
                          :top         "↓"
                          :topRight    "↙"
                          :left        "→"
                          :bottomLeft  "↗"
                          :bottom      "↑"
                          :bottomRight "↖"
                          :right       "←"}}
    [:> Text {:inverse true
              :color           "green"} "Hello, Mom! " (str (* 10 @state))]]])

(render (r/as-element [hello]))
