(ns tiny.db.details
  (:require [clojure.string]))

;; Locker naming convention for sql tables
(defn kw->storage-name [n]
  (if (string? n)
    n
    (let [n0 (str (namespace n) "_ndnmsp_" (name n))
          n1 (if (clojure.string/starts-with? n0 "_") (clojure.string/replace-first n0 #"\_" "") n0)
          n2 (clojure.string/replace  n1 #"\-" "_nddash_")
          n3 (clojure.string/replace n2 #"\?" "_ndqmark_")
          n4 (clojure.string/replace n3 #"\$" "_nddollar_")
          n5 (clojure.string/replace n4 #"\!" "_ndbang_")
          table-name (clojure.string/replace n5 #"\." "_nddot_")]
      table-name)))

(defn storage-name->kw [n]
  (if (keyword? n)
    n
    (let [n0 (clojure.string/replace n #"_nddash_" "-")
          n1 (clojure.string/replace n0 #"_ndqmark_" "?")
          n2 (clojure.string/replace n1 #"_nddollar_" "$")
          n3 (clojure.string/replace n2 #"_ndbang_" "!")
          n4 (clojure.string/replace n3 #"_nddot_" ".")
          n5 (clojure.string/split n4 #"_ndnmsp_")]
      (apply keyword n5))))
