# Ornament

<!-- badges -->
[![cljdoc badge](https://cljdoc.org/badge/com.lambdaisland/ornament)](https://cljdoc.org/d/com.lambdaisland/ornament) [![Clojars Project](https://img.shields.io/clojars/v/com.lambdaisland/ornament.svg)](https://clojars.org/com.lambdaisland/ornament)
<!-- /badges -->

CSS-in-Clj(s)

## Features

- Define styled components
- Use Garden syntax for CSS, or Girouette (Tailwind-style) utility class names
- Have styling live close to, but separate from DOM structure
- Compile to plain CSS files as a compile step
- Use components in any Hiccup implementation, frontend or backend

<!-- installation -->
## Installation

To use the latest release, add the following to your `deps.edn` ([Clojure CLI](https://clojure.org/guides/deps_and_cli))

```
com.lambdaisland/ornament {:mvn/version "0.2.19"}
```

or add the following to your `project.clj` ([Leiningen](https://leiningen.org/))

```
[com.lambdaisland/ornament "0.2.19"]
```
<!-- /installation -->

## Introduction

Ornament is the culmination of many discussions and explorations with the aim to
find the sweet spot in how to handle styling in large Clojure or ClojureScript
web projects. It takes ideas from CSS-in-JS approaches, and utility-class
libraries, while (in our opinion) improving on both.

At the heart of ornament is the `defstyled` macro, which defines a "styled
component". It combines a name, a HTML tag, and styling rules.

```clojure
(require '[lambdaisland.ornament :as o])

(o/defstyled freebies-link :a
  {:font-size "1rem"
   :color "#cff9cf"
   :text-decoration "underline"})
```

This does two things, first of all it creates a Hiccup component, which combines
the tag (`:a` in this case), with a class name based on the component name.

```clojure
;; Hiccup
[freebies-link {:href "/episodes/interceptors-concepts} "Freebies"]
```

Which renders as:

```html
<a class="lambdaisland_episodes__freebies_link">Freebies</a>
```

The styling information is rendered during a build step to CSS, and written out,
so it gets served as any other plain CSS file.

```clojure
(spit "resource/public/ornament.css" (o/defined-styles))
```

```css
.lambdaisland_episodes__freebies_link {
  font-size: 1rem;
  color: #cff9cf;
  text-decoration: underline;
}
```

If you prefer to use [Girouette](https://github.com/green-coder/girouette)
(Tailwind) utility classes (a.k.a. tokens), then you can do that as well, or you
can mix and match.

```clojure
(o/defstyled freebies-link :a
  :text-base
  :text-green-500
  :underline)
```

Finally you can add one or more function bodies to your component, which acts as
a render function, determining how the children of the component will render.
Note that this render function only determines the "inside" of the component, it
will still get wrapped with the tag and class name passed to `defstyled`.

```clojure
(o/defstyled page-grid :div
  :relative :h-full :md:flex
  [:>.content :flex-1 :p-2 :md:p-10 :bg-gray-100]
  ([sidebar content]
   [:<>
    sidebar
    [:div.content
     content]]))
```

Hopefully these examples have sufficiently whetted your appetite. We'll explain
the syntax and features of `defstyled` in detail down below. But first we need
to explain Ornament's philosophy on how to deal with CSS, to give you accurate
expectations of how it will behave. This is especially relevant for
ClojureScript projects.

## Ornament CSS Compilation

Ornament is written in CLJC, meaning you can call `defstyled` in ClojureScript
exactly the same way as in Clojure. But, there's one important distinction, we
do not add any styling information (be it Garden, Girouette, or CSS), to your
ClojureScript build.

This is an important design decision, and it's worth elaborating a bit. We
understand the appeal of something like CSS-in-JS from a _syntax and programmer
convenience_ point of view, and we try to offer the same kind of convenience.
However, we question that it is sensible to deal with CSS generation in
JavaScript. We think it's vastly superior to generate CSS once at build/deploy
time, and to then deal with it *as CSS*.

This pays dividends, we don't need to add all the machinery of Garden and
Girouette to the frontend build, neither do we need all the styling definitions,
so Ornament will have minimal impact on your bundle size. And your CSS can be
served — and cached — as a simple static CSS file.

What you do get from using `defstyled` in ClojureScript is a component
(function) which you can use in Hiccup (e.g. Reagent) to render HTML. The
component knows about the HTML tag to use, the CSS class name to add, and if you
added a render function, it knows about that as well.

So where does the styling go? We add that to a registry *during macroexpansion*.
In other words: in Clojure. Once all `defstyled` invocations have been compiled,
you can grab the full CSS with `(o/defined-styles)`, and spit it to a file.

Before you grab the output of `(o/defined-styles)` you need to make sure all
your styles have in fact been defined. For Clojure projects this merely means
requiring all namespaces. If you have some kind of main application entry point
that loads all your components/views, then load that, and capture the styles
once it has finished.

### ClojureScript

For ClojureScript you have two options, either you define all you components in
`cljc` files, and use the same approach as in Clojure. The alternative is to
first run your ClojureScript build, and then in the same process write out the
styles. You can for instance write your own script that invokes the
ClojureScript build API, then follows it up by writing out the styles, or you
can use something like Shadow-cljs build hooks.

#### Limitations

Keep in mind that the style (Garden/CSS) section of a component is only ever
processed in Clojure, even when used in ClojureScript files. This means that it
is not possible to reference ClojureScript variables or functions.

```clojure
(def sizes {:s "0.5rem" :m "1.rem" :l "2rem"})

(o/defstyled foo :div
  {:padding (:m sizes)})
```

This will work in Clojure, but not ClojureScript. Referencing
variables/functions inside the component body is not a problem.

```clojure
(def divider [:hr])

(o/defstyled dividers :div
  {:padding "1rem"}
  ([& children]
   (into [:<>]
         (interpose divider)
         children)))
```

This works in both Clojure and ClojureScript.

Note that it *is* possible to reference previously defined `defstyled`
components in the style rules section, even in ClojureScript, see the section
"Referencing other components in Rules" below.

```clojure
(o/defstyled referenced :div
  {:color :blue})

(o/defstyled referer :p
  [referenced {:color :red}] ;; use as classname
  [:.foo referenced]) ;; use as style rule
```

#### Shadow-cljs build hook example

This is enough to get recompilation of your styles to CSS, which shadow-cljs
will then hot-reload.

```clojure
;; Easiest to just make this a clj file.
(ns my.hooks
  (:require [lambdaisland.ornament :as o]
            [garden.compiler :as gc]
            [girouette.tw.preflight :as girouette-preflight]))

;; Optional, but it's common to still have some style rules that are not
;; component-specific, so you can use Garden directly for that
(def global-styles
  [[:html {:font-size "14pt"}]])

(defn write-styles-hook
  {:shadow.build/stage :flush}
  [build-state & args]
  ;; In case your global-styles is in a separate clj file you will have to
  ;; reload it yourself, shadow only reloads/recompiles cljs/cljc files
  #_(require my.styles :reload)
  ;; Just writing out the CSS is enough, shadow will pick it up (make sure you
  ;; have a <link href=styles.css rel=stylesheet>)
  (spit "resources/public/styles.css"
        (str
         ;; `defined-styles` takes a :preflight? flag, but we like to have some
         ;; style rules between the preflight and the components. This whole bit
         ;; is optional.
         (gc/compile-css (concat
                          girouette-preflight/preflight
                          styles/global-styles))
         "\n"
         (o/defined-styles)))
  build-state)
```

```clojure
;; shadow-cljs.edn
{,,,

 ;; For best results, otherwise you will find that some styles are missing after
 ;; restarting the shadow process
 :cache-blockers #{lambdaisland.ornament}

 :builds
 {:main
  {:target     :browser
   ,,,
   :build-hooks [(my.hooks/write-styles-hook)]}}}
```

## Defstyled Component Syntax

`defstyled` really does two things, the macro expands to a form like

```clojure
(def footer (reify StyledComponent ...))
```

This "styled component" acts as a function, which is what makes it compatible
with Hiccup implementations.

```clojure
(footer "hello")
;;=>
[:footer {:class ["project-discovery_parts__footer"]} "hello"]
```

It also implements various protocol methods. You don't typically need to call
these yourself, but they can be useful for verifying how your component behaves.

```clojure
(o/classname footer)  
;;=> "project-discovery_parts__footer"
(o/tag footer)
;;=> :footer
(o/rules footer)
;;=> [{:max-width "60rem"}]
(o/as-garden footer)
;;=> [".project-discovery_parts__footer" {:max-width "60rem"}]
(o/css footer)
;;=> ".project-discovery_parts__footer{max-width:60rem}"
```

### Component Name

The first argument to `defstyled` is the component name, this will create a var
with the given name, containing the component. A function-like object that can
be used to render HTML.

The name will also determine the class name that will be used in the HTML and
CSS. For this Ornament combines the namespace name with the component name, and
munges them to be valid CSS identifiers.

```clojure
(ns my.views
 (:require [lambdaisland.ornament :as o]))

(o/defstyled footer :footer
  {:max-width "60rem"})
  
(o/classname footer)
;; my_views__footer
```

You can use metadata on the namespace to change the namespace prefix.

``` clojure
(ns ^{:ornament/prefix "views"} com.company.project.frontend.views
 (:require [lambdaisland.ornament :as o]))

(o/defstyled footer :footer
  {:max-width "60rem"})
  
(o/classname footer)
;; views__footer
```

Using fully qualified var names as class names provides the unexpected benefit
that it becomes trivial to find the component you are looking at in your
browser's inspector.

When stringifying the component you also get the class name back, this allows
using them to reference a certain class, for instance in Hiccup:

```clojure
[:div {:class (str footer)} ...]

;; Depending on your Hiccup implementation this can also work
[:div {:class [footer]} ...]

(js/querySelector (str footer))
```

### HTML Tag

The second argument to `defstyled` is the HTML tag. This is typically a keyword,
like `:section`, `:tr`, or `:div`. This is used when rendering the component as
Hiccup, and so anything that is valid in your Hiccup implementation of choice is
fair game, including `:div#some-id` or `:p.a_class`. You could also use for
instance a Reagent component, assuming it correctly handles receiving a
properties map as its first argument. You can't use `:<>` as the tag, since we
can't add a class name to a fragment.

As a special case you can use another styled component as the tag.

```clojure
(defstyled about-footer footer
  ,,,)
```

This will cause the new component to "inherit" both the HTML tag used by the
referenced componet, and any CSS rules it defines.

### Rules

After the name and tag, `defstyled` takes one or more "rules". These can be
maps, vectors, or keywords.

Maps and vectors are handled by [Garden](https://github.com/noprompt/garden),
and we recommend reading the Garden documentation and getting familiar with the
syntax. Maps define CSS styles as demonstrated before, with vectors you can
apply styles to descendant elements, or handle pseudo-elements.

```clojure
(in-ns 'my-nav)

(o/defstyled menu :nav
  {:padding "2rem"}
  [:a {:color "blue"}]
  [:&:hover {:background-color "#888"}])
 
;; Inspect the result
(o/css menu)
```

Result:

```css
.my_nav__menu{padding:2rem}
.my_nav__menu a{color:blue}
.my_nav__menu:hover{background-color:#888}
```

Keywords are handled by [Girouette](https://github.com/green-coder/girouette).
Girouette uses a grammar to parse utility class names like `:text-green-500`,
and converting the result to Garden syntax. Out of the box it supports all the
same names that Tailwind provides, but you can define custom rules, or adjust
the color palette. (See [Customizing Girouette](#customizing-girouette)).

Note that you can mix and match these. You should be able to use a Girouette
keyword anywhere where you would use a Garden properties map.

#### Referencing other components in Rules

You can use a previously defined `defstyled` component either as a selector, or
as a style rule.

Consider this "call to action" button.

```clojure
(o/defstyled cta :button
  {:background-color "red"})
```

You might use it as part of another component, and add additional styling for
that context.

```clojure
(o/defstyled buy-now-section :div
  [cta {:padding "2rem"}]
  ([]
   [:<>
    [:p "The best widgest in the world"]
    [cta {:value "Buy now!"}]]))
```

Here `cta` is a shorthand for writing the full Ornament class name of the
component. Now the `cta` button will get some extra padding in this context, in
addition to its red background.

You can also use `cta` as a reusable group of styles. In this case we wont to
style the `:a` element with the `cta` styles.

```clojure
(o/defstyled pricing-link :span
  [:a cta]
  ([]
   [:a {:href "/pricing"} "Pricing"]))
```

This kind of referencing previously defined components works both in Clojure and
ClojureScript, even though in ClojureScript usage you can't normally reference
vars inside your style declaration. To make these work we resolve these symbols
during compilation based on Ornament's registry of components.

## Render functions

After the component name, tag, and CSS rules, you can optionally put one or more
render functions, consisting of an argument vector, and the function body.

```clojure
(o/defstyled with-body :p
  :px-5 :py-3 :rounded-xl
  {:color "azure"}
  ([& children]
   (into [:strong] children)))
   
[with-body "hello"]
;;=>
"<p class=\"ot__with_body\"><strong>hello</strong></p>"
```

You can put multiple of these to deal with multiple arities

```clojure
(o/defstyled multi-arity :p
  ([arg1]
   [:strong arg1])
  ([arg1 arg2]
   [:<>
    [:strong arg1] [:em arg2]]))
```

Without render functions a styled component works almost like a plain HTML tag
when using in Hiccup: the first argument, if it's a map, is treated as a map of
HTML attributes, any following arguments are treated as children.

When you supply your own render function this behavior changes. All arguments
are passed to the render function to determine the children of the styled
component. If the first argument is a map, then the `:class`, `:id`, and
`:style` elements are added to the outer component (they are still passed to the
render function as well).

The rationale is that when using a styled component in your Hiccup, it should be
straightforward to add an extra class or inline styling to the component. We
don't want to break that use case. But we don't want to treat the map in the
first argument as only consisting of HTML attributes in this case, since you may
use that map to pass arbitrary values to the render function. So we lift out
`:class`, `:id` and `:style`, and ignore the rest.

```clojure
(o/defstyled videos :section
  ([{:keys [videos]}]
   (into [:<>] (map #(do [video %]) videos))))
```

```clojure
[videos {:videos (fetch-videos} :id "main-listing"}]
```

It is still possible to set extra HTML attributes on the component in this case,
but it has to be done from *inside the render function*, through metadata on the
return value.

```clojure
(o/defstyled nav-link :a
  ([{:keys [id]}]
   (let [{:keys [url title description]} (get-route id)]
     ^{:href url :title description}
     [:<> title])))

;;=>
<a href="/videos" title="Watch amazing videos" class="ot__nav_link">Videos</a>
```

## Differences from Garden

The rules section of a component is essentially
[Garden](https://github.com/noprompt/garden) syntax. We run it through the
Garden compiler, and so things that work in Garden generally work there as well,
with some exceptions.

Keywords that come first inside a vector are always treated as CSS selectors, as
you would expect, but if they occur elsewhere then we first pass them to
Girouette to expand to style rules class names. If Girouette does not recognize
the keyword as a classname, then it's preserved in the Garden as-is.

That means that generally things work as expected, since selectors and Girouette
classes don't have much overlap.

```clojure
;; ✔️ :ol is recognized as a selector

(o/defstyled list-wrapper :div
  [:ul :ol {:background-color "blue"}])

(o/css list-wrapper)
;; => ".ot__list_wrapper ul,.ot__list_wrapper ol{background-color:blue}"

;; ✔️ :bg-blue-500 is recognized as a utility class

(o/defstyled list-wrapper :div
  [:ul :bg-blue-500])

(o/css list-wrapper)
;; => ".ot__list_wrapper ul{--gi-bg-opacity:1;background-color:rgba(59,130,246,var(--gi-bg-opacity))}"
```

But there is some potential for clashes, e.g. Girouette has a `:table` class.

```clojure
;; ❌ not what we wanted

(o/defstyled fig-wrapper :div
  [:figure :table {:padding "1rem"}])
  
(o/css fig-wrapper)
;; => ".ot__fig_wrapper figure{display:table;padding:1rem}"
```

Instead use a set to make it explicit that these are multiple selectors. It's
good practice to do this in general since it is more explicit and reduces
ambiguity and chance of clashes.

```clojure
(o/defstyled fig-wrapper :div
  [#{:figure :table} {:padding "1rem"}])

(o/css fig-wrapper)
;; => ".ot__fig_wrapper figure,.ot__fig_wrapper table{padding:1rem}"
```

### Garden Extensions

Ornament does a certain amount of pre-processing before passing the rules over
to Garden for compilation. This allows us to support some extra syntax which we
find more convenient.

### Special "tags"

Use these as the first element in a vector to opt into special handling. Some of
these are used where a selector would be used, others are helpers for defining
property values.

- `:at-media`

You can add breakpoints for responsiveness to your components with `:at-media`.

```clojure
(o/defstyled eps-container :div
  {:display "grid"
   :grid-gap "1rem"
   :grid-template-columns "repeat(auto-fill, minmax(20rem, 1fr))"
   :padding "0 1rem 1rem"}
  [:at-media {:min-width "40rem"}
   {:grid-gap "2rem"
    :padding "0 2rem 2rem"}])
```

- `:cssfn`

CSS functions can be invoked with `:cssfn`

```clojure
(o/defstyled with-css-fn :a
  [:&:after {:content [:cssfn :attr "href"]}])
```

- `:at-supports`

Support for feature tests via `@supports`

```clojure
(o/defstyled feature-check :div
  [:at-supports {:display "grid"}
   {:display "grid"}])
```

- `:rgb` / `:hsl` / `:rgba` / `:hsla`

Shorthands for color functions

```clojure
(o/defstyled color-fns :div
  {:color [:rgb 150 30 75]
   :background-color [:hsla 235 100 50 0.5]})

(o/css color-fns)
;;=>
".ot__color_fns{color:#961e4b;background-color:hsla(235,100%,50%,0.5)}"
```

- `:str`

Turns any strings into quoted strings, for cases where you need to put string content in your CSS.

```clojure
(o/defstyled with-css-fn :a
  [:&:after {:content [:str " (" [:cssfn :attr "href"] ")"]}])
```

#### Special property handling

Some property names we recognize and treat special, mainly to make it less
tedious to define composite values.

- `:grid-area` / `:border` / `:margin` / `:padding`

Treat vector values as space-separated lists, e.g. `:padding [10 0 15 0]`.
Non-vector values are passed on unchanged.

- `:grid-template-areas`

Use nested vectors to define the areas

```clojure
   :grid-template-areas [["title"      "title"      "user"]
                         ["controlbar" "controlbar" "controlbar"]
                         ["...."       "...."       "...."]
                         ["...."       "...."       "...."]
                         ["...."       "...."       "...."]
```

## Customizing Girouette

Girouette is highly customizable. Out of the box it supports the same classes as
Tailwind does, but you can customize the colors, fonts, or add completely new
rules for recognizing class name.

The `girouette-api` atom contains the result of `giroutte/make-api`. By
replacing it you can customize how keywords are expanded to Garden. We provide a
`set-tokens!` function which makes the common cases straightforward. This
configures Girouette, so that these tokens become available inside Ornament
style declarations.

`set-tokens!` takes a map with these (optional) keys:

- `:colors` : map from keyword to 6-digit hex color, without leading `#`
- `:fonts`: map from keyword to font stack (comman separated string)
- `:components`: sequence of Girouette components, each a map with `:id`
  (keyword), `:rules` (string, instaparse, can be omitted), and `:garden` (map,
  or function taking instaparse results and returning Garden map)


```clojure
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
                                         ["li:before"
                                          {:content bullet-char}]])}]})
```

Let's go over these. Colors is straightforward, it introduces a new color name,
so now I can use classes like `:text-primary-500` or `:bg-primary`.

Fonts provide the `:font-<name>` class, so in this case `:font-system`.

With custom components there's a lot you can do. The first one here,
`:full-center`, only has a `:garden` key, which has plain data as its value.
This basically provides an alias or shorthand, so we can use `:full-center` in
place of `{:display "inline-flex" :align-items "center"}`. The second one,
`:full-center-bis` is essentially the same, but we've used other Girouette
classes. Just as in `defstyled` you can use those too.

The third one introduces a completely custom rule. It has a `:rules` key, which
gets a string using Instaparse grammar syntax. Here we're definining a grammar
which will recognize any classname starting with "bullets-" and followed by a
single character.

If `:rules` is omitted we assume this is a static token, and we'll generate a
rule of the form `token-id = <'token-id'>`. That's what happens with the first
two components.

In this case the `:garden` key gets a function, which receives the parse
information (under the `:component-data` key), and can use it to build up the
Garden styling. Notice that we're using the "bullet-char" that we parsed out of
the class name, to set the `:content` on `:li:before`.

The end result is that we can do something like this:

```clojure
(o/defstyled bear-list :ul
  :bullets-🐻)
  
[bear-list
 [:li "Black"]
 [:li "Formosan"]]
```

And get a bullet list which uses bear emojis for the bullets.

`set-tokens` will add the new colors, fonts, and components to the defaults that
Girouette provides. You can change that by adding a `^:replace` tag (this uses
meta-merge). e.g. `{:colors ^:replace {...}}`) 

<!-- opencollective -->
## Lambda Island Open Source

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

ornament is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our Open Collective](http://opencollective.com/lambda-island),
so that we may continue to enjoy a thriving Clojure ecosystem.

You can find an overview of our projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

<!-- contributing -->
## Contributing

Everyone has a right to submit patches to ornament, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as EPL 1.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license -->
## License

Copyright &copy; 2021-2022 Arne Brasseur and contributors

Available under the terms of the Eclipse Public License 1.0, see LICENSE.txt
<!-- /license -->
