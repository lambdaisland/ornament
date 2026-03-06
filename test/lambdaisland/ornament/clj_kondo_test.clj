(ns lambdaisland.ornament.clj-kondo-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.shell :as shell]
            [clojure.edn :as edn]))

(defn- lint-string
  "Run clj-kondo --lint on a code string, return findings as EDN."
  [code]
  (let [{:keys [out]} (shell/sh "clj-kondo"
                                "--lint" "-"
                                "--config-dir" ".clj-kondo"
                                "--config" "{:output {:format :edn}}"
                                :in code)]
    (:findings (edn/read-string out))))

(defn- ornament-warnings
  "Return only the ornament/invalid-syntax findings."
  [findings]
  (filter #(= :lambdaisland.ornament/invalid-syntax (:type %)) findings))

(defn- warning-messages
  "Return the set of warning message strings from findings."
  [findings]
  (into #{} (map :message) findings))

;; Valid patterns — should produce no ornament warnings

(deftest keyword-tag-test
  (testing "keyword tag produces no warnings"
    (let [warnings (-> "(require '[lambdaisland.ornament :as o])
                        (o/defstyled foo :div)"
                       lint-string
                       ornament-warnings)]
      (is (empty? warnings)))))

(deftest styled-component-as-tag-test
  (testing "styled component used as tag produces no warnings"
    (let [warnings (-> "(require '[lambdaisland.ornament :as o])
                        (o/defstyled base :span {:color \"blue\"})
                        (o/defstyled inherited base)"
                       lint-string
                       ornament-warnings)]
      (is (empty? warnings)))))

(deftest symbol-in-rules-test
  (testing "symbol references in rules position produce no warnings"
    (let [warnings (-> "(require '[lambdaisland.ornament :as o])
                        (o/defstyled bold :span :font-bold)
                        (o/defstyled heading :h1 bold :text-3xl)"
                       lint-string
                       ornament-warnings)]
      (is (empty? warnings)))))

(deftest fn-body-test
  (testing "defstyled with fn body produces no warnings"
    (let [warnings (-> "(require '[lambdaisland.ornament :as o])
                        (o/defstyled foo :div ([props] [:div props]))"
                       lint-string
                       ornament-warnings)]
      (is (empty? warnings)))))

;; Invalid patterns — should produce ornament warnings

(deftest non-symbol-name-test
  (testing "non-symbol name produces warning"
    (let [msgs (-> "(require '[lambdaisland.ornament :as o])
                     (o/defstyled 123 :div)"
                   lint-string
                   ornament-warnings
                   warning-messages)]
      (is (contains? msgs "Style name must be a simple symbol")))))

(deftest non-keyword-non-styled-tag-test
  (testing "numeric literal as tag produces warning"
    (let [msgs (-> "(require '[lambdaisland.ornament :as o])
                     (o/defstyled foo 123)"
                   lint-string
                   ornament-warnings
                   warning-messages)]
      (is (contains? msgs "Tag must be a keyword or an ornament-styled-component")))))

(deftest regular-def-as-tag-test
  (testing "regular def used as tag produces warning"
    (let [msgs (-> "(require '[lambdaisland.ornament :as o])
                     (def not-styled 42)
                     (o/defstyled foo not-styled)"
                   lint-string
                   ornament-warnings
                   warning-messages)]
      (is (contains? msgs "Tag must be a keyword or an ornament-styled-component")))))

(deftest invalid-fn-part-test
  (testing "non-list in fn position produces warning"
    (let [msgs (-> "(require '[lambdaisland.ornament :as o])
                     (o/defstyled foo :div #{:a :b})"
                   lint-string
                   ornament-warnings
                   warning-messages)]
      (is (contains? msgs "Function part (if present) must be a list")))))
