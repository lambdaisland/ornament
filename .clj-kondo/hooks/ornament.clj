(ns hooks.ornament
  (:require [clj-kondo.hooks-api :as api]))

(defn defstyled [{:keys [node]}]
  (let [[class-name html-tag & more] (rest (:children node))
        _ (when-not (and (api/token-node? class-name)
                         (simple-symbol? (api/sexpr class-name)))
            (api/reg-finding! {:row (:row (meta class-name))
                               :col (:col (meta class-name))
                               :message "Style name must be a symbol"
                               :type :lambdaisland.ornament/invalid-syntax}))
        ; _ (prn :class-name class-name)
        _ (when-not (api/keyword-node? html-tag)
            (api/reg-finding! {:row (:row (meta html-tag))
                               :col (:col (meta html-tag))
                               :message "Html-tag must be a keyword"
                               :type :lambdaisland.ornament/invalid-syntax}))
        ; _ (prn :html-tag html-tag)
        ; _ (prn :more more)
        fn-tag (first (drop-while (fn [x]
                                    (or (api/keyword-node? x)
                                        (api/map-node? x)
                                        (api/vector-node? x)))
                                  more))
        _ (prn :fn-tag fn-tag)
        _ (when (or (api/list-node? fn-tag)
                    (nil? fn-tag))
            (api/reg-finding! {:row (:row (meta fn-tag))
                               :col (:col (meta fn-tag))
                               :message "fn-tag must be at least a list or nil"
                               :type :lambdaisland.ornament/invalid-syntax}))
        def-class-form (api/list-node
                        (list (api/token-node 'def)
                              class-name
                              (api/token-node 'nil)))]
    (if (api/list-node? fn-tag)
      (let [[binding-vec & body] (:children fn-tag)
            new-node (api/list-node
                      (list*
                       (api/token-node 'fn)
                       binding-vec
                       body))]
        (prn :new-node (api/sexpr new-node))
        {:node new-node})
     ;; nil node
      {:node def-class-form})))
