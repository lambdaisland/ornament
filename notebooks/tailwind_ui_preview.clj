(ns notebooks.tailwind-ui-preview
  (:require [nextjournal.clerk :ac clerk]
            [lambdaisland.ornament :as o]
            [lambdaisland.hiccup :as h]))

(def html (comp nextjournal.clerk/html h/render))

(o/defstyled
  ^{:doc "Taken from
https://tailwindcomponents.com/component/tailwind-card-portfolio"}
  portfolio-card :div
  :flex :items-center :justify-center
  :h-screen :bg-gradient-to-br
  :from-indigo-500 :to-indigo-800
  [:>div :bg-white :font-semibold :text-center :rounded-3xl
   :border :shadow-lg :p-10 :max-w-xs]
  [:img :mb-3 :w-32 :h-32 :rounded-full :shadow-lg :mx-auto]
  [:h1 :text-lg :text-gray-700]
  [:h3 :text-sm :text-gray-400]
  [:p :text-xs :text-gray-400 :mt-4]
  [:button :bg-indigo-600 :px-8 :py-2 :mt-8 :rounded-3xl
   :text-gray-100 :font-semibold :uppercase :tracking-wide]
  ([{:keys [name image title bio button-text]}]
   [:div
    [:div
     [:img {:src image :alt name}]
     [:h1 name]
     [:h3 title]
     [:p bio]
     [:button button-text]]]))

(html
 [portfolio-card {:name "Arne Brasseur"
                  :title "Technological Maestro"
                  :image "https://pbs.twimg.com/profile_images/1330134972750041092/Q7_zfpmq_400x400.jpg"
                  :bio "Turning tea into Sexps"
                  :button-text "Get in touch!"}])

(o/defstyled mark-external-links :div
  ["a[href*=\"//\"]:not([href*=\"mysite.com\"]):after"
   {:content "\" ↗️\""}])


(html
 [mark-external-links
  [:p "We love "
   [:a {:href "https://en.wikipedia.org/wiki/Clojure"} "Clojure"]]])


(html
 [:p {:class [mark-external-links]} "We love "
  [:a {:href "https://en.wikipedia.org/wiki/Clojure"} "Clojure"]])

(o/defstyled my-para :p
  [:div
   mark-external-links])

(html
 [my-para "We love "
  [:a {:href "https://en.wikipedia.org/wiki/Clojure"} "Clojure"]])

(o/css my-para)


;; https://tailwindcomponents.com/component/footer-with-newsletter

(def svg-heart
  [:svg
   {:fill "#e53e3e", :viewbox "0 0 24 24", :stroke "currentColor"}
   [:path
    {:stroke-linecap "round",
     :stroke-linejoin "round",
     :stroke-width "2",
     :d
     "M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"}]])

(def svg-fb
  [:svg
   {:viewbox "0 0 512 512"}
   [:path
    {:d
     "M455.27,32H56.73A24.74,24.74,0,0,0,32,56.73V455.27A24.74,24.74,0,0,0,56.73,480H256V304H202.45V240H256V189c0-57.86,40.13-89.36,91.82-89.36,24.73,0,51.33,1.86,57.51,2.68v60.43H364.15c-28.12,0-33.48,13.3-33.48,32.9V240h67l-8.75,64H330.67V480h124.6A24.74,24.74,0,0,0,480,455.27V56.73A24.74,24.74,0,0,0,455.27,32Z"}]])

(def svg-email
  [:svg
   {:fill "none", :viewbox "0 0 24 24", :stroke "currentColor"}
   [:path
    {:stroke-linecap "round",
     :stroke-linejoin "round",
     :stroke-width "2",
     :d
     "M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"}]])

(def svg-linkedin
  [:svg
   {:viewbox "0 0 512 512"}
   [:path
    {:d
     "M444.17,32H70.28C49.85,32,32,46.7,32,66.89V441.61C32,461.91,49.85,480,70.28,480H444.06C464.6,480,480,461.79,480,441.61V66.89C480.12,46.7,464.6,32,444.17,32ZM170.87,405.43H106.69V205.88h64.18ZM141,175.54h-.46c-20.54,0-33.84-15.29-33.84-34.43,0-19.49,13.65-34.42,34.65-34.42s33.85,14.82,34.31,34.42C175.65,160.25,162.35,175.54,141,175.54ZM405.43,405.43H341.25V296.32c0-26.14-9.34-44-32.56-44-17.74,0-28.24,12-32.91,23.69-1.75,4.2-2.22,9.92-2.22,15.76V405.43H209.38V205.88h64.18v27.77c9.34-13.3,23.93-32.44,57.88-32.44,42.13,0,74,27.77,74,87.64Z"}]])

(def svg-twitter
  [:svg
   {:viewbox "0 0 512 512"}
   [:path
    {:d
     "M496,109.5a201.8,201.8,0,0,1-56.55,15.3,97.51,97.51,0,0,0,43.33-53.6,197.74,197.74,0,0,1-62.56,23.5A99.14,99.14,0,0,0,348.31,64c-54.42,0-98.46,43.4-98.46,96.9a93.21,93.21,0,0,0,2.54,22.1,280.7,280.7,0,0,1-203-101.3A95.69,95.69,0,0,0,36,130.4C36,164,53.53,193.7,80,211.1A97.5,97.5,0,0,1,35.22,199v1.2c0,47,34,86.1,79,95a100.76,100.76,0,0,1-25.94,3.4,94.38,94.38,0,0,1-18.51-1.8c12.51,38.5,48.92,66.5,92.05,67.3A199.59,199.59,0,0,1,39.5,405.6,203,203,0,0,1,16,404.2,278.68,278.68,0,0,0,166.74,448c181.36,0,280.44-147.7,280.44-275.8,0-4.2-.11-8.4-.31-12.5A198.48,198.48,0,0,0,496,109.5Z"}]])

(o/defstyled footer-with-newsletter :footer
  :bg-gray-300 :w-full :py-6 :px-4 :my-32
  [:div.top :px-4 :pt-3 :pb-4 :border-b :-mx-4 :border-gray-400
   [:>div :max-w-xl :mx-auto
    [:>h2 :text-xl :text-left :inline-block :font-semibold :text-gray-800]
    [:>p :text-gray-700 :text-xs :pl-px]
    [:>form :mt-2
     [:>div :flex :items-center
      [:>input :w-full :px-2 :py-4 :mr-2 :bg-gray-100 :shadow-inner :rounded-md :border :border-gray-400 :focus:outline-none]
      [:>button :bg-blue-600 :text-gray-200 :px-5 :py-2 :rounded :shadow]]]]]
  [:div.bottom :flex :items-center :justify-between :my-4
   [:.rights-reserved :text-blue-500]
   [:.author :inline-flex :text-blue-500 :px-2 :pt-6
    [:>svg :w-5 :h-5 :mx-1 :pt-px :text-red-600]]
   [:>div :flex :items-center
    [:>a [:>svg :h-6 :w-6 :text-blue-600 :mr-6]]
    [:.fill :fill-current]]]
  ([]
   [:<>
    [:div.top
     [:div
      [:h2 "Join Our Newsletter"]
      [:p "Latest news ,articles and updates montly delevered to your inbox."]
      [:form {:action "#"}
       [:div
        [:input {:type "email", :required "required"}]
        [:button {:style "margin-left: -7.8rem;"} "Sign Up"]]]]]
    [:div.bottom
     [:p.rights-reserved "All rights reserved"]
     [:p.author "Built with" svg-heart "by Mohammed Ibrahim(Jermine Junior)."]
     [:div
      [:a.fill {:href "#"} svg-fb]
      [:a {:href "#"} svg-email]
      [:a.fill {:href "#"} svg-linkedin]
      [:a.fill {:href "#"} svg-twitter]]]]))

(html [footer-with-newsletter])

^{:nextjournal.clerk/no-cache true
  :nextjournal.clerk/visibility :hide}
(html
 [:style (o/defined-styles)])
