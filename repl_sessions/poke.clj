(ns poke
  (:require
   [lambdaisland.ornament :as o]
   [lambdaisland.hiccup :as hiccup]))

(set! *print-namespace-maps* false)

(o/defstyled freebies-link :a
  {:font-size "1rem"
   :color "#cff9cf"
   :text-decoration "underline"})

(o/rules freebies-link)

(freebies-link {:href "/episodes/interceptors-concepts"} "hello")

[:a {:class ["poke__freebies_link"]
     :href "/episodes/interceptors-concepts"} "hello"]

(o/defstyled foo :div
  {:margin size-2})
(o/css foo)
(o/defprop size-2 "2rem")
(o/defrules main-styles
  "Main application styles"
  [:.link {:color "blue"}]
  [:.link:visited {:color "purple"}]
  [:main
   [:.container {:width size-2}]])

(garden.compiler/expand size-2)
main-styles
(o/defutil square {:aspect-ratio 1})
o/props-registry
(o/defined-garden)
(o/defined-styles)
(o/defstyled avatar :img
  {size-2 size-2}
  #_#_(garden.stylesheet/at-media {"print" true} [:& {:color "blue"}])
  (garden.stylesheet/at-keyframes "myanim" [:100% {:height "10px"}])
  )
(#'garden.compiler/expand-stylesheet {size-2 size-2})
(garden.compiler/compile-css [:& {size-2 size-2}])

(hiccup/render [avatar {:style {size-2 "3rem"}}])
(map class (o/process-rules
            (o/rules avatar)))
duration |
easing-function |
delay |
iteration-count |
direction |
fill-mode |
play-state |
name

(o/defanimation pulse
  :duration
  "2s"
  :keyframes
  ["0%" "100%" {:opacity 1}]
  ["50%" {:opacity 0.5}])

(o/css avatar)

*e
(o/defined-styles)

(hiccup/render [:div {:class [square]}])

()
