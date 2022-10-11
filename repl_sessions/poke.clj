(ns poke
  (:require [lambdaisland.ornament :as o]))


(o/defstyled freebies-link :a
  {:font-size "1rem"
   :color "#cff9cf"
   :text-decoration "underline"})

(freebies-link {:href "/episodes/interceptors-concepts"} "hello")

[:a {:class ["poke__freebies_link"]
     :href "/episodes/interceptors-concepts"} "hello"]
