;; Used to fire off commands at the cli
(ns cmd
  (:require [babashka.process :as sh]
            [medley.core]))

(def cmd
  {:task ["tb" "-t"]
   :note ["tb" "-n"]
   :board ["tb" "-t"]
   :complete ["tb -c"]
   :focus ["tb -b"]
   :mark ["tb -s"]
   :copy ["tb -y"]
   :view ["tb"]
   :timeline ["tb -i"]
   :prioritize ["tb -p @"]
   :move ["tb -m"]
   :delete ["tb -d"]
   :kill ["tb --clear"]
   :graveyard ["tb -a"]
   :restore ["tb -r"]
   :search ["tb -f"]})

(def menu (map name (keys cmd)))
