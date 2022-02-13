(ns notebooks.template
  (:require
   [lambdaisland.ornament :as o]
   [lambdaisland.ornament.clerk-util :refer [inline-styles render]]))

;; # Ornament Notebook Template

;; Define components

(o/defstyled strong-link :a
  {:font-weight 1000})

;; Render them with Hiccup

(render
 [strong-link {:href "https://github.com/lambdaisland/open-source"}
  "Check out our open source offerings!"])

;; Inline our styles last, so that this happens after all `defstyled`s are
;; defined.

^{:nextjournal.clerk/no-cache true}
(inline-styles)
