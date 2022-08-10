(ns notebooks.attributes-and-properties
  (:require
   [lambdaisland.ornament :as o]
   [lambdaisland.ornament.clerk-util :refer [inline-styles
                                             render
                                             expand]]))

;; # Attributes and Properties

;; When dealing with Ornament components it's important to understand the
;; distinction between "attributes" and "properties".

;;; ## Attributes

;; Attributes are a concept from HTML, they are the key-value pairs you provide
;; to HTML elements in your markup.

;; ```html
;; <a href="//example.com" title="Example">Go to Example</a>
;; ```

;; In this example `href` and `title` are attributes.

;; When you define a plain Ornament component, one that doesn't have a custom
;; render function, then you can pass in attributes by providing a map as the
;; first argument, just like you would do in plain Hiccup.

;; Define components

(o/defstyled strong-link :a
  {:font-weight 1000
   :border "1px solid #999"})

[:a {:href "https://github.com/lambdaisland/open-source"}
 "Check out our open source offerings!"]

(expand
 [strong-link
  {:href "https://github.com/lambdaisland/open-source"}
  "Check out our open source offerings!"])

;; You can see that these two Hiccup forms are basically equivalent, except that
;; in the case of `strong-link` an extra class is added to the `class`
;; attribute.

;; ## Properties

;; When defininig components through a render function, as is common in
;; React/Reagent, you can also pass a map as the first argument. These are
;; called "properties" or (in React especially) as "props".

;; It's up to the component (the render function) to do something with these.

(defn user-list [{:keys [users] :as props}]
  [:ul
   (for [{:keys [name]} users]
     [:li name])])

(render
 [user-list {:users [{:name "Arne"}
                     {:name "Felipe"}]}])

;; The same is true for Ornament components that contain their own render
;; function.

(o/defstyled styled-user-list :ul
  [:li {:list-style "square"}]
  ([{:keys [users] :as props}]
   (for [{:keys [name]} users]
     [:li name])))

(render
 [styled-user-list {:users [{:name "Arne"}
                            {:name "Felipe"}]}])

;; ## Setting Attributes on Styled Components

;; But what if we still want to set certain attributes on this `:ul`, perhaps we
;; want to indicate that this element is in a different language using the
;; `lang` attribute.

;; Generally this is the responsibility of the component itself, it can return a
;; fragment (`:<>`), inclduding an attributes map.


(o/defstyled name-list-de :ul
  [:li {:list-style "square"}]
  ([{:keys [users] :as props}]
   [:<> {:lang "de"}
    (for [{:keys [name]} users]
      [:li name])]))

(expand
 [name-list-de {:users [{:name "Goethe"}
                        {:name "Freud"}]}])


;; So now we've added a `lang` attribute inside the component. You can ignore the
;; extra attributes here like `:col` and `:row`, they are a consequence of how
;; Clerk renders things, combined with the fact that we support a legacy syntax
;; where the attributes are provided as metadata on the result of the render
;; function.

;; The component could even take a `lang` property, and pass that on as a `lang`
;; attribute, so that you can decide to set the language at the point where you
;; are using this component.

;; However Ornament also supports a special property,
;; `:lambdaisland.ornament/attrs`, which will get merged in with the other
;; attributes.

(expand
 [name-list-de {:users [{:name "Goethe"}
                        {:name "Freud"}]
                ::o/attrs {:title "German Philosophers"}}])

;; These `::o/attrs` will take precedence over attributes set inside the
;; component. So our component with `lang=de` could be used for a different
;; language as well. The value inside the component is the default, but can be
;; overruled when using the components.

(expand
 [name-list-de {:users [{:name "René Magritte"}
                        {:name "James Ensor"}]
                ::o/attrs {:lang "nl"
                           :title "Belgische Kunstenaars"}}])

;; When merging attributes like this `:class` and `:style` are handled special.
;; Classes are additive, you get both the classes defined inside the component,
;; the ones passed in through `::o/attrs`, and the special ornament class that
;; gets generated to link this component to its styles. You can use either
;; strings or vectors of strings as the `class` attribute.

;; `:style` attributes are merged (assuming they are maps).

(o/defstyled name-list-klz :ul
  [:li {:list-style "square"}]
  ([{:keys [users] :as props}]
   [:<> {:class "some-class"
         :style {:background-color "red"}}
    (for [{:keys [name]} users]
      [:li name])]))

(expand
 [name-list-klz {:users [{:name "John"}]
                 ::o/attrs
                 {:class "other-class"
                  :style {:text-color "blue"}}}])

;; Class attribute as a vector:

(expand
 [name-list-klz {:users [{:name "John"}]
                 ::o/attrs {:class ["one-class" "other-class"]
                            :style {:text-color "blue"}}}])

;; For both `:class` and `:style` you can get the regular merge behavior back,
;; where the `::o/attrs` value completely replaces the default value specified
;; in the component, by adding a `^:replace` metadata on the vector or map.

(expand
 [name-list-klz {:users [{:name "John"}]
                 ::o/attrs {:class ^:replace ["one-class" "other-class"]
                            :style ^:replace {:text-color "blue"}}}])

;; ## Conclusion

;; While we were working on Ornament, and trying it out on various projects, we
;; found we wanted a large degree of flexibility for setting attributes, either
;; from within the component, or from without.

;; We wanted to stay fairly close to how things work in plain Hiccup, as well as
;; in Reagent, so people can largely rely on their existing mental models.

;; At the same time we had to reconcile some differences with how plain HTML
;; elements work in Hiccup, vs how rendered components work. We went through a
;; few iterations, and finally realized that by making a clear distinction
;; between attributes and properties we could define behavior that is
;; consistent, explicit, and intuitive, while avoiding "magic" and a
;; proliferation of special cases.

;; While there are some particulars to be aware of, we hope the result will
;; generally be found to be intuitive, and to yield code that does not present
;; undue surprises to the reader.

^{:nextjournal.clerk/no-cache true
  :nextjournal.clerk/visibility #{:fold}}
(inline-styles)
