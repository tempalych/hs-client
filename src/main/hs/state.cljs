(ns hs.state
  (:require [reagent.core :as r]))

(def state (r/atom {:edit-mode false}))

(comment 
  @state
  
  )
