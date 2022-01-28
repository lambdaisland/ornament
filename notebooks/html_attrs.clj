(ns notebooks.html-attrs
  (:require [nextjournal.clerk :as clerk]
            [lambdaisland.ornament :as o]
            [lambdaisland.hiccup :as h]))

(def html (comp clerk/html h/render))

^{::clerk/no-cache true
  ::clerk/visibility :hide}
(html [:style (o/defined-styles)])

;; Let's take a simple button component

(o/defstyled button :button
  :border-1 :border-black
  :p-2 :m-1 :rounded
  [:&:disabled
   :border-gray-400
   :text-gray-400])

(o/as-garden button)
(o/css button)

;; This is what it looks like

(html [button  "Click me"])

;; It's basically just an alias for a HTML element, so we can give it an
;; attributes map and children.

(html [button {:type "submit"
               :disabled "disabled"}
       "Click me"])

;; Now suppose we actually have some inner markup for this button that we want
;; to encapsulate, so we add a render function.

(o/defstyled button2 :button
  :border-1 :border-black
  :p-2 :m-1 :rounded
  [:&:disabled
   :border-gray-400
   :text-gray-400]
  ([]
   [:<> "Send!"]))

(html [button2])

;; That's cool and all, but now we are no longer able to pass in html
;; attributes, which is somewhat confusing.

(comment
  (html [button2 {:disabled "disabled"}])
  ;; Arity exception
  )

;; Currently what you can do is use metadata inside the render function to
;; signal that you want to set attributes on the outer element (the button). You
;; can think of the caret as pointing "upwards" to the component itself.

(o/defstyled button3 :button
  :p-2 :m-1 :border-1
  :border-black :text-gray-800 :rounded
  [:&:disabled :border-gray-400 :text-gray-400]
  ([]
   ^{:disabled "disabled"}
   [:<> "Send!"]))

(html [button3])

;; And you can take an attribute map as an argument, and then pass that through,
;; to allow setting attributes from the outer markup.

(o/defstyled button4 :button
  :p-2 :m-1 :border-1
  :border-black :text-gray-800 :rounded
  [:&:disabled :border-gray-400 :text-gray-400]
  ([attrs]
   (with-meta
     [:<> "Send!"]
     (merge {:disabled "disabled"} attrs))))

(html [button4 {:id "my-btn"}])

;; So far for the exposition of the current situation. It is not great, to say
;; the least. It's an escape hatch that we added in to still be able to do these
;; things, but it's ugly and unintuitive.

;; Let's actually pick this apart, there are basically three places where
;; attributes can come from

;; - Ornament / the component : we have a class attribute which we always need
;;   to add

;; - Render function : people should be able to declare common attributes as
;;   part of the render function

;; - Markup: when using a component you may want to set extra attributes

;; What we want to get to is something much simpler, unsurprising, and consistent.
;; The main proposal is to start supporting this syntax:

(o/defstyled button4 :button
  :p-2 :m-1 :border-1
  :border-black :text-gray-800 :rounded
  [:&:disabled :border-gray-400 :text-gray-400]
  ([]
   [:<> {:disabled "disabled"} "Send!"]))

;; While this is new syntax it should be unsurprising, `:<>` is a placeholder for
;; the component tag (`:button`), and it's followed by its attributes. When
;; rendering we would add in the ornament classname, and we're done.

;; The question is what happens when people pass in attributes from the outside,
;; because now this no longer works:

(comment
  (html [button4 {:type "submit"}]))

;; Currently that's a arity exception because this component takes no arguments.
;; Even if it did, Ornament is not doing anything with that map, but the
;; component author could:

(o/defstyled button4 :button
  :p-2 :m-1 :border-1
  :border-black :text-gray-800 :rounded
  [:&:disabled :border-gray-400 :text-gray-400]
  ([attrs]
   [:<> (merge {:disabled "disabled"} attrs) "Send!"]))

;; - Pros: simple, consistent, fairly predictable
;; - Cons: components need to explicitly add support, it's a fairly naive approach, we might want smarter merging

;; By smart merging we mainly mean: merging of class and style attributes

(o/defstyled button5 :button
  ([attrs]
   [:<> {;; Extra class in addition to .notebooks_html-attrs__button5
         :class "extra-class"
         ;; inline styles
         :style {:color "blue"}} "Send!"]))

(comment
  [button5 {:class ["more-class" "and-more"]
            :style {:padding "1rem"}}])

;; Does it make sense to want these things to merge?

;; Something to consider as well is that components may pass a map with any old
;; data into a component, we can't just assume that it's HTML attributes

(comment
  [blog-view
   {:posts posts
    :authors authors}])

;; So we like a solution that's explicit and opt-in, but that still always
;; provides a way to set extra attrs from the outside, even if the component did
;; not account for that.

;; A solution could be a custom key in the map, perhaps namespaced to be
;; unambiguously an Ornament thing

(comment
  [blog-view
   {:posts posts
    :authors authos
    ::o/attrs {:id "foo"}}])
