(ns lambdaisland.ornament.watcher
  "Watch the filesystem for changes, and regenerate the Ornament CSS file

  We generally combine this with Figwheel, and let figwheel handle reloading the
  CLJ files, as well as hot-loading the new CSS in the browser.

  Hawk and Glögi are BYO"
  (:require [clojure.java.io :as io]
            [hawk.core :as hawk]
            [lambdaisland.glogc :as log]
            [lambdaisland.ornament :as ornament])
  (:import [java.util Timer TimerTask]))

(defn debounced
  "Debounce a function, it will be called at most once every delay-ms
  milliseconds."
  [f delay-ms]
  (let [timer (Timer.)
        last-task (atom nil)]
    (fn [& args]
      (let [task (proxy [TimerTask] [] (run [] (apply f args)))]
        (swap! last-task
               (fn [prev]
                 (when prev (.cancel ^TimerTask prev))
                 (.schedule ^Timer timer ^TimerTask task delay-ms)
                 task)))
      nil)))

(defn requires-ornament?
  "Does this Clojure file require the Ornament namespace?"
  [f]
  (try
    (with-open [rdr (-> f io/file io/reader java.io.PushbackReader.)]
      (->> rdr
           (read {:features #{:clj} :read-cond :allow})
           flatten
           (some '#{lambdaisland.ornament})))
    (catch Exception _
      false)))

(defn make-output-fn [{:keys [outfile]
                       :or {outfile "resources/public/css/compiled/ornament.css"}}]
  (debounced
   (fn []
     (log/debug :ornament-watcher/writing outfile)
     (io/make-parents outfile)
     (spit outfile (ornament/defined-styles)))
   1000))

(defn make-hawk-handler [opts]
  (let [write-ornament-css! (make-output-fn opts)]
    (fn [ctx {:keys [kind file]}]
      (when (requires-ornament? file)
        (write-ornament-css!)
        (when-let [cb (:callback opts)]
          (cb))))))

(defn start-watcher!
  "Start a watcher which recreates the ornament CSS output file when source
  namespaces change.

  - `:watch-paths` The source directories to watch
  - `:outfile` The CSS file to write to
  - `:callback` Optional function to call after the CSS updates"
  [{:keys [watch-paths]
    :or {watch-paths ["src"]}
    :as opts}]
  (hawk/watch! [{:paths ["src"]
                 :handler (make-hawk-handler opts)}]))

(defn stop-watcher! [hawk]
  (hawk/stop! hawk))
