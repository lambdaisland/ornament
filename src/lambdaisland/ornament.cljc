(ns lambdaisland.ornament
  "CSS-in-clj(s)"
<<<<<<< Updated upstream
  #?@
  (:clj
   [(:require
     [clojure.string :as str]
     [clojure.walk :as walk]
     [garden.color :as gcolor]
     [garden.compiler :as gc]
     [garden.stylesheet :as gs]
     [garden.types :as gt]
     [garden.util :as gu]
     [girouette.tw.color :as girouette-color]
     [girouette.tw.core :as girouette]
     [girouette.tw.default-api :as girouette-default]
     [girouette.tw.preflight :as girouette-preflight]
     [girouette.tw.typography :as girouette-typography]
     [girouette.version :as girouette-version]
     [meta-merge.core :as meta-merge])]
   :cljs
   [(:require [clojure.string :as str] [garden.util :as gu])]))
||||||| Stash base
  (:require [clojure.string :as str]
            [meta-merge.core :as meta-merge]
            #?@(:clj [[clojure.walk :as walk]
                      [garden.compiler :as gc]
                      [garden.core :as garden]
                      [garden.color :as gcolor]
                      [garden.types :as gt]
                      [garden.stylesheet :as gs]
                      [girouette.version :as girouette-version]
                      [girouette.tw.core :as girouette]
                      [girouette.tw.preflight :as girouette-preflight]
                      [girouette.tw.typography :as girouette-typography]
                      [girouette.tw.color :as girouette-color]
                      [girouette.tw.default-api :as girouette-default]]))
  #?(:cljs
     (:require-macros [lambdaisland.ornament :refer [defstyled]])))
=======
  (:require [clojure.string :as str]
            [meta-merge.core :as meta-merge]
            #?@(:clj [[clojure.walk :as walk]
                      [garden.compiler :as gc]
                      [garden.core :as garden]
                      [garden.color :as gcolor]
                      [garden.types :as gt]
                      [garden.stylesheet :as gs]
                      [girouette.version :as girouette-version]
                      [girouette.tw.core :as girouette]
                      [girouette.tw.preflight :as girouette-preflight]
                      [girouette.tw.typography :as girouette-typography]
                      [girouette.tw.color :as girouette-color]]))
  #?(:cljs
     (:require-macros [lambdaisland.ornament :refer [defstyled]])))
>>>>>>> Stashed changes

#?(:clj
   (defonce ^{:doc "Registry of styled components

     Keys are fully qualified symbols (var names), values are maps with the
     individual `:tag`, `:rules`, `:classname`. We add an `:index` to be able to
     iterate over the components/styles in source order. This is now the
     preferred way to iterate over all styles (as in [[defined-styles]]), rather
     than the old approach of finding all vars with a given metadata attached to
     them.

     Clojure-only because we only deal with CSS on the backend, the frontend
     only knows about classnames. `:component` points at a StyledComponent
     instance that can be used to get the [[css]] for that component."}
     registry
     (atom {})))

#?(:clj
   (defonce ^{:doc "Registry of plain CSS (Garden) rules"}
     rules-registry
     (atom {})))

#?(:clj
   (defonce ^{:doc "Registry of custom properties"}
     props-registry
     (atom {})))

(def ^:dynamic *strip-prefixes*
  "Prefixes to be stripped from class names in generated CSS"
  nil)

(defprotocol StyledComponent
  (classname [_]
    "The CSS class name for this component, derived from the var and ns name.")
  (as-garden [_]
    "Return the styles for this component in Garden syntax (i.e. EDN data)")
  (css [_]
    "Compile this component's styles to CSS")
  (rules [_]
    "Get the rules passed to this component, without any processing.")
  (tag [_]
    "HTML tag (keyword) for this component")
  (component [_]
    "Function which is a Hiccup component, for styled components which have one or more function tails.")
  (as-hiccup [_ args]
    "Render to hiccup"))

(declare process-rule)

#?(:clj
   (do
     (defonce ^{:doc "Atom containing the return value
       of [[girouette/make-api]], making it possible to swap this out for your
       own Girouette instance. See also [[set-tokens!]] for a convenient API for
       common use cases."}
       girouette-api
       (atom nil))

     (def default-tokens-v2
       (delay
         {:components (-> @(requiring-resolve 'girouette.tw.default-api/all-tw-components)
                          (girouette-version/filter-components-by-version [:tw 2]))
          :colors     girouette-color/tw-v2-colors
          :fonts      girouette-typography/tw-v2-font-family-map}))

     (def default-tokens-v3
       (delay
         {:components (-> @(requiring-resolve 'girouette.tw.default-api/all-tw-components)
                          (girouette-version/filter-components-by-version [:tw 3]))
          :colors     girouette-color/tw-v3-unified-colors-extended
          :fonts      girouette-typography/tw-v2-font-family-map}))

     (def default-tokens default-tokens-v2)

     (defn set-tokens!
       "Set \"design tokens\": colors, fonts, and components

        This configures Girouette, so that these tokens become available inside
        Ornament style declarations.

        - `:colors` : map from keyword to 6-digit hex color, without leading `#`
        - `:fonts`: map from keyword to font stack (comman separated string)
        - `:components`: sequence of Girouette components, each a map with
          `:id` (keyword), `:rules` (string, instaparse, can be omitted), and
          `:garden` (map, or function taking instaparse results and returning Garden
          map)
        - `:tw-version`: which Girouette defaults to use, either based on Tailwind
          v2, or v3. Valid values: 2, 3.

        If `:rules` is omitted we assume this is a static token, and we'll
        generate a rule of the form `token-id = <'token-id'>`.

        `:garden` can be a function, in which case it receives a map with a
        `:compoent-data` key containing the instaparse parse tree. Literal maps or
        vectors are wrapped in a function, in case the returned Garden is fixed. The
        resulting Garden styles are processed again as in `defstyled`, so you can use
        other Girouette or other tokens in there as well. Use `[:&]` for returning
        multiple tokens/maps/stylesUse `[:&]` for returning multiple
        tokens/maps/styles.

        By default these are added to the Girouette defaults, which are in terms
        based on the Tailwind defaults. We still default to v2 (to avoid breaking
        changes), but you can opt-in to Tailwind v3 by adding `:tw-version 3`. Use
        meta-merge annotations (e.g. `{:colors ^:replace {...}}`) to change that
        behaviour."
       [{:keys [components colors fonts tw-version]
         :or {tw-version 2}}]
       (let [{:keys [components colors fonts]}
             (meta-merge/meta-merge
              (case tw-version
                2 @default-tokens-v2
                3 @default-tokens-v3)
              {:components
               (into (empty components)
                     (map (fn [{:keys [id rules garden] :as c}]
                            (cond-> c
                              (not rules)
                              (assoc :rules (str "\n" (name id) " = <'" (name id) "'>" "\n"))
                              (not (fn? garden))
                              (assoc :garden (constantly garden))

                              :always
                              (update :garden #(comp process-rule %)))))
                     (flatten components))
               :colors (into (empty colors)
                             (map (juxt (comp name key) val))
                             colors)
               :fonts (into (empty fonts)
                            (map (juxt (comp name key) val))
                            fonts)})]
         (reset! girouette-api
                 (girouette/make-api
                  components
                  {:color-map colors
                   :font-family-map fonts}))))

     (defonce set-default-tokens (set-tokens! nil))

     (defn class-name->garden [n]
       ((:class-name->garden @girouette-api) n))

     (defmethod print-method ::styled [x writer]
       (.write writer (classname x)))

     (def munge-map
       {\@ "_CIRCA_"
        \! "_BANG_"
        \# "_SHARP_"
        \% "_PERCENT_"
        \& "_AMPERSAND_"
        \' "_SINGLEQUOTE_"
        \* "_STAR_"
        \+ "_PLUS_"
        \- "_"
        \/ "_SLASH_"
        \: "_COLON_"
        \[ "_LBRACK_"
        \{ "_LBRACE_"
        \< "_LT_"
        \\ "_BSLASH_"
        \| "_BAR_"
        \= "_EQ_"
        \] "_RBRACK_"
        \} "_RBRACE_"
        \> "_GT_"
        \^ "_CARET_"
        \~ "_TILDE_"
        \? "_QMARK_"})

     (defn munge-str
       ([s]
        (munge-str s munge-map))
       ([s munge-map]
        #?(:clj
           (let [sb (StringBuilder.)]
             (doseq [ch s]
               (if-let [repl (get munge-map ch)]
                 (.append sb repl)
                 (.append sb ch)))
             (str sb))
           :cljs
           (apply str (map #(get munge-map % %) s)))))

     (defn classname-for
       "Convert a fully qualified symbol into a CSS classname

  Munges special characters, and honors `:ornament/prefix` metadata on the
  namespace."
       [varsym]
       (let [prefix (or (:ornament/prefix (meta (the-ns (symbol (namespace varsym)))))
                        (-> varsym
                            namespace
                            (str/replace #"\." "_")
                            (str "__")))]
         (str prefix (munge-str (name varsym)))))

     (defn join-vector-by [sep val]
       (if (vector? val)
         (str/join sep val)
         val))

     (defmulti process-tag
       "Support some of our Garden extensions

  Convert tagged vectors in the component rules into plain Garden, e.g.
  `[:at-media]` or `[:rgb]`. Default implementation handles using styled
  components as selectors, or otherwise simply preserves the tag."
       (fn [[tag & _]] tag))

     (defmethod process-tag :default [v]
       (let [tag (first v)]
         (into (if (set? tag)
                 (into [] tag)
                 [(cond
                    (= ::styled (type tag))
                    (str "." (classname tag))
                    (sequential? tag)
                    (process-rule tag)
                    :else
                    tag)])
               (map process-rule (next v)))))

     (defmethod process-tag :at-media [[_ media-queries & rules]]
       (gs/at-media media-queries (into [:&] (map process-rule) rules)))

     (defmethod process-tag :cssfn [[_ fn-name & args]]
       (gt/->CSSFunction fn-name args))

     (defmethod process-tag :at-supports [[_ feature-queries & rules]]
       (gt/->CSSAtRule
        :feature
        {:feature-queries feature-queries
         :rules           (list (into [:&] (map (comp process-rule)) rules))}))

     (defmethod process-tag :rgb [[_ r g b]]
       (gcolor/rgb [r g b]))

     (defmethod process-tag :hsl [[_ h s l]]
       (gcolor/hsl [h s l]))

     (defmethod process-tag :rgba [[_ r g b a]]
       (gcolor/rgba [r g b a]))

     (defmethod process-tag :hsla [[_ h s l a]]
       (gcolor/hsla [h s l a]))

     (defmethod process-tag :str [[_ & xs]]
       [(map #(if (string? %) (pr-str %) (process-rule %)) xs)])

     (defmulti process-property
       "Special handling of certain CSS properties. E.g. setting `:grid-template-areas`
  using a vector."
       (fn [prop val] prop))

     (defmethod process-property :default [_ val]
       (if (vector? val)
         (process-tag val)
         val))

     (defmethod process-property :grid-template-areas [_ val]
       (if (vector? val)
         (str/join " "
                   (map (fn [row]
                          (pr-str (str/join " " (map name row))))
                        val))
         val))

     (defmethod process-property :grid-area [_ val] (join-vector-by " / " val))
     (defmethod process-property :border [_ val] (join-vector-by " " val))
     (defmethod process-property :margin [_ val] (join-vector-by " " val))
     (defmethod process-property :padding [_ val] (join-vector-by " " val))

     (defn process-rule
       "Process a single \"rule\" into plain Garden

  Components receive a list of rules. These can be Garden-style maps,
  Girouette-style keywords, or Garden-style vectors of selectors+rules. This
  function together with [[process-tag]] and [[process-property]] defines the
  recursive logic to turn this into something we can pass to the Garden
  compiler."
       [rule]
       (cond
         (record? rule) ; Prevent some defrecords in garden.types to be fudged
         rule

         (simple-keyword? rule)
         (let [girouette-garden (class-name->garden (name rule))]
           (cond
             (nil? girouette-garden)
             #_(throw (ex-info "Girouette style expansion failed" {:rule rule}))
             rule

             (and (record? girouette-garden)
                  (= (:identifier girouette-garden) :media))
             (-> girouette-garden
                 (update-in [:value :rules] (fn [rules]
                                              (map #(into [:&] (rest %)) rules))))
             :else
             (second girouette-garden)))

         (map? rule)
         (into {} (map (fn [[k v]] [k (process-property k v)])) rule)

         (vector? rule)
         (process-tag rule)

         :else
         rule))

     (defn process-rules
       "Process the complete set of rules for a component, see [[process-rule]]

  If multiple consecutive rules result in Garden property maps, then they get
  merged, to prevent unnecessary bloat of the compiled CSS."
       [rules]
       (let [add-rule (fn add-rule [acc r]
                        (cond
                          (and (vector? r)
                               (or (= :& (first r))
                                   (= "&" (first r))))
                          (reduce add-rule acc (next r))

                          (and (map? r)
                               (map? (last acc))
                               (not (record? r))
                               (not (record? (last acc))))
                          (conj (vec (butlast acc))
                                (merge (last acc) r))

                          :else
                          (conj acc r)))]
         (seq (reduce add-rule [] (map process-rule rules)))))))

(defn add-class
  "Hiccup helper, add a CSS classname to an existing `:class` property

  We allow components to define `:class` as a string, a vector, or to use a
  styled component directly as a class. (This last behavior is to support some
  legacy code, we recommend using a wrapping vector in that case).

  This function handles these cases, and will always return a vector of class
  names."
  [classes class]
  (cond
    (nil? class)
    classes

    (sequential? class)
    (reduce add-class classes class)

    (string? classes)
    [class classes]

    (= ::styled (:type (meta classes)))
    [class (str classes)]

    (and (sequential? classes) (seq classes))
    (vec (cons class classes))

    :else
    [(str class)]))

;; vocab note: we call "attributes" the key-value pairs you can supply to a HTML
;; element, like `class`, `style`, or `href`. We call "properties" the map you
;; pass as the first child to a Ornament/Hiccup component. For component that
;; don't have a custom render functions these properties will be used as
;; attributes. For components that do have a custom render function it depends
;; on what the render function does. In this case you can still pass in
;; attributes directly using the special `:lambdaisland.ornament/attrs`
;; property.
;; See also the Attributes and Properties notebook.

(defn merge-attr
  "Logic for merging two attribute values for the same key.
  - `class` : append the classname(s)
  - `style` : merge the right style map into the left"
  [k v1 v2]
  (case k
    :class (if (and (vector? v2) (:replace (meta v2)))
             v2
             (add-class v2 v1))
    :style (if (or (not (and (map? v1) (map? v2)))
                   (:replace (meta v2)))
             v2
             (merge v1 v2))
    v2))

(defn merge-attrs
  "Combine attribute maps"
  ([p1 p2]
   (when (or p1 p2)
     (let [merge-entry (fn [m e]
                         (let [k (key e)
                               v (val e)]
                           (if (contains? m k)
                             (assoc m k (merge-attr k (get m k) v))
                             (assoc m k v))))]
       (reduce merge-entry (or p1 {}) p2))))
  ([p1 p2 & ps]
   (reduce merge-attrs (merge-attrs p1 p2) ps)))

(defn attr-add-class [attrs class]
  (if class
    (update attrs :class add-class class)
    attrs))

(defn expand-hiccup-tag-simple
  "Expand an ornament component being called directly with child elements, without
  custom render function."
  [tag css-class children extra-attrs]
  (let [[tag attrs children :as result]
        (if (sequential? children)
          (as-> children $
            (if (= :<> (first $)) (next $) $)
            (if (map? (first $))
              (into [tag (attr-add-class
                          (merge-attrs (first $) (meta children) extra-attrs)
                          css-class)] (next $))
              (into [tag (attr-add-class
                          (merge-attrs (meta children) extra-attrs)
                          css-class)]
                    (if (vector? $) (list $) $))))
          [tag (attr-add-class extra-attrs css-class) children])]
    (if (= :<> (first children))
      (recur tag nil children attrs)
      result)))

(defn expand-hiccup-tag
  "Handle expanding/rendering the component to Hiccup

  For plain [[defstyled]] components this simply adds the CSS class name. For
  components with a render function this handles the expansion, and also handles
  fragments (`:<>`), optionally with an attributes map, and handles merging
  attributes passed in via the `::attrs` property."
  [tag css-class args component]
  (if component
    (let [result (apply component args)]
      (if (fn? result)
        (fn [& args]
          (expand-hiccup-tag-simple tag css-class (apply result args) (::attrs (first args))))
        (expand-hiccup-tag-simple tag css-class result (::attrs (first args)))))
    (expand-hiccup-tag-simple tag css-class (seq args) nil)))

(defn styled
  ([varsym css-class tag rules component]
   #?(:clj
      ^{:type ::styled}
      (reify
        StyledComponent
        (classname [_]
          (reduce
           (fn [c p]
             (if (str/starts-with? (str c) p)
               (reduced (subs (str c) (count p)))
               c))
           css-class
           *strip-prefixes*))
        (as-garden [this]
          (into [(str "." (classname this))]
                (process-rules rules)))
        (css [this] (gc/compile-css
                     {:pretty-print? false}
                     (as-garden this)))
        (rules [_] rules)
        (tag [_] tag)
        (component [_] component)
        (as-hiccup [this children]
          (expand-hiccup-tag tag (classname this) children component))

        clojure.lang.IFn
        (invoke [this]
          (as-hiccup this nil))
        (invoke [this a]
          (as-hiccup this [a]))
        (invoke [this a b]
          (as-hiccup this [a b]))
        (invoke [this a b c]
          (as-hiccup this [a b c]))
        (invoke [this a b c d]
          (as-hiccup this [a b c d]))
        (invoke [this a b c d e]
          (as-hiccup this [a b c d e]))
        (invoke [this a b c d e f]
          (as-hiccup this [a b c d e f]))
        (invoke [this a b c d e f g]
          (as-hiccup this [a b c d e f g]))
        (invoke [this a b c d e f g h]
          (as-hiccup this [a b c d e f g h]))
        (invoke [this a b c d e f g h i]
          (as-hiccup this [a b c d e f g h i]))
        (invoke [this a b c d e f g h i j]
          (as-hiccup this [a b c d e f g h i j]))
        (invoke [this a b c d e f g h i j k]
          (as-hiccup this [a b c d e f g h i j k]))
        (invoke [this a b c d e f g h i j k l]
          (as-hiccup this [a b c d e f g h i j k l]))
        (invoke [this a b c d e f g h i j k l m]
          (as-hiccup this [a b c d e f g h i j k l m]))
        (invoke [this a b c d e f g h i j k l m n]
          (as-hiccup this [a b c d e f g h i j k l m n]))
        (invoke [this a b c d e f g h i j k l m n o]
          (as-hiccup this [a b c d e f g h i j k l m n o]))
        (invoke [this a b c d e f g h i j k l m n o p]
          (as-hiccup this [a b c d e f g h i j k l m n o p]))
        (invoke [this a b c d e f g h i j k l m n o p q]
          (as-hiccup this [a b c d e f g h i j k l m n o p q]))
        (invoke [this a b c d e f g h i j k l m n o p q r]
          (as-hiccup this [a b c d e f g h i j k l m n o p q r]))
        (invoke [this a b c d e f g h i j k l m n o p q r s]
          (as-hiccup this [a b c d e f g h i j k l m n o p q r s]))
        (applyTo [this args]
          (as-hiccup this args))

        Object
        (toString [this] (classname this))

        gc/IExpandable
        (expand [this]
          (mapcat
           (fn [rule]
             (gc/expand
              (if (map? rule)
                [:& rule]
                rule)))
           rules)))

      :cljs
      (let [render-fn
            (fn [& children]
              (expand-hiccup-tag tag
                                 css-class
                                 children
                                 component))
            component (specify! render-fn
                        StyledComponent
                        (classname [_] css-class)
                        (as-garden [_] )
                        (css [_] )
                        (rules [_] )
                        (tag [_] tag)
                        (component [_] component)
                        (as-hiccup [_ children]
                          (expand-hiccup-tag tag css-class children component))

                        Object
                        (toString [_] css-class)

                        ;; https://ask.clojure.org/index.php/11514/functions-with-metadata-can-not-take-more-than-20-arguments
                        cljs.core/IMeta
                        (-meta [_] {:type ::styled}))]
        (js/Object.defineProperty component "name" #js {:value (str varsym)})
        component))))

#?(:clj
   (defn qualify-sym [env s]
     (when (symbol? s)
       (if (:ns env)
         ;; cljs
         (if (simple-symbol? s)
           (or (some-> env :ns :uses s name (symbol (name s)))
               (symbol (name (-> env :ns :name)) (name s)))
           (symbol (or (some-> env :ns :requires (get (symbol (namespace s))) name)
                       (namespace s))
                   (name s)))

         ;; clj
         (if (simple-symbol? s)
           (or (some-> (ns-refers *ns*) (get s) symbol)
               (symbol (str *ns*) (str s)))
           (let [ns (namespace s)
                 n (name s)
                 aliases (ns-aliases *ns*)]
             (symbol (or (some-> aliases (get (symbol ns)) ns-name str) ns) n)))))))

#?(:clj
   (defn fn-tail? [o]
     (and (list? o)
          (vector? (first o)))))

#?(:clj
   (defn update-index [registry varsym]
     (update-in registry [varsym :index] (fnil identity (count registry)))))

#?(:clj
   (defn register! [reg varsym m]
     ;; We give each style an incrementing index so they get a predictable
     ;; order (i.e. source order). If a style is evaluated again (e.g. REPL use)
     ;; then it keeps its original index/position.
     (swap! reg
            (fn [reg]
              (-> reg
                  (update varsym merge m)
                  (update-index varsym))))))

#?(:clj
   (defn render-docstring
     "Add the compiled CSS to the docstring, for easy dev-time reference. Ignored
  when `*compile-files*` is true, to prevent CSS from bloating up a production
  build."
     [docstring rules]
     (str
      docstring
      (when (not *compile-files*)
        (str
         (when docstring
           (str "\n\n"))
         (gc/compile-css (process-rules rules)))))))

#?(:clj
   (defn component->selector [&env s]
     (if (symbol? s)
       (let [qsym (qualify-sym &env s)]
         (if (contains? @registry qsym)
           (str "." (get-in @registry [qsym :classname]))
           s))
       s)))

#?(:clj
   (defn component->rules [&env s]
     (if (symbol? s)
       (let [qsym (qualify-sym &env s)]
         (if (contains? @registry qsym)
           (get-in @registry [qsym :rules])
           [s]))
       [s])))

#?(:clj
   (defn prop->rvalue [&env s]
     (if (symbol? s)
       (let [qsym (qualify-sym &env s)]
         (if (contains? @props-registry qsym)
           (str "var(--" (get-in @props-registry [qsym :propname]) ")")
           s))
       s)))

#?(:clj
   (defmacro defstyled [sym tagname & styles]
     (let [varsym (symbol (name (ns-name *ns*)) (name sym))
           css-class (classname-for varsym)
           [docstring & styles] (if (string? (first styles)) styles (cons nil styles))
           [styles fn-tails] (split-with (complement fn-tail?) styles)
           tag (if (keyword? tagname)
                 tagname
                 (get-in @registry [(qualify-sym &env tagname) :tag]))
           rules (cond
                   (keyword? tagname)
                   (vec styles)
                   (symbol? tagname)
                   (into (or (:rules (get @registry (qualify-sym &env tagname))) [])
                         styles))
           fn-tails (if (seq fn-tails)
                      fn-tails
                      (when (symbol? tagname)
                        (:fn-tails (get @registry (qualify-sym &env tagname)))))

           fn-tails (when (seq fn-tails)
                      (if (and (= 1 (count fn-tails))
                               (= 0 (count (ffirst fn-tails))))
                        `(([] ~@(rest (first fn-tails)))
                          ([attrs#] [:<> attrs# (do ~@(rest (first fn-tails)))]))
                        fn-tails))
           ;; For ClojureScript support (but also used in Clojure-only), add the
           ;; Clojure-version of the styled component to the registry directly
           ;; during macroexpansion, so that even in a ClojureScript-only world
           ;; we can access it later to compile the styles, even though the
           ;; styles themselves are never part of a ClojureScript build.
           ;;
           ;; To allow using previously defined styled components as selectors
           ;; we do our own resolution of these symbols, if we recognize them.
           ;; This is necessary since in ClojureScript rules are fully handled
           ;; on the Clojure side (we don't want any of the CSS overhead in the
           ;; build output), and when defined defstyled in cljs files there are
           ;; no Clojure vars that we can resolve, so we need to resolve this
           ;; ourselves via the registry.
           component->selector (partial component->selector &env)
           prop->rvalue (partial prop->rvalue &env)
           component->rules (partial component->rules &env)
           rules (eval `(do
                          (in-ns '~(ns-name *ns*))
                          ~(walk/postwalk
                            (fn [o]
                              (cond
                                (vector? o)
                                (into [(if (set? (first o))
                                         (into #{} (map component->selector (first o)))
                                         (component->selector (first o)))]
                                      (mapcat component->rules)
                                      (next o))
                                (map? o)
                                (update-vals o prop->rvalue)
                                :else
                                o))
                            (vec
                             (mapcat component->rules rules)))))]
       (register! registry
                  varsym
                  {:var varsym
                   :tag tag
                   :rules rules
                   :classname css-class
                   :fn-tails fn-tails
                   :component (styled varsym
                                      css-class
                                      tag
                                      rules
                                      nil)})

       ;; Actual output of the macro, this creates a styled component as a var,
       ;; so that it can be used in Hiccup. This `styled` invocation in turn is
       ;; platform-specific, the ClojureScript version only knows how to render
       ;; the component with the appropriate classes, it has no knowledge of the
       ;; actual styles, which are expected to be rendered on the backend or
       ;; during compilation.
       `(def ~(with-meta sym
                {::css true
                 :ornament (dissoc (get @registry varsym) :component :fn-tails)
                 :arglists (if (seq fn-tails)
                             `'~(map first fn-tails)
                             ''([] [& children] [attrs & children]))
                 :doc (render-docstring docstring [(into [(str "." css-class)] rules)])})
          (styled '~varsym
                  ~css-class
                  ~tag
                  ~(when-not (:ns &env) rules)
                  ~(when (seq fn-tails)
                     `(fn ~@fn-tails)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rules

#?(:clj
   (defmacro defrules
     "Define plain garden rules. Takes an optional docstring, and any number of
  Garden rules (vectors of selector + styles, possibly nested, at-rules, etc).

  Defines a var just so that you can inspect what's been evaluated, but the main
  action is the side-effect of registering the rules in a registry, which gets
  prepended to the rest of your Ornament CSS."
     [rules-name & rules]
     (let [[docstring & rules] (if (string? (first rules))
                                 rules
                                 (cons nil rules))
           varsym (qualify-sym &env rules-name)
           rules  (process-rules
                   (eval `(do
                            (in-ns '~(ns-name *ns*))
                            ~(cons 'list rules))))]
       (register! rules-registry varsym {:rules rules})
       (when-not (:ns &env)
         `(def ~rules-name ~(render-docstring docstring rules) '~rules)))))

#?(:clj
   (defmacro defutil
     "Define utility class, takes a name for the class, optionally a docstring, and a
  style map. Use the util var in your styles or as as class in hiccup."
     ([util-name styles]
      `(defutil ~util-name ~nil ~styles))
     ([util-name docstring styles]
      (let [varsym (qualify-sym &env util-name)
            klzname (classname-for varsym)
            rules (list [(str "." klzname)
                         (eval `(do
                                  (in-ns '~(ns-name *ns*))
                                  ~styles))])
            docstring (render-docstring docstring rules)]
        (register! rules-registry varsym {:rules rules})
        `(def ~util-name
           ~docstring
           (with-meta
             (reify
               Object
               (toString [_] ~klzname)
               gc/IExpandable
               (expand [_]
                 (gc/expand
                  [:& ~styles])))
             {:type ::util}))))))

#?(:clj
   (defmethod print-method ::util [u writer]
     (.write writer (str u))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Props

(defprotocol CSSProp
  (lvalue [p])
  (rvalue [p]))

(defn css-prop [prop-name default]
  #?(:clj
     (with-meta
       (reify
         CSSProp
         (lvalue [_] (str "--" (name prop-name)))
         (rvalue [_] (str "var(--" (name prop-name) ")"))
         gu/ToString
         (to-str [this]
           (str "--" (name prop-name)))
         Object
         (toString [_] (str "var(--" (name prop-name) ")"))
         clojure.lang.ILookup
         (valAt [this kw] (when (= :default kw) default))
         (valAt [this kw fallback] (if (= :default kw) default fallback))
         )
       {:type ::prop})
     :cljs
     (with-meta
       (reify
         CSSProp
         (lvalue [_] (str "--" (name prop-name)))
         (rvalue [_] (str "var(--" (name prop-name) ")"))
         ILookup
         (-valAt [this kw] (when (= :default kw) default))
         (-valAt [this kw fallback] (if (= :default kw) default fallback))
         )
       {:type ::prop})))

#?(:clj
   (defmethod print-method ::prop [p writer]
     (.write writer (lvalue p))))

#?(:clj
   (defn propname-for
     [propsym]
     (let [prefix (or (:ornament/prefix (meta (the-ns (symbol (namespace propsym)))))
                      (-> propsym
                          namespace
                          (str/replace #"\." "-")
                          (str "--")))]
       (str prefix (munge-str (str/replace (name propsym)
                                           #"^--" "") (dissoc munge-map \-))))))

#?(:clj
   (defmacro defprop
     "Define a custom CSS property (variable). Use the resulting var either where a
  value is expected (will expand to `var(--var-name)`), or where a name is
  expected (e.g. to assign it in a context)."
     ([prop-name]
      `(defprop ~prop-name nil))
     ([prop-name value]
      `(defprop ~prop-name nil ~value))
     ([prop-name docstring value]
      (let [varsym (qualify-sym &env prop-name)
            propname (propname-for varsym)
            value (eval value)]
        (register! props-registry varsym {:propname propname :value value})
        `(def ~prop-name
           ~(str
             (when docstring
               (str docstring "\n\n"))
             "Default: " value)
           (css-prop '~propname ~value))))))

#?(:clj
   (defn import-tokens*!
     ([tokens {:keys [include-values? prefix]
               :or {include-values? true
                    prefix ""}}]
      (mapcat
       identity
       (for [[tname tdef] tokens]
         (let [tname (str prefix tname)
               {:strs [$description $value $type]} tdef
               more (into {} (remove (fn [[k v]] (= (first k) \$))) tdef)]
           (cond-> [`(defprop ~(symbol tname)
                       ~@(when $description [(str $description "\n\nDefault: " $value)])
                       ~@(when (and $value include-values?)
                           [$value]))]
             (seq more)
             (into (import-tokens*! (str tname "-") more)))))))))

#?(:clj
   (defmacro import-tokens!
     "Import a standard design tokens JSON file.
  Emits a sequence of `defprop`, i.e. it defines custom CSS properties (aka
  variables). See https://design-tokens.github.io/community-group/format/
  - tokens: parsed JSON, we don't bundle a parser, you have to do that yourself
  - opts: options map, supports `:prefix` and `:include-values?`. Has to be
    literal (used by the macro itself)
    - prefix: string prefix to add to the (clojure and CSS) var names
    - :include-values? false: only create the Clojure vars to access the props,
      don't include their definitions/values in the CSS. Presumably because you are
      loading CSS separately that already defines these.
  "
     ([tokens & [opts]]
      `(do ~@(import-tokens*! (eval tokens) opts)))))

#?(:clj
   (defn defined-garden
     "All CSS defined through the different Ornament facilities (defprop, defstyled,
  defrules), in Garden syntax. Run this through `garden.compiler/compile-css`."
     []
     (concat
      (let [props (->> @props-registry
                       vals
                       (filter (comp some? :value)))]
        (when (seq props)
          [[":where(html)" (into {}
                                 (map (juxt (comp (partial str "--") :propname)
                                            :value))
                                 props)]]))
      (->> @rules-registry
           vals
           (sort-by :index)
           (mapcat :rules))
      (->> @registry
           vals
           (sort-by :index)
           (map (fn [{:keys [var tag rules classname]}]
                  (as-garden (styled var classname tag rules nil))))))))

#?(:clj
   (defn defined-styles
     "Collect all styles that have been defined, and compile them down to CSS. Use
  this to either spit out or inline a stylesheet with all your Ornament styles.
  Optionally the Tailwind preflight (reset) stylesheet can be prepended using
  `:preflight? true`. This defaults to Tailwind v2 (as provided by Girouette).
  Version 3 is available with `:tw-version 3`"
     [& [{:keys [preflight? tw-version compress?]
          :or {preflight? false
               tw-version 2
               compress? true}}]]
     (gc/compile-css
      {:pretty-print? (not compress?)}
      (cond->> (defined-garden)
        preflight? (concat (case tw-version
                             2 girouette-preflight/preflight-v2_0_3
                             3 girouette-preflight/preflight-v3_0_24))))))

#?(:clj
   (defn cljs-restore-registry
     "Restore the Ornament registry based on a ClojureScript compiler env

  Due to caching some defstyled macros may not get recompiled, causing gaps in
  the CSS. To work around this we add Ornament data to the cljs analyzer var
  metadata, so it gets cached and restored with the rest of the analyzer state."
     [compiler-env]
     (when (empty? @registry)
       (reset! registry
               (into {}
                     (for [[_ {:keys [defs]}] (:cljs.analyzer/namespaces compiler-env)
                           [_ {{:keys [ornament]} :meta}] defs
                           :when ornament]
                       [(:var ornament) ornament]))))))

(comment
  (spit "/tmp/ornament.css" (defined-styles))

  (->> @rules-registry
       vals
       (sort-by :index)
       (mapcat :rules)
       process-rules))

