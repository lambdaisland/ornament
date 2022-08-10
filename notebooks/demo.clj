(ns demo
  (:require [lambdaisland.hiccup :as hiccup]
            [lambdaisland.ornament :as o]
            [nextjournal.clerk :as clerk]))

;; # A Small Demonstration of Ornament

;; Helper to render components:

(defn render [h]
  (clerk/html (hiccup/render h {:doctype? false})))

;; A relatively simple component, using Girouette (Tailwind-style) styling, and
;; leaning into the fact that we can organize our styles however we like,
;; including splitting things up and adding comments.

(o/defstyled navbar :nav
  ;; layout
  :flex :space-x-4
  [:a :px-3 :py-2 :my-2]
  ;; fonts & borders
  :font-sans
  [:a :text-sm :font-medium :rounded-md]
  ;; colors
  :bg-gray-800
  [:a :text-gray-300 :hover:bg-gray-700 :hover:text-white
   :rounded-md :text-sm :font-medium
   [:&.active :bg-gray-900 :text-white]]
  ([links]
   (for [[{:keys [text href active?]}] links]
     [(if active? :a.active :a)
      {:href href}
      text])))

;; Let's see what that looks

(render
 [navbar
  [[{:text "Lambda Island"
     :href "https://lambdaisland.com"
     :active? true}]
   [{:text "Gaiwan"
     :href "https://gaiwan.co"}]]])

;; We can also inspect all aspects of the component

(o/as-garden navbar)

(o/css navbar)

(navbar [[{:text "Lambda Island"
           :href "https://lambdaisland.com"
           :active? true}]
         [{:text "Gaiwan"
           :href "https://gaiwan.co"}]])

;; Inline our styles last, so all component styles are certainly defined.

^{::clerk/no-cache true}
(render [:style (o/defined-styles)])
