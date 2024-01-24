(ns tiny.actions.fix-favicon)

(def fix-favicon
  {:enter (fn [{{uri :uri} :request
                :as        state}]
            (case uri
              "/favicon.ico" (assoc-in state [:request :uri] "/img/favicon.ico")
              state))})
