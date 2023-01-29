(ns hs.spec
  (:require [clojure.spec.alpha :as s]
            [hs.state :refer [state]]))

(s/def ::string-50
  (s/and string?
         #(some? (re-matches #"^[ \r\n\t\S]{0,50}$" %))))

(s/def ::ne-string-50
  (s/and string? 
         #(some? (re-matches #"^[ \r\n\t\S]{1,50}$" %))))

(s/def ::string-100
  (s/and string?
         #(some? (re-matches #"^[ \r\n\t\S]{0,100}$" %))))

(s/def ::string-1000
  (s/and string?
         #(some? (re-matches #"^[ \r\n\t\S]{0,1000}$" %))))

(def gender-list #{"F" "M" "Un"})
(s/def ::gender
  (fn [value]
    (contains? gender-list value)))

(s/def ::birthdate
  (fn [value]
    (<= (js/Date. value) (js/Date.))))

(def constraints
  {:lname     :hs.spec/ne-string-50
   :fname     :hs.spec/ne-string-50
   :pname     :hs.spec/string-50
   :gender    :hs.spec/gender
   :birthdate :hs.spec/birthdate
   :address   :hs.spec/string-1000
   :insurance :hs.spec/string-100})

(defn validate! [k v] 
  (let [constraint (k constraints)] 
    (if (not (s/valid? constraint v))
      (swap! state assoc :invalid (conj (:invalid @state) k))
      (swap! state assoc :invalid (remove #(= % k) (:invalid @state))))))

(defn validity-class [element]
  (if (some #(= % element) (:invalid @state))
    "invalid"
    "valid"))


(s/def ::->date
  (s/conformer
   (fn [value]
     (let [parsed-date (js/Date. value)]
       (if (js/isNaN parsed-date)
         ::s/invalid
         parsed-date)))))

(s/def ::date->string-yyyy-mm-dd
  (s/conformer
   (fn [date-value]
     (if (nil? date-value)
       nil
       (let [dd (.padStart (str (.getDate date-value)) 2 "0")
             mm (.padStart (str (+ 1 (.getMonth date-value))) 2 "0")
             yyyy (.getFullYear date-value)]
         (str yyyy "-" mm "-" dd))))))


(comment 
  @state
  )