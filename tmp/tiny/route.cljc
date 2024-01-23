(ns tiny.route
  (:require [clojure.string]
            [medley.core :refer [deep-merge]]))

(defn path+uri->path-params
  [path uri]
  (cond (= "/" path)
        {}
        (re-find #"\*" path)
        (let [index-of-k-start (clojure.string/index-of path ":")
              k (-> (subs path (+ 1 index-of-k-start))
                    drop-last
                    clojure.string/join
                    keyword)
              v (subs uri index-of-k-start)]
          {k v})
        :else
        (let [split-path (->> (clojure.string/split path #"/")
                              (remove empty?)
                              vec)
              split-uri (->> (clojure.string/split uri #"/")
                             (remove empty?)
                             vec)]
          (into {} (map-indexed
                    (fn [idx item]
                      (cond
                         ; required parameter
                        (and (clojure.string/starts-with? item ":")
                             (not (clojure.string/ends-with? item "?")))
                        {(keyword (subs item 1)) (get split-uri idx)}
                         ; optional parameter
                        (and (clojure.string/starts-with? item ":")
                             (clojure.string/ends-with? item "?")
                             (get split-uri idx))
                        {(keyword (-> item
                                      (subs 0 (- (count item) 1))
                                      (subs 1)))
                         (get split-uri idx)}))
                    split-path)))))

(defn path->regex-path
  [path]
  (cond (= "/" path) "\\/"
        (re-find #"\*" path) (-> (clojure.string/replace path #"\:.*?\*" ".*?")
                                 (clojure.string/replace #"/" "\\/"))
        :else
        (->> (clojure.string/split path #"/")
             (map #(cond
                     (and (clojure.string/starts-with? % ":")
                          (not (clojure.string/ends-with? % "?")))
                     ".*"                     
                     (and (clojure.string/starts-with? % ":")
                          (clojure.string/ends-with? % "?"))
                     "?.*?"
                     :else
                     %))
             (clojure.string/join "\\/"))))

(defn match-route
  [routes uri request-method]
  (let [route (->> routes
                   (filter #(not (= :not-found (:path %))))
                   (map #(merge % {:regex-path (path->regex-path (:path %))}))
                   (filter #(and (re-matches (re-pattern (:regex-path %)) uri)
                                 (= (:method %) request-method)))
                   first)]
    (when route
      (dissoc route :regex-path))))

(defn route-view [{:keys [ctx request] :as state}]
  (let [{:keys [uri request-method]} request
        {:keys [routes]} ctx
        {:keys [action workflow path] :as match} (match-route routes uri request-method)
        params (path+uri->path-params path uri)]
    {:uri uri
     :method request-method
     :match match
     :path path
     :action action
     :workflow workflow
     :params params}))

(def focus-on-route-interceptor
  {:name :focus-on-route
   :enter (fn [state]
            (assoc-in state [:focus :route] (route-view state)))})

(def routes [{:path "/"
              :method :get
              :action (fn [req] {:status 200
                                 :body "Hi there!"})}
             {:path "/hello/:who"
              :method :get
              :action (fn [state]
                        {:status 200
                         :body (str "Hello, " (-> state
                                                  :focus
                                                  :route
                                                  :params
                                                  :who))})}
             {:path "/workflow/:who"
              :method :get
              :workflow [{:name :dude
                          :enter (fn [state]
                                   (assoc state :response {:status 200
                                                           :body (str "Hello, " (-> state
                                                                                    :focus
                                                                                    :route
                                                                                    :params
                                                                                    :who))}))}]}])
