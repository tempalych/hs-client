(ns hs.core
  (:require [reagent.core :as r]
            [hs.components.header :refer [header]]
            [hs.components.patients-list :refer [patients-list]]
            [hs.components.search :refer [search]]
            [hs.state :refer [state]]))

(goog-define server-url "http://localhost:8080")

(defn app 
  []
  [:div.container
   [header] 
   [:hr]
   [(fn [] (search server-url))]
   [:br]
   [(fn [] (patients-list server-url))]
   [:hr]])

(defn ^:export main
  []
  (let [url (str server-url "/patients")]
    (-> (js/fetch url #js{:method "GET"})
        (.then (fn [response]
                 (when-not (.-ok response)
                   (throw (ex-info (.-statusText response)
                                   {:response response})))
                 (swap! state dissoc :error)
                 (.json response)))
        (.then (fn [patients-json]
                 (let [patients-vec (js->clj patients-json :keywordize-keys true)]
                   (swap! state assoc
                          :patients
                          (reduce (fn [accum item]
                                    (assoc accum (str (:id item))
                                           (assoc item
                                                  :birthdate
                                                  (:birthdate item))))
                                  {}
                                  patients-vec)))))))

  (r/render
   [app]
   (.getElementById js/document "app")))

(comment
  (shadow.cljs.devtools.api/nrepl-select :app)
  (ns hs.core)  
  
  @state 
  )