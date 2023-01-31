(ns hs.components.patients-list
  (:require [hs.state :refer [state]]
            [hs.spec :as spec]))

(defn update-new-patient [k v]
  (spec/validate! k v)
  (swap! state assoc-in [:new k] v))

(comment
  (:invalid @state)
  (:invalid @state) 
  (spec/validity-class :lname)
  )
  
  
(defn add-patient [server-url]
  (let [url (str server-url "/patients")
        {:keys [fname lname pname gender birthdate address insurance]} (:new @state)]
   (-> (js/fetch url
                 #js{:method "POST"
                     :headers #js{"Content-Type" "application/json"}
                     :body (js/JSON.stringify
                            #js{:fname fname
                                :lname lname
                                :pname pname
                                :gender gender
                                :birthdate birthdate
                                :address address
                                :insurance insurance})})
       (.then (fn [response]
                (when-not (.-ok response)
                  (throw (ex-info (.-statusText response)
                                  {:response response})))
                (swap! state dissoc :error)
                (.json response)))
       (.then (fn [patient-json]
                (let [patient-vec (first (js->clj patient-json :keywordize-keys true))]
                  (swap! state assoc-in [:patients (str (:id patient-vec))] patient-vec)
                  (swap! state assoc :new {})))))))

(defn delete-patient [server-url id]
  (let [url (str server-url "/patients")]
    (-> (js/fetch url
                  #js{:method "DELETE"
                      :headers #js{"Content-Type" "application/json"}
                      :body (js/JSON.stringify #js{:id id})})
        (.then (fn [response]
                 (when-not (.-ok response)
                   (throw (ex-info (.-statusText response)
                                   {:response response})))
                 (swap! state dissoc :error)
                 (.json response)))
        (.then (fn []
                 (swap! state update-in [:patients] dissoc (str id)))))))

(defn edit-patient [id]
  (swap! state assoc :new (get (:patients @state) (str id)))
  (swap! state assoc :edit-mode true))

(defn apply-edit-patient [server-url]
  (let [url (str server-url "/patients")
        {:keys [id fname lname pname gender birthdate address insurance]} (:new @state)]
    (-> (js/fetch url
                  #js{:method "PUT"
                      :headers #js{"Content-Type" "application/json"}
                      :body (js/JSON.stringify
                             #js{:id id
                                 :fname fname
                                 :lname lname
                                 :pname pname
                                 :gender gender
                                 :birthdate birthdate
                                 :address address
                                 :insurance insurance})})
        (.then (fn [response]
                 (when-not (.-ok response)
                   (throw (ex-info (.-statusText response)
                                   {:response response})))
                 (swap! state dissoc :error)
                 (.json response)))
        (.then (fn []
                 (swap! state assoc-in [:patients (str (:id (:new @state)))] (:new @state))
                 (swap! state assoc :new {})
                 (swap! state assoc :edit-mode false))))))

(defn add-or-apply [server-url]
  (if (empty? (:invalid @state))
    (if (:edit-mode @state)
      (apply-edit-patient server-url)
      (add-patient server-url))
    (js/alert "Check attributes validation")))

(defn cancel []
  (swap! state assoc :new {})
  (swap! state assoc :edit-mode false))

(defn patients-list [server-url]
  [:div
   [:table
    [:thead {:class "table-head"}
     [:tr
      [:td "Last Name"]
      [:td "First Name"]
      [:td "Patronymic"]
      [:td "Gender"]
      [:td "Birthdate"]
      [:td "Address"]
      [:td "Insurance Number"]
      [:td "Edit"]
      [:td "Del"]]]
    [:tbody {:id "tblBody"}
     (map (fn [patient]
            (let [{:keys [id
                          lname
                          fname
                          pname
                          gender
                          birthdate
                          address
                          insurance]} patient
                  edit-mode (:edit-mode @state)]
              [:tr {:key id}
               [:td lname]
               [:td fname]
               [:td pname]
               [:td gender]
               [:td birthdate]
               [:td address]
               [:td insurance]
               [:td [:button {:id (str "btnEdit" id)
                              :disabled edit-mode
                              :on-click (fn [] (edit-patient id))} "..."]]
               [:td [:button {:id (str "btnDel" id)
                              :disabled edit-mode
                              :on-click (fn [] (delete-patient server-url id))} "🗑️"]]]))
          (vals (:patients @state)))]
    [:tfoot {:class "addoredit"}
     (let [{:keys [lname fname pname gender birthdate address insurance]} (:new @state)
           edit-mode (:edit-mode @state)]
       [:tr
        [:td [:input {:value lname :name "lname" :placeholder "Last Name"
                      :on-change (fn [e] (update-new-patient :lname (-> e .-target .-value)))}]]
        [:td [:input {:value fname :name "fname" :placeholder "First Name"
                      :on-change (fn [e] (update-new-patient :fname (-> e .-target .-value)))}]]
        [:td [:input {:value pname :name "pname" :placeholder "Patronymic"
                      :on-change (fn [e] (update-new-patient :pname (-> e .-target .-value)))}]]
        [:td
         [:label "M " [:input {:type "radio"
                               :name "gender"
                               :checked (= gender "M")
                               :on-change (fn [] (update-new-patient :gender "M"))}]]
         [:label "F " [:input {:type "radio"
                               :name "gender"
                               :checked (= gender "F")
                               :on-change (fn [] (update-new-patient :gender "F"))}]]
         [:label "Un " [:input {:type "radio"
                                :name "gender"
                                :checked (= gender "Un")
                                :on-change (fn [] (update-new-patient :gender "Un"))}]]]
        [:td [:input {:value (or birthdate "2000-01-01")
                      :type "date"
                      :on-change (fn [e] (update-new-patient :birthdate (-> e .-target .-value)))}]]
        [:td [:input {:value address :name "address" :placeholder "Address"
                      :on-change (fn [e] (update-new-patient :address (-> e .-target .-value)))}]]
        [:td [:input {:value insurance :name "insurance" :placeholder "Insurance Number"
                      :on-change (fn [e] (update-new-patient :insurance (-> e .-target .-value)))}]]
        [:td [:button {:id "btnAdd" :on-click (fn [] (add-or-apply server-url))}
              (if (not edit-mode)
                "➕"
                "✅")]]
        [:td [:button {:id "btnCancel"
                       :on-click (fn [] (cancel))
                       :disabled (not edit-mode)}
              "❌"]]])
     (let [invalid-lname (spec/validity-class :lname)
           invalid-fname (spec/validity-class :fname)
           invalid-pname (spec/validity-class :pname)
           invalid-gender (spec/validity-class :gender)
           invalid-birthdate (spec/validity-class :birthdate)
           invalid-address (spec/validity-class :address)
           invalid-insurance (spec/validity-class :insurance)]
       [:tr {:class "validation-row"}
        [:td [:div {:class (str "validator-" invalid-lname)} "Non-empty, max 50"]]
        [:td [:div {:class (str "validator-" invalid-fname)} "Non-empty, max 50"]]
        [:td [:div {:class (str "validator-" invalid-pname)} "Max 50"]]
        [:td [:div {:class (str "validator-" invalid-gender)} "*"]]
        [:td [:div {:class (str "validator-" invalid-birthdate)} "Today or earlier"]]
        [:td [:div {:class (str "validator-" invalid-address)} "Max 1000"]]
        [:td [:div {:class (str "validator-" invalid-insurance)} "Max 100"]]
        [:td]
        [:td]])]]])