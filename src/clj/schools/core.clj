(ns schools.core
  (:require [clojure.java.io :refer [reader writer]]
            [clojure.string :refer [split]]
            [clojure.core.match :refer [match]]))

(def filename "szkoly_krakow_2014_P_1261.pdf")
(def filename-txt "szkoly_krakow_2014_P_1261.txt")

(defn first-word [line] (first (split line #"\s+")))

[:foo (first-word "foo bar")]

(first-word "Okręgowa komisja")

(match [:foo (first-word "foo bar")]
       [:foo "foo"] 1
       :else 2)

(defn calc-state [state line]
  (match [state (first-word line)]
    [_ "Okręgowa"] :header
    [:header "Rok"] :header-ends
    [:header-ends _] :printing
    [_ "Data"] :footer
    [:footer "Liczba"] :footer-ends
    [:footer-ends _] :printing
    :else state))

(defn skip-comments [lines]
  (for [ln lines :when (and (< 0 (count ln)) (not= \# (first ln)))] ln))

(defn remove-headers-and-footers [lines]
  (loop [lines (skip-comments lines) state :printing result []]
    (if-let [ln (first lines)]
      (let [state (calc-state state ln)]
        (recur (rest lines) state (if (= :printing state) (conj result ln) result)))
      result)))

(defn split-schools [lines]
  (for [pair (partition 2 (partition-by #(= % "2002") lines))]
    (apply concat pair)))

(defn parse-name-address [sq]
  (let [parts (partition-by #(.startsWith % "Kraków") sq)]
    {:name (clojure.string/join " " (first parts)) :address (clojure.string/join " " (apply concat (rest parts)))}))

(defn parse-data [sq]
  (for [year-data (partition 20 sq)]
    (zipmap [:students :dislectic :score :stanin :reading :writing :reasoning :using-info :applying-knowledge :general :p1 :p2 :p3 :p4 :p5 :p6 :p7 :p8 :9 :year] year-data)
    ))

(defn parse-school [line]
  (let [address-lines (drop-last 260 line)
        data-lines (take-last 260 line)
        school (parse-name-address address-lines)
        data (parse-data data-lines)]
    (merge school {:data data})))

(with-open [rdr (reader filename-txt)
            wrtr (writer "src/cljs/schools/data.cljs")]
  (let [lines (line-seq rdr)
        lines (remove-headers-and-footers lines)
        school-lines (split-schools lines)
        parsed-schools (map parse-school school-lines)]
    ;(clojure.pprint/pprint '(ns schools.data) wrtr)
    ;(clojure.pprint/pprint (list 'defonce 'schools (pr-str parsed-schools)) wrtr)
    ;(clojure.pprint/pprint (list 'defonce 'schools (list 'read-string (pr-str parsed-schools))) wrtr)
    (.write wrtr "(ns schools.data)")
    (.write wrtr "(defonce schools '")
    (clojure.pprint/pprint parsed-schools wrtr)
    (.write wrtr ")")
    ))
    ;(println (take 3 parsed-schools))))

  (def z '(:a :b :c (:a :b)))
  (clojure.pprint/pprint (list 'read-string (pr-str z)))
  (read-string (pr-str z))
  (prn z)
  z
  (pr-str z)

  (print z)
