(ns hooks.ornament
  (:require [clj-kondo.hooks-api :as api]))

(defonce styled-registry (atom #{}))

(defn- symbol-node? [node]
  (and (api/token-node? node)
       (symbol? (api/sexpr node))))

(defn- styled-component? [node]
  (and (symbol-node? node)
       (contains? @styled-registry (api/sexpr node))))

(defn defstyled [{:keys [node]}]
  (let [[class-name html-tag & more] (rest (:children node))
        _ (when-not (and (symbol-node? class-name)
                         (simple-symbol? (api/sexpr class-name)))
            (api/reg-finding! {:row (:row (meta class-name))
                               :col (:col (meta class-name))
                               :message "Style name must be a simple symbol"
                               :type :lambdaisland.ornament/invalid-syntax}))
        _ (when-not (or (api/keyword-node? html-tag)
                        (styled-component? html-tag))
            (api/reg-finding! {:row (:row (meta html-tag))
                               :col (:col (meta html-tag))
                               :message "Tag must be a keyword or an ornament-styled-component"
                               :type :lambdaisland.ornament/invalid-syntax}))
        fn-tag (first (drop-while (fn [x]
                                    (or (api/string-node? x)
                                        (api/keyword-node? x)
                                        (api/map-node? x)
                                        (api/vector-node? x)
                                        (api/token-node? x)))
                                  more))
        _ (when (and fn-tag
                     (not (api/list-node? fn-tag)))
            (api/reg-finding! {:row (:row (meta fn-tag))
                               :col (:col (meta fn-tag))
                               :message "Function part (if present) must be a list"
                               :type :lambdaisland.ornament/invalid-syntax}))
        symbol-tag? (symbol-node? html-tag)]
    ;; Register this component in the styled-registry
    (swap! styled-registry conj (api/sexpr class-name))
    (if (api/list-node? fn-tag)
      (let [[binding-vec & body] (:children fn-tag)
            fn-node (api/list-node
                     (list*
                      (api/token-node 'fn)
                      binding-vec
                      body))
            new-def-node (api/list-node
                          (if symbol-tag?
                            (list (api/token-node 'def)
                                  class-name
                                  (api/list-node
                                   (list (api/token-node 'do)
                                         html-tag
                                         fn-node)))
                            (list (api/token-node 'def)
                                  class-name
                                  fn-node)))]
        {:node new-def-node})
      (let [def-class-form (api/list-node
                            (if symbol-tag?
                              (list (api/token-node 'def)
                                    class-name
                                    html-tag)
                              (list (api/token-node 'def)
                                    class-name
                                    (api/token-node 'nil))))]
        {:node def-class-form}))))
