(ns patbrown.secrets
  #?(:bb (:require [babashka.pods :as pods]
                   [clojure.java.io :as io]
                   [clojure.java.shell :as sh]
                   [clojure.pprint :as pprint]
                   [clojure.string :as string])
     :clj (:require [buddy.core.codecs :as codecs]
                    [buddy.core.nonce :as nonce]
                    [buddy.core.crypto :as crypto]
                    [buddy.core.kdf :as kdf]
                    [clojure.java.io :as io]
                    [clojure.java.shell :as sh]
                    [clojure.pprint :as pprint]
                    [clojure.string :as string]))
  (:import (java.util Base64)))

#?(:bb (pods/load-pod 'org.babashka/buddy "0.3.4"))
#?(:bb (require '[pod.babashka.buddy.core.codecs :as codecs]
                '[pod.babashka.buddy.core.nonce :as nonce]
                '[pod.babashka.buddy.core.crypto :as crypto]
                '[pod.babashka.buddy.core.kdf :as kdf]))

(set! *warn-on-reflection* true)
(defn bytes->b64 [^bytes b] (String. (.encode (Base64/getEncoder) b)))
(defn b64->bytes [^String s] (.decode (Base64/getDecoder) (.getBytes s)))

#?(:bb (defn slow-key-stretch-with-pbkdf2 [weak-text-key n-bytes]
         (kdf/get-engine-bytes
          {:key weak-text-key
           :salt (b64->bytes "j3gT0zoPJos=")
           :alg :pbkdf2
           :digest :sha512
           :iterations 1e5
           :length n-bytes}))
   :clj (defn slow-key-stretch-with-pbkdf2 [weak-text-key n-bytes]
          (kdf/get-bytes
           (kdf/engine {:key weak-text-key
                        :salt (b64->bytes "j3gT0zoPJos=")
                        :alg :pbkdf2
                        :digest :sha512
                        :iterations 1e5}) ;; target O(100ms) on commodity hardware
           n-bytes)))


(defn encrypt
  "Encrypt and return a {:data <b64>, :iv <b64>} that can be decrypted with the
  same `password`.
  Performs pbkdf2 key stretching with quite a few iterations on `password`."
  [clear-text password]
  (let [initialization-vector (nonce/random-bytes 16)
        encrypt-fn #?(:bb crypto/block-cipher-encrypt :clj crypto/encrypt)]
     {:data (bytes->b64
             (encrypt-fn
              (codecs/to-bytes clear-text)
              (slow-key-stretch-with-pbkdf2 password 64)
              initialization-vector
              {:algorithm :aes256-cbc-hmac-sha512}))
      :iv (bytes->b64 initialization-vector)}))

(defn decrypt
  "Decrypt and return the clear text for some output of `encrypt` given the
  same `password` used during encryption."
  [{:keys [data iv]} password]
  (let [decrypt-fn #?(:bb crypto/block-cipher-decrypt :clj crypto/decrypt)]
    (codecs/bytes->str
     (decrypt-fn
      (b64->bytes data)
      (slow-key-stretch-with-pbkdf2 password 64)
      (b64->bytes iv)
      {:algorithm :aes256-cbc-hmac-sha512}))))

(def ^:dynamic *default-secret*
  (get  (into {} (System/getenv)) "SALT"))
(def ^:dynamic *secrets-file-location*
  (or (get (into {} (System/getenv)) "SECRET_DB") "resources/.config/secrets"))

(defn encrypt-secrets!
  ([data] (encrypt-secrets! *default-secret* data))
  ([pass data]
   (spit *secrets-file-location* (encrypt (str data) pass))))

(defn decrypt-secrets!
  ([] (decrypt-secrets! *default-secret*))
  ([pass]
   (let [raw-secrets (read-string (slurp *secrets-file-location*))]
     (clojure.edn/read-string (decrypt raw-secrets pass)))))

(defn add-secret!
  ([k v] (add-secret! *default-secret* k v))
  ([pass k v]
   (let [secrets (decrypt-secrets! pass)
         new-secrets (assoc secrets k v)]
     (encrypt-secrets! pass new-secrets))))

(defn rm-secret!
  ([k] (rm-secret! *default-secret* k))
  ([pass k]
   (let [secrets (decrypt-secrets! pass)
         new-secrets (dissoc secrets k)]
     (encrypt-secrets! pass new-secrets))))

(defn get-secret
  ([] (decrypt-secrets! *default-secret*))
  ([k] (if (keyword? k)
         (get-secret *default-secret* k)
         (decrypt-secrets! k)))
  ([pass k]
   (get-in (decrypt-secrets! pass) (if-not (vector? k) [k] k))))

(comment
(encrypt-secrets! {:a 0})
(decrypt-secrets!)
(add-secret! :b 10)
(get-secret :a)
  )
