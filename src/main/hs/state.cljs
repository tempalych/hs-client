(ns hs.state
  (:require [reagent.core :as r]))

(def state (r/atom {:editMode false}))

(comment 
  @state
  
  )
