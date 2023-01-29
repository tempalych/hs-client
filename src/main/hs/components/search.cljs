(ns hs.components.search
  (:require [hs.state :refer [state]]))

(defn find-patients [server-url]
  (let [url (str server-url "/patients/find")
        {:keys [fname lname]} (:search @state)]
    (-> (js/fetch url
                  #js{:method "POST"
                      :headers #js{"Content-Type" "application/json"}
                      :body (js/JSON.stringify
                             #js{:fname fname
                                 :lname lname})})
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
                                  patients-vec))))))))

(defn update-search [k v] 
  (swap! state assoc-in [:search k] v))

(defn search [server-url]
  [:div
   [:table 
    [:tbody
     (let [{:keys [lname fname]} (:search @state)]
       [:tr
        [:td [:input {:value lname :name "lname-search" :placeholder "Last Name"
                      :on-change (fn [e] (update-search :lname (-> e .-target .-value)))}]]
        [:td [:input {:value fname :name "fname-search" :placeholder "First Name"
                      :on-change (fn [e] (update-search :fname (-> e .-target .-value)))}]]
        
        [:td [:button {:on-click (fn [e] (find-patients server-url))} "🔍" ]]])
     ]]])


(comment
  (:search @state)
  
  )