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
                               :message "Tag must be a keyword or an ornament-styled-component"
                               :type :lambdaisland.ornament/invalid-syntax}))
        ; _ (prn :html-tag html-tag)
        ; _ (prn :more more)
        fn-tag (first (drop-while (fn [x]
                                    (or (api/string-node? x)
                                        (api/keyword-node? x)
                                        (api/map-node? x)
                                        (api/vector-node? x)))
                                  more))
        ; _ (prn :fn-tag fn-tag)
        _ (when (and fn-tag
                     (not (api/list-node? fn-tag)))
            (api/reg-finding! {:row (:row (meta fn-tag))
                               :col (:col (meta fn-tag))
                               :message "Function part (if present) must be a list"
                               :type :lambdaisland.ornament/invalid-syntax}))]
    (if (api/list-node? fn-tag)
      (let [[binding-vec & body] (:children fn-tag)
            fn-node (api/list-node
                     (list*
                      (api/token-node 'fn)
                      binding-vec
                      body))
            new-def-node (api/list-node
                          (list (api/token-node 'def)
                                class-name
                                fn-node))]
        (prn :new-def-node (api/sexpr new-def-node))
        {:node new-def-node})
     ;; nil node
      (let [def-class-form (api/list-node
                            (list (api/token-node 'def)
                                  class-name
                                  (api/token-node 'nil)))]
        (prn :def-class-form (api/sexpr def-class-form))
        {:node def-class-form}))))
