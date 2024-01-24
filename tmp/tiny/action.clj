(ns tiny.action)

(defmacro defaction [ctx]
  (let [{:keys [actions#]} ctx
        sym# (symbol (name (:name ctx)))
        fqid# (keyword "action" (name sym#))
        metamap# nil
        m# nil]
    (doall [`(intern 'action
                     '~(with-meta sym# (merge {:action? true
                                              :action ctx
                                              :id fqid#}
                                             metamap#))
                     ~m#)
            `(def ~(with-meta sym# metamap#) ~m#)])))

(defmacro defworkflow [ctx]
  (let [{:keys [actions#]} ctx
        sym# (symbol (name (:name ctx)))
        fqid# (keyword "workflow" (name sym#))
        metamap# nil
        m# nil]
    (do `(intern 'workflow
                 ~(with-meta sym# (merge {:workflow? true
                                          :workflow ctx
                                          :id fqid#}
                                         metamap#))))))
