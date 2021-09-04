(ns repl-sessions.cssparser
  (:require [clojure.java.io :as io])
  (:import (com.steadystate.css.parser CSSOMParser SACParserCSS3 HandlerBase )
           (org.w3c.css.sac InputSource DocumentHandler)))

;; https://javadoc.io/static/net.sourceforge.cssparser/cssparser/0.9.11/com/steadystate/css/parser/CSSOMParser.html

(def css3-parser (SACParserCSS3.))
(def parser (CSSOMParser. css3-parser))

(.setDocumentHandler css3-parser
                     ^DocumentHandler
                     (proxy [HandlerBase] []
                       (ignorableAtRule [x y]
                         (prn [x y]))))

(def s
  (rand-nth (seq (.getRules
                  (.getCssRules
                   (.parseStyleSheet
                    parser
                    (InputSource. (io/reader (io/file "/home/arne/ARS/ductile/stylesheets/jit.css")))
                    nil
                    nil))))))
(bean (.getStyle s))
