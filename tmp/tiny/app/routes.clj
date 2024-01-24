(ns tiny.app.routes)

(def home {:path "/"
           :method :get
           :action (fn [req] {:status 200
                              :body "Hi there!"})})

(def delete [{:path "/chain/:who"
              :method :get
              :chain [{:name :dude
                       :enter (fn [state]
                                (assoc state :response {:status 200
                                                        :body (str "Hello, " (-> state
                                                                                 :focus
                                                                                 :route
                                                                                 :params
                                                                                 :who))}))}]}
             {:path "/hello/:who"
               :method :get
               :action (fn [state]
                         {:status 200
                          :body (str "Hello, " (-> state
                                                   :focus
                                                   :route
                                                   :params
                                                   :who))})}])

(def routes (vec (flatten [home delete])))
