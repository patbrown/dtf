;; # `DIARY:` 

;; ---
(ns diary.blank
  {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [nextjournal.clerk :as clerk]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY:

(defn -concat
  "Concatenate routes interceptors with the defaults ones,
  or override it if its type isn't a map."
  [{except-interceptors :except
    around-interceptors :around
    inside-interceptors :inside
    :as                 interceptors}
   default-interceptors]
  (if (map? interceptors)
    ;; get around/inside interceptors
    (remove (set except-interceptors)
            (concat around-interceptors
                    default-interceptors
                    inside-interceptors))
    ;; else override
    (or interceptors default-interceptors)))

(defn action->try
  [action]
  (fn [state]
    (try (action state)
         (catch Exception e
           (assoc state :error e)))))

(defn looper
  [state interceptors action]
  (loop [state        state
         interceptors interceptors
         backwards    '()
         action       action
         direction    :enter]
    (let [direction-error?    (= :error direction)
          direction-enter?    (= :enter direction)
          exception           (:error state)]
      (cond
        ;; just got an exception, executing all remaining interceptors backwards
        (and exception (not direction-error?))
        (recur state
               (if direction-enter? backwards interceptors)
               '()
               action
               :error)
        ;; executes current direction (:enter, :leave or :error)
        (seq interceptors)
        (let [direction (if (and direction-error? (not exception))
                          :leave ; error was "handled", executing remaining interceptors (:leave direction)
                          direction)
              act       (-> interceptors
                            first
                            (get direction identity)
                            action->try)
              state     (act state)
              next-interceptors (if (and (:error state) (= :leave direction))
                                  interceptors
                                  (rest interceptors))]
          (recur state
                 next-interceptors
                 (when direction-enter? (conj backwards (first interceptors)))
                 action
                 direction))
        direction-enter?
        (recur ((action->try action) state)
               backwards
               '()
               identity
               :leave)
        :else state))))

(defn execute
  "Execute the interceptors queue and invoke the
  action procedure between its enter-leave stacks."
  [state default-interceptors]
  (let [interceptors (-concat
                       (get-in state [:request-data :interceptors])
                       default-interceptors)
        action       (action->try (get-in state [:request-data :action] identity))]
    ;; execute the interceptors queue calling the action
    ;; between its enter/leave stacks
    (looper state interceptors action)))

(def routes [{:path "/"
              :method :get
              :response {:status 200
                         :body "Hi there!"}}
             {:path "/hello/:who"
              :method :get
              :response (fn [req]
                          {:status 200
                           :body (str "Hello, " (:who (:params req)))})}])


(defrecord State
  [request request-data response session-data deps])

(defn make
  "Create an empty state structure."
  [deps request]
  (->
    {:deps deps
     :request  request
     :response {}}
    map->State (conj {})))

(require '[ruuter.core :as ruuter])


(defn handler-fn
  [deps]
  (fn handle*
    ([http-request]
     (let [state (make deps http-request)
           queue (list #(execute % (:router-interceptors deps))
                       ;; At this point 
                       #(route/match %)
                       #(execute % (:controller-interceptors deps)))
           result (reduce (fn [s f] (f s)) state queue)]
       (:response result)))
    ([request respond _]
     (respond (handle* request)))))

(defn path->regex-path
  "Takes in a raw route `path` and turns it into a regex pattern to
  match against the request URI."
  [path]
  (cond (= "/" path)
        "\\/"
        (re-find #"\*" path)
        (-> (clojure.string/replace path #"\:.*?\*" ".*?")
            (clojure.string/replace #"/" "\\/"))
        :else
        (->> (clojure.string/split path #"/")
             (map #(cond
                     ; matches anything, and must be present
                     ; for example `:name`
                     (and (clojure.string/starts-with? % ":")
                          (not (clojure.string/ends-with? % "?")))
                     ".*"
                     ; matches anything, but is optional
                     ; for example `:name?`
                     (and (clojure.string/starts-with? % ":")
                          (clojure.string/ends-with? % "?"))
                     "?.*?"
                     :else
                     ; what comes around, goes around
                     %))
             (clojure.string/join "\\/"))))

(defn- path+uri->path-params
  "Takes a raw route `path` and the actual request `uri`, which it then
  turns into a map of k:v, if any parameters were used in the `path`."
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
  "For a collection of `route`, will attempt to find one that matches
  the given `uri` and `request-method`. If none is matched, `nil` will
  be returned instead."
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
  "Given the current route and the current HTTP request, it will
   attempt to return a response, either directly if it's a map or
   indirectly if it's a function. In case of a function, it will also
   pass along the request map with added-in params that were parsed
   from the route path.
   If the response is invalid, or does not exist, a error message with
   status code 404 will be returned instead."
  [{:keys [path response]} {:keys [uri] :as req}]
  (cond
    ; responses are maps, so there's no reason they can't be
    ; direct maps.
    (map? response)
    response
    ; responses can also be functions that return maps, and
    ; when using a function, you get the whole `req` and params
    ; with it as well.
    (fn? response)
    (response (->> {:params (path+uri->path-params path uri)}
                   (merge req)))
    ; if by whatever reason we make it here it must mean the
    ; route is invalid, or doesn't exist, in which case we return
    ; an error message.
    :else
    {:status 404
     :body "Not found."}))

(defn route
    "For a given collection of `routes` and the current HTTP request as
  `req`, will attempt to match a route with the HTTP request, which it
  will then try to return a response for. The only requirement for `req`
  is to contain both a `uri` and `request-method` key. First should match
  the request path (like the paths defined in routes) and the second
  should match the request method used by the HTTP server you pass this fn to.
  If no route matched for a given HTTP request it will try to find a
  route with `:not-found` as its `:path` instead, and return the response
  for that, and if that route was also not found, will return a built-in
  404 response instead."
  [routes {:keys [uri request-method] :as req}]
  (if-let [route (match-route routes uri request-method)]
    (route+req->response route req)
    (route+req->response (->> routes
                              (filter #(= :not-found (:path %)))
                              first) req)))

;; ---
^{:nextjournal.clerk/visibility {:code :hide}}
(j/show! #'self)
^{:nextjournal.clerk/visibility {:code :hide}}
(j/instrument)
(comment
  (j/cold-dev!)
  (j/dev-without-portal)
  (j/serve!)

;
  )
