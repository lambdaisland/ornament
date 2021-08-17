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
com.lambdaisland/ornament {:mvn/version "0.0.0"}
```

or add the following to your `project.clj` ([Leiningen](https://leiningen.org/))

```
[com.lambdaisland/ornament "0.0.0"]
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

For ClojureScript you have two options, either you define all you components in
`cljc` files, and use the same approach as in Clojure. The alternative is to
first run your ClojureScript build, and then in the same process write out the
styles. You can for instance write your own script that invokes the
ClojureScript build API, then follows it up by writing out the styles, or you
can use something like Shadow-cljs build hooks.

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

### Garden Extensions

Ornament does a certain amount of pre-processing before passing the rules over
to Garden for compilation. This allows us to support some extra syntax which we
find more convenient.

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

## Customizing Girouette


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

Copyright &copy; 2020 Arne Brasseur and contributors

Available under the terms of the Eclipse Public License 1.0, see LICENSE.txt
<!-- /license -->
