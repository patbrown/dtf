(ns tiny.app.v1
  (:require [tiny.secrets :refer [get-secret]]
            [tiny.app.routes :refer [routes]]))

(def db
  {:port     (get-secret :patbrown.smallfoot.db/port)
   :dbname   (get-secret :patbrown.smallfoot.db/dbname)
   :host     (get-secret :patbrown.smallfoot.db/host)
   :dbtype   (get-secret :patbrown.smallfoot.db/dbtype)
   :user     (get-secret :patbrown.smallfoot.db/user)
   :password (get-secret :patbrown.smallfoot.db/password)})

(def manifest {:db db
               :server {:port  3000
                        :host "0.0.0.0"
                        :join? false}
               :routes routes})
