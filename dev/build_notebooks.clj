(ns build-notebooks
  "Build notebooks as a static app on CI"
  (:require
   [clojure.java.io :as io]
   [nextjournal.clerk :as clerk]))

(defn -main [sha]
  (clerk/build-static-app!
   {:paths (->> (file-seq (io/file "notebooks"))
                (remove (memfn ^java.io.File isDirectory)))
    :bundle? false
    :path-prefix ""
    :git/sha sha
    :git/url "https://github.com/lambdaisland/ornament"
    :browse? false}))
