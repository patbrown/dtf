(ns tiny.ui)

;; # A STORY OF HELPFUL STEWARDSHIP

;; ### The are all components after all
;; From windows down to atoms, they are all components.
;; This makes it smooth and easy to speak the same language.
;; ---
;; ### HOUSE KEEPING
;; This push dataflow for ui makes the story hard to tell in order.
(declare chains prep-contents contents create render interpret)
;; ### TWO IMPORTANT KEYS
;; - SELF - where the config is
;; - FOCUS - where the data is

;; - Hierarchy dictates that I can have clear, requests and provides


;; ### START WITH JUST DATA
;; Components use a variant key dispatch to allow for different concerns at different levels.
;; This is a hierarchy driven system
;; variant is the level and it's the dispatch for everything by default   
;; Remember it's a push!
(defmulti create #(-> % :self :component/variant))
(defmulti focus #(-> % :self :component/variant))
(defmulti i-require #(-> % :self :component/variant))
(defmulti i-accept #(-> % :self :component/variant))
(defmulti i-provide #(-> % :self :component/variant))
(defmulti self #(-> % :self :component/variant))
(defmulti load #(-> % :self :component/variant))
(defmulti unload #(-> % :self :component/variant))

(defn *create [ctx] nil)
(defn *focus [ctx] nil)
(defn *i-provide [ctx] nil)
(defn *i-require [ctx] nil)
(defn *self [ctx] nil)
(defn *load [ctx] nil)
(defn *unload [ctx] nil)


(comment (create {:self {:component/variant :component/compound
                         :compound/variant :compound/on-off
                         :compound/molecule :molecule/radio-button
                         :compound/data-attributes [[:attr/id :well/name]]
                         :instance/dt [:dt/id :dt/compound]
                         :instance/traits #{}
                         :instance/tags {"a" "v"}}}))



