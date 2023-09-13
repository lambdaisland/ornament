(ns ^{:ornament/prefix "ot"}
    lambdaisland.ornament-test
  (:require [lambdaisland.ornament :as o]
            [clojure.test :refer [deftest testing is are use-fixtures run-tests join-fixtures]]
            #?(:clj [lambdaisland.hiccup :as hiccup]
               :cljs [lambdaisland.thicc :as thicc]))
  #?(:cljs
     (:require-macros lambdaisland.ornament-test)))

(defn render [h]
  #?(:clj (hiccup/render h {:doctype? false})
     :cljs (.-outerHTML (thicc/dom h))))

(o/defstyled simple :span
  {:color "#ffffff"})

(o/defstyled tokens :span
  :px-5 :py-3 :rounded-xl)

(o/defstyled child-selector-tokens :div
  :pt-4
  :space-y-2)

(o/defstyled combined :span
  :px-5 :py-3 :rounded-xl
  {:color "azure"})

(o/defstyled nested :ul
  :px-3
  [:li {:list-style :square}])

(o/defstyled with-body :p
  :px-5 :py-3 :rounded-xl
  {:color "azure"}
  ([& children]
   (into [:strong] children)))

(o/defstyled with-body-derived with-body
  :font-bold)

(o/defstyled timed :time
  :border
  :border-black
  ([{:keys [date time]}]
   ^{:datetime (str date " " time)}
   [:<> date " " time]))

(o/defstyled ornament-in-ornament :div
  {:color "blue"}
  [simple {:color "red"}])

(o/defstyled base :span
  {:color "blue"
   :background-color "red"})

(o/defstyled inherited base
  {:color "green"
   :list-style :square})

(def my-tokens {:main-color "green"})

;; Referencing non-defstyled variables in rules is only possible in Clojure
(o/defstyled with-code :div
  {:background-color (-> my-tokens :main-color)})

;; TODO add assertions for these
(o/defstyled with-media :div
  {:padding "0 1rem 1rem"}
  [:at-media {:min-width "1rem"}
   {:grid-gap "1rem"
    :padding "0 2rem 2rem"}])

(o/defstyled with-css-fn :a
  [:&:after {:content [:str " (" [:cssfn :attr "href"] ")"]}])

(o/defstyled feature-check :div
  [:at-supports {:display "grid"}
   {:display "grid"}])

(o/defstyled color-fns :div
  {:color [:rgb 150 30 75]
   :background-color [:hsla 235 100 50 0.5]})

(o/defstyled nav-link :a
  ([{:keys [id]}]
   (let [{:keys [url title description]} {:url "/videos" :title "Videos" :description "Watch amazing videos"}]
     ^{:href url :title description}
     [:<> title])))

(o/defstyled referenced :div
  {:color :blue})

(o/defstyled referer :p
  [referenced {:color :red}] ;; use as classname
  [:.foo referenced]) ;; use as style rule

(o/defstyled siblings :p
  ;; Because of Tailwind/Girouette we can't always use the [:foo :bar {styles}]
  ;; garden syntax, instead we support using a set for this.
  [#{:span :div} {:color "red"}])

(o/defstyled siblings-plain :div
  ;; Plain garden version still works *if* it doesn't clash with anything
  ;; Girouette recognizes.
  [:ul :ol {:background-color "blue"}])

(o/defstyled attrs-in-fragment :div
  ([children]
   [:<> {:lang "nl"}
    children]))

(o/defstyled attrs-in-fragment-props :div
  ([{:keys [person]}]
   [:<> {:lang "nl"}
    "hello, " person]))

(o/defstyled attrs-in-fragment-styled :div
  ([{:keys [person]}]
   [:<> {:class "extra-class"
         :style {:color "blue"}}
    "hello, " person]))

(o/defstyled attrs-legacy :div
  ([{:keys [person]}]
   ^{:class "extra-class"
     :style {:color "blue"}}
   [:<> "hello, " person]))

;; Example from the README
(o/defstyled freebies-link :a
  {:font-size "1rem"
   :color "#cff9cf"
   :text-decoration "underline"})

;; For use in reagent, `::o/attrs` are still propagated to the element
(o/defstyled form-2 :div
  ([a]
   (fn [b]
     [:<> "hello"])))

;; Will fail to compile on cljs if the :require-macros line is missing
(o/defstyled with-str :div
  {:border (str "1px solid red")}
  ([props]
   [:<> "foo"]))


;; More ways to reuse styles across components
(o/defstyled bold :span
  :font-medium)

;; Referencing another component at the top level like this inherits its styles
(o/defstyled heading-1-top :h1
  bold :text-3xl)

;; Of course we can chain these
(o/defstyled heading-2-top :h2
  heading-1-top :text-2xl)

;; Doing this inside a `:&` is equivalent
(o/defstyled heading-1-nest :h1
  [:& bold :text-3xl])

(o/defstyled heading-2-nest :h2
  [:& heading-1-nest :text-2xl])


#?(:clj
   (deftest css-test
     (is (= ".ot__simple{color:#fff}"
            (o/css simple)))

     (is (= ".ot__tokens{padding-left:1.25rem;padding-right:1.25rem;padding-top:.75rem;padding-bottom:.75rem;border-radius:.75rem}"
            (o/css tokens)))

     (is (= ".ot__child_selector_tokens{padding-top:1rem}.ot__child_selector_tokens>:not([hidden])~:not([hidden]){margin-top:.5rem}"
            ;; This is what the Tailwind docs say should be output, but that's
            ;; not what Tailwind actually does, and so Girouette changed its
            ;; behavior to match Tailwind, see
            ;; https://github.com/green-coder/girouette/issues/84
            ;; ".ot__child_selector_tokens{padding-top:1rem}.ot__child_selector_tokens>*+*{margin-top:.5rem}"
            (o/css child-selector-tokens)))

     (is (= ".ot__combined{padding-left:1.25rem;padding-right:1.25rem;padding-top:.75rem;padding-bottom:.75rem;border-radius:.75rem;color:azure}"
            (o/css combined)))

     (is (= ".ot__nested{padding-left:.75rem;padding-right:.75rem}.ot__nested li{list-style:square}"
            (o/css nested)))

     (is (= ".ot__with_body{padding-left:1.25rem;padding-right:1.25rem;padding-top:.75rem;padding-bottom:.75rem;border-radius:.75rem;color:azure}"
            (o/css with-body)))

     (is (= ".ot__with_body_derived{padding-left:1.25rem;padding-right:1.25rem;padding-top:.75rem;padding-bottom:.75rem;border-radius:.75rem;color:azure;font-weight:700}"
            (o/css with-body-derived)))

     (is (= ".ot__ornament_in_ornament{color:blue}.ot__ornament_in_ornament .ot__simple{color:red}"
            (o/css ornament-in-ornament)))

     (is (= ".ot__inherited{color:green;background-color:red;list-style:square}"
            (o/css inherited)))

     #?(:clj
        (is (= ".ot__with_code{background-color:green}"
               (o/css with-code))))

     (is (= ".ot__with_media{padding:0 1rem 1rem}@media(min-width:1rem){.ot__with_media{grid-gap:1rem;padding:0 2rem 2rem}}"
            (o/css with-media)))

     (is (= ".ot__referer .ot__referenced{color:red}.ot__referer .foo{color:blue}"
            (o/css referer)))

     (is (= (o/css siblings)
            ".ot__siblings div,.ot__siblings span{color:red}"))

     (is (= ".ot__siblings_plain ul,.ot__siblings_plain ol{background-color:blue}"
            (o/css siblings-plain)))


     (is (= ".ot__heading_1_top{font-weight:500;font-size:1.875rem;line-height:2.25rem}"
            (o/css heading-1-top)))

     (is (= ".ot__heading_2_top{font-weight:500;font-size:1.5rem;line-height:2rem}"
            (o/css heading-2-top)))

     (is (= ".ot__heading_1_nest{font-weight:500;font-size:1.875rem;line-height:2.25rem}"
            (o/css heading-1-nest)))

     (is (= ".ot__heading_2_nest{font-weight:500;font-size:1.5rem;line-height:2rem}"
            (o/css heading-2-nest)))))

(deftest rendering-test
  (are [hiccup html] (= html (render hiccup))
    [simple]
    "<span class=\"ot__simple\"></span>"

    [simple {:class "xxx"}]
    "<span class=\"ot__simple xxx\"></span>"

    [simple {:class "xxx"} [:strong "child"]]
    "<span class=\"ot__simple xxx\"><strong>child</strong></span>"

    [simple {:class "xxx" :style {:border-bottom "1px solid black"}} [:strong "child"]]
    "<span class=\"ot__simple xxx\" style=\"border-bottom: 1px solid black;\"><strong>child</strong></span>"

    [timed {:date "2021-06-25" :time "10:11:12"}]
    "<time datetime=\"2021-06-25 10:11:12\" class=\"ot__timed\">2021-06-25 10:11:12</time>"

    [simple {:class timed}]
    "<span class=\"ot__simple ot__timed\"></span>"

    [simple {:class [timed]}]
    "<span class=\"ot__simple ot__timed\"></span>"

    [with-body "hello"]
    "<p class=\"ot__with_body\"><strong>hello</strong></p>"

    [with-body-derived "hello"]
    "<p class=\"ot__with_body_derived\"><strong>hello</strong></p>"

    ;; we're getting inconsistent but equivalent rendering here between clj and
    ;; cljs. Not ideal, but not a big deal either. Working around with reader
    ;; conditionals.
    ;; FIXME: write this in a more robust way, maintaining this is becoming a PITA
    [attrs-in-fragment "hello"]
    #?(:clj "<div lang=\"nl\" class=\"ot__attrs_in_fragment\">hello</div>"
       :cljs "<div class=\"ot__attrs_in_fragment\" lang=\"nl\">hello</div>")

    [attrs-in-fragment-props
     {:person "Arne"
      ::o/attrs {:lang "en" :title "greeting"}}]
    #?(:clj "<div lang=\"en\" title=\"greeting\" class=\"ot__attrs_in_fragment_props\">hello, Arne</div>"
       :cljs "<div title=\"greeting\" class=\"ot__attrs_in_fragment_props\" lang=\"en\">hello, Arne</div>")

    [attrs-in-fragment-props {:person "Jake"}]
    #?(:clj "<div lang=\"nl\" class=\"ot__attrs_in_fragment_props\">hello, Jake</div>"
       :cljs "<div class=\"ot__attrs_in_fragment_props\" lang=\"nl\">hello, Jake</div>")

    [attrs-in-fragment-styled {:person "Finn"}]
    "<div class=\"ot__attrs_in_fragment_styled extra-class\" style=\"color: blue;\">hello, Finn</div>"

    [attrs-in-fragment-styled {:person "Finn"
                               ::o/attrs {:class "extra2"
                                          :style {:background-color "rebeccapurple"}}}]
    #?(:clj "<div class=\"ot__attrs_in_fragment_styled extra-class extra2\" style=\"color: blue;\n  background-color: rebeccapurple;\">hello, Finn</div>"
       :cljs "<div class=\"ot__attrs_in_fragment_styled extra-class extra2\" style=\"color: blue; background-color: rebeccapurple;\">hello, Finn</div>")

    [attrs-legacy {:person "Arne"}]
    "<div class=\"ot__attrs_legacy extra-class\" style=\"color: blue;\">hello, Arne</div>"

    ;; ClojureScript bug, this does not currently work:
    ;; https://ask.clojure.org/index.php/11514/functions-with-metadata-can-not-take-more-than-20-arguments
    #_#_
    [simple
     [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a]
     [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a] [:a]
     [:a] [:a] [:a] [:a]]
    "<span class=\"ot__simple\"><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a><a></a></span>"
    [(form-2 7) {::o/attrs {:data-a 11}}]
    "<div data-a=\"11\" class=\"ot__form_2\">hello</div>"

    ))

(deftest direct-invocation-test
  (is (= [:a {:class ["ot__freebies_link"] :href "/episodes/interceptors-concepts"} "hello"]
         (freebies-link {:href "/episodes/interceptors-concepts"} "hello")))

  (is (= [:a {:class ["ot__freebies_link"]} "hello"]
         (freebies-link "hello"))))

(o/defstyled custok1 :div
  :bg-primary)

(o/defstyled custok2 :div
  :font-system)

(o/defstyled custok3 :div
  :full-center)

(o/defstyled custok4 :div
  :full-center-bis)

(o/defstyled custok5 :ul
  :bullets-🐻)

#?(:clj
   (deftest custom-tokens-test
     (o/set-tokens! {:colors {:primary "001122"}
                     :fonts {:system "-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji"}
                     :components [{:id :full-center
                                   :garden {:display "inline-flex"
                                            :align-items "center"}}
                                  {:id :full-center-bis
                                   :garden [:& :inline-flex :items-center]}
                                  {:id :custom-bullets
                                   :rules "custom-bullets = <'bullets-'> bullet-char
                                      <bullet-char> = #\".\""
                                   :garden (fn [{[bullet-char] :component-data}]
                                             [:&
                                              {:list-style "none"
                                               :padding 0
                                               :margin 0}
                                              [:li
                                               {:padding-left "1rem"
                                                :text-indent "-0.7rem"}]
                                              ["li::before"
                                               {:content bullet-char}]])}]})


     (is (= ".ot__custok1{--gi-bg-opacity:1;background-color:rgba(0,17,34,var(--gi-bg-opacity))}"
            (o/css custok1)))

     (is (= ".ot__custok2{font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji}"
            (o/css custok2)))

     (is (= ".ot__custok3{display:inline-flex;align-items:center}"
            (o/css custok3)))

     (is (= ".ot__custok4{display:inline-flex;align-items:center}"
            (o/css custok4)))

     (is (= ".ot__custok5{list-style:none;padding:0;margin:0}.ot__custok5 li{padding-left:1rem;text-indent:-0.7rem}.ot__custok5 li::before{content:🐻}"
            (o/css custok5)))

     (o/set-tokens! {})))

#?(:clj
   (deftest meta-merge-tokens-test
     ;; establish baseline
     (is (= {:--gi-bg-opacity 1, :background-color "rgba(239,68,68,var(--gi-bg-opacity))"}
            (o/process-rule :bg-red-500)))

     (is (= {:font-family "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace"}
            (o/process-rule :font-mono)))

     (is (= {:border-radius "0.75rem"}
            (o/process-rule :rounded-xl)))

     ;; Replace the default colors/fonts, leave the components so we can still do bg-* or font-*
     (o/set-tokens! {:colors ^:replace {:primary "001122"}
                     :fonts ^:replace {:system "-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji"}})

     ;; The built-in ones are all gone, they expand to selectors now
     (is (= :bg-red-500 (o/process-rule :bg-red-500)))
     (is (= :font-mono (o/process-rule :font-mono)))

     (is {:--gi-bg-opacity 1, :background-color "rgba(0,17,34,var(--gi-bg-opacity))"}
         (o/process-rule :bg-primary))

     (is {:font-family "-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji"}
         (o/process-rule :font-system))


     ;; Replace the components
     (o/set-tokens! {:components ^:replace [{:id :full-center
                                             :garden {:display "inline-flex"
                                                      :align-items "center"}}]})

     (is (= {:display "inline-flex", :align-items "center"}
            (o/process-rule :full-center)))

     (is (= :rounded-xl (o/process-rule :rounded-xl)))

     ;; Reset to defaults
     (o/set-tokens! {})))

#?(:clj
   (deftest defined-styles-test
     (let [reg @o/registry]
       (reset! o/registry {})

       ;; Deal with the fact that the registry is populated at compile time
       (eval
        `(do
           (in-ns '~(symbol (namespace `_)))
           (o/defstyled ~'my-styles :div
             {:color "red"})
           (o/defstyled ~'more-styles :span
             :rounded-xl)))

       (is (= ".ot__my_styles{color:red}\n.ot__more_styles{border-radius:.75rem}"
              (o/defined-styles)))

       (reset! o/registry reg))))

(comment
  (require 'kaocha.repl)
  (kaocha.repl/run)
  )
