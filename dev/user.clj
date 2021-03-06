(ns user)

(defmacro jit [sym]
  `(requiring-resolve '~sym))

(defn browse []
  ((jit clojure.java.browse/browse-url) "http://localhost:7777"))

(def portal-instance (atom nil))

(defn portal
  "Open a Portal window and register a tap handler for it. The result can be
  treated like an atom."
  []
  ;; Portal is both an IPersistentMap and an IDeref, which confuses pprint.
  (prefer-method @(jit clojure.pprint/simple-dispatch) clojure.lang.IPersistentMap clojure.lang.IDeref)
  (let [p ((jit portal.api/open) @portal-instance)]
    (reset! portal-instance p)
    (add-tap (jit portal.api/submit))
    p))

(defn clerk! []
  ((jit nextjournal.clerk/serve!) {:watch-paths ["notebooks"]}))
