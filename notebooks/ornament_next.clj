(ns ornament-next
  (:require
   [lambdaisland.hiccup :as hiccup]
   [lambdaisland.ornament :as o]
   [nextjournal.clerk :as clerk]))

(reset! o/registry {})
(reset! o/rules-registry {})
(reset! o/props-registry {})

;; Original Ornament was all about styled components, meaning we put
;; Garden-syntax CSS inside your components to style them. Later on we added
;; support for Girouette, which means you can use shorthand tags similar to
;; Tailwind utility classes to define your style rules.

;; This is great, but it's not the full story. Ornament Next gives you several
;; news ways to define and structure your styles, and better dev-time
;; affordances.

;; Here's a regular old Ornament styled component, except that it now sports a
;; docstring. The docstring that actually gets set on the var also contains the
;; compiled CSS, and we set `:arglists`, so you can see how to use it aas a
;; component in your Hiccup.

(o/defstyled user-form :form
  "Form used on the profile page"
  :mx-3)

(:arglists (meta #'user-form))
(:doc (meta #'user-form))

;; The new macros that follow all support docstrings.

;; ## defrules

;; The most basic one is `defrules`, which lets you define plain Garden CSS
;; rules that get prepended to your Ornament styles. Realistically there are
;; always still things you define globally, and you shouldn't have to jump
;; through extra hoops to do so. `defrules` still takes a name and optionally a
;; docstring, so you can split up your styles and document them.

(o/defrules my-style
  "Some common defaults"
  [:* {:box-sizing "border-box"}]
  [:form :mx-2])

;; ## defutil

;; There's now also `defutil` for defining utility classes. This is in a way
;; similar, in that it defines global CSS, but you get a handle onto something
;; that you can use like a CSS class.

(o/defutil square
  "Ensure the element has the same width and height."
  {:aspect-ratio 1})

;; This creates a utility class in your CSS. Note that it's namespaced, like all
;; classes in Ornament, to be collision free.

(o/defined-styles)

;; You can now use this in multiple ways, the simplest is direcly in hiccup.

(hiccup/render [:img {:class [square]}])

;; You can also use it in styled components, to pull those additional style
;; rules into the CSS of the component.

(o/defstyled avatar :img
  "A square avatar"
  square)

(o/css avatar)

;; ## defprop

;; Modern CSS heavily leans on CSS custom properties, also known as variables.
;; These are especially useful for defining design tokens.

;; These can be defined with or without

(o/defprop without-default)
(o/defprop color-primary "hsla(201, 100%, 50%, 1)")

(o/defined-styles)
