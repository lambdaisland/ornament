(ns lambdaisland.ornament.clerk-util
  (:require [lambdaisland.hiccup :as hiccup]
            [lambdaisland.ornament :as o]
            [nextjournal.clerk :as clerk]))

(defn render
  "Render hiccup containing Ornament component references inside a Clerk
  notebook."
  [h]
  (clerk/html (hiccup/render h {:doctype? false})))

(defn inline-styles
  "Inject our CSS styles into the Clerk document, so components render correctly.
  Add this at the end of your notebook, and add a 'no-cache' marker.

  ```
  ^{::clerk/no-cache true}
  (util/inline-styles)
  ```
  "
  []
  (render [:style (o/defined-styles)]))

(defn expand
  "Expand a hiccup form with an ornament component to plain hiccup elements. Does not recurse."
  [[component & args]]
  (o/as-hiccup component args))
