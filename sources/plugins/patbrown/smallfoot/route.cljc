(ns patbrown.smallfoot.route
  (:require [clojure.string]))

(defn deep-merge [& maps]
  (letfn [(reconcile-keys [val-in-result val-in-latter]
            (if (and (map? val-in-result)
                     (map? val-in-latter))
              (merge-with reconcile-keys val-in-result val-in-latter)
              val-in-latter))
          (reconcile-maps [result latter]
            (merge-with reconcile-keys result latter))]
    (reduce reconcile-maps maps)))

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

(defn route+req->response
  [{:keys [path response]} {:keys [uri] :as req}]
  (cond
    (map? response) response
    (fn? response) (response (-> {:params (path+uri->path-params path uri)}
                                 (deep-merge req)))
    :else
    {:status 404
     :body "Not found."}))

(defn route
  [routes {:keys [uri request-method] :as req}]
  (if-let [route (match-route routes uri request-method)]
    (route+req->response route req)
    (route+req->response (->> routes
                              (filter #(= :not-found (:path %)))
                              first) req)))
