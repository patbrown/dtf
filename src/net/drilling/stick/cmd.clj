(ns net.drilling.stick.cmd
  (:require [babashka.process :as sh]
            [medley.core]))

(def cmd
  {:add nil
   :rm nil
   :pr nil
   :edit nil
   :task ["tb" "-t"]
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

(defn -main [& args]
  (let [cmd (first args)
        args (rest args)
        all (flatten (into (:cmd cmd) args))]
    (println (vec all))))
