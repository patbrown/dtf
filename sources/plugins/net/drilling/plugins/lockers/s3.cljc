(ns net.drilling.plugins.lockers.s3
  #?(:bb (:require [clojure.edn]
                   [com.grzm.awyeah.client.api :as aws]
                   [net.drilling.plugins.secrets :as ***]
                   [net.drilling.plugins.supastore :refer [commit cleanup commit! supastore create-supastore]])
     :clj (:require [clojure.edn]
                    [cognitect.aws.client.api :as aws]
                    [cognitect.aws.credentials :as credentials]
                    [net.drilling.plugins.secrets :as ***]
                    [net.drilling.plugins.supastore :refer [commit cleanup commit! supastore create-supastore
                                                            kw->locker-name locker-name->kw]]))
  #?(:bb (:import [net.drilling.plugins.supastore IStorageBackend Storage]
                   [java.io Writer Closeable])
     :clj (:import [net.drilling.plugins.supastore IStorageBackend]
                   [clojure.lang IAtom IDeref IRef IMeta IObj IReference Atom]
                   (java.util.concurrent.atomic AtomicBoolean)
                   (java.util.concurrent.locks ReentrantLock Lock)
                   [java.io Writer Closeable])))

(def default-storage "fjck")
(def client
  #?(:bb (aws/client {:api :s3})
     :clj (aws/client {:api :s3
                       :region (***/get-secret ::region)
                       :credentials-provider (credentials/basic-credentials-provider
                                              {:access-key-id (***/get-secret ::access-key-id)
                                               :secret-access-key (***/get-secret ::secret-access-key)})})))



(def write-with-default str)
(def read-with-default #(clojure.edn/read-string (slurp %)))
(def default-config
  {::client client
   ::storage default-storage
   ::read-with read-with-default
   ::write-with write-with-default
   ::lock #?(:bb nil :clj (ReentrantLock.))})

(defn simple-call
  [client op req] (aws/invoke client {:op op :request req}))

;; # AWS API
(defn list-buckets [connection]
  (set (mapv :Name (:Buckets (aws/invoke connection {:op :ListBuckets})))))
(defn bucket-exists?
  ([bucket] (if-not (map? bucket)
              (bucket-exists? client bucket)
              (let [{:keys [client bucket]} bucket]
                (bucket-exists? client bucket))))
  ([client bucket] (contains? (list-buckets client) bucket)))

(defn list-objects [connection bucket]
  (set (mapv :Key (:Contents (aws/invoke connection {:op :ListObjects :request {:Bucket bucket}})))))
(defn object-exists?
  ([{:keys [client bucket object]}]
   (object-exists? client bucket object))
  ([client bucket object]
   (contains? (list-objects client bucket) object)))

(defn create-bucket [connection bucket]
  (aws/invoke connection {:op :CreateBucket :request {:Bucket bucket}}))
(defn delete-bucket [connection bucket]
  (aws/invoke connection {:op :DeleteBucket :request {:Bucket bucket}}))
(defn put-object [connection bucket object body]
  (aws/invoke connection {:op :PutObject :request {:Bucket bucket :Key object :Body body}}))

(defn delete-object [connection bucket object]
  (aws/invoke connection {:op :DeleteObject :request {:Bucket bucket :Key object}}))
(defn get-object [connection bucket object]
  (aws/invoke connection {:op :GetObject :request {:Bucket bucket :Key object}}))
(defn get-edn
  ([{:keys [client bucket object]}] (get-edn client bucket object))
  ([bucket object] (get-edn client bucket object))
  ([client bucket object]
   (let [res (:Body (aws/invoke client {:op :GetObject :request {:Bucket bucket
                                                                 :Key object}}))]
     (-> res slurp clojure.edn/read-string))))


(defrecord S3Backend [connection bucket locker read-with write-with atom-ref]
  IStorageBackend
  (snapshot [_]
    (-> (get-object connection bucket (kw->locker-name locker))
        :Body
        slurp
        read-with))
  (commit [this]
    (commit this :deref))
  (commit [this x]
    (let [f (fn [state]
              (put-object connection
                             bucket
                             (kw->locker-name locker)
                             (if (= x :deref) @state x))
              state)]
      (commit! atom-ref f nil)))
  (cleanup [_]
    (delete-object connection bucket (kw->locker-name locker))))

(defn s3-supastore
  ([{:keys [connection bucket locker read-with write-with]
     :as provided-config}]
   (let [locker (kw->locker-name locker)]
     (create-supastore (merge default-config
                            provided-config
                            {:make-backend (partial ->S3Backend connection bucket locker read-with write-with)})))))

(defmethod supastore :s3 [m] (s3-supastore m))

(defn locker [m]
  (if-not (map? m)
    (supastore (assoc default-config :locker m))
    (supastore (merge default-config m))))

(comment
  (def t17 (supatom {:backing :s3
                     :locker :xmas-eve-eve/hey
                     :bucket "d23m.supatom.testing"
                     :connection s3/client
                     :init {:c 555}}))

  (def asdf-fda (locker :asdf/fda))
  @asdf-fda
  (swap! asdf-fda assoc :a 3)

  (swap! t17 assoc :www 111)
  (slurp (s3/get-object {:object :test/k :bucket "d23m.supatom.testing" :client s3/client}))
  (s3/list-objects "d23m.supatom.testing")
  (s3/bucket-exists? "d23m.supatom.testing")
  (s3/object-exists? "d23m.supatom.testing" :test/k)
;
  )
