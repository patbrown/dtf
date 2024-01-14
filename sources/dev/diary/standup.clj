;; # `DIARY:` STAND THIS BITCH UP
;; ---
(ns diary.standup
 {:nextjournal.clerk/visibility {:code :show :result :hide}}
  (:require [j]
            [nextjournal.clerk :as clerk]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def self *ns*)

;; ---
;; ### DIARY: Start with fun maps which will be the base of all.

(require '[robertluo.fun-map :as fun])

;; so I'm creating a macro for resources. They are all lifecycle by default. They are all self-contained maps by default. I'm just going to adorn resources with plain data in meta in the runtime, let the meta expand as we go. It's a weapon.
;; ---
;; So here is the starter macro

(defmacro rsc [id & fms]
  (let [nmsp# (-> id namespace str (clojure.string/replace #"\." "*"))
        sym# (symbol (str nmsp# "$" (name id)))]
    (do `(def ~(with-meta sym# {:rsc? true
                                :id id})
           (robertluo.fun-map/life-cycle-map (identity (apply merge ~@fms)))))))

;; - Routes
;; - Attributes/Specs/Datatypes
;; - RBAC
;; - Queries
;; - Commands



;; This is an example call
^{:nextjournal.clerk/visibility {:result :show}}
(def compb {:component/b (fun/fnk [:component/a] (fun/closeable (inc a) #(println "halt :b")))})
(def compd {:component/d {::id :that}})
;; Notice I can include these resources in each other. 
(rsc :t/a {:component/a
           (fun/fnk []
                    (fun/closeable 100 #(println "halt :a")))}
     compb
     compd)
;; here is what it looks like
^{:nextjournal.clerk/no-cache true
  :nextjournal.clerk/visibility {:result :show}}
t$a

(rsc :t/b {:component/c (fun/fnk [:component/a]
                                 (* a 8))}
     t$a)

;; Further embedding and now we've got some magic
^{:nextjournal.clerk/visibility {:result :show}}
(fun/touch t$b)

^{:nextjournal.clerk/visibility {:result :show}}
(:component/b t$a)

;; ### So what do I do with these to make a change for the better.
;; **These are the things that I want to put together**
;; ---
;; - Routes
;; - Actions
;; - Attributes/Specs/Datatypes
;; - RBAC
;; - Queries
;; - Commands



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
