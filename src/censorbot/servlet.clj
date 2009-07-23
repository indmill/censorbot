(ns censorbot.servlet
  (:gen-class :extends com.google.wave.api.AbstractRobotServlet)
  (:import
    [com.google.appengine.api.users UserServiceFactory]
    [com.google.wave.api Blip Event EventType RobotMessageBundle TextView Wavelet])
  (:require [clojure.contrib.str-utils  :as str-utils]
            [clojure.contrib.str-utils2 :as str-utils2]))

(def swearwords
  ["boobies" "willies" "toilet" "fart" "mimsy" "wee" "poo" "burp"])

(defn append-blip
  [wavelet message]
  (let [view (.getDocument (.appendBlip wavelet))]
    (.append view message)))

(defn welcome-if-new
  [bundle wavelet]
  (if (.wasSelfAdded bundle) 
    (append-blip wavelet (str "Hello. I'm a children's playground censorbot! No swearing please"))))

(defn blip-submitted-events
  [events]
  (filter (fn [e] (= (EventType/BLIP_SUBMITTED) (.getType e))) events))

(defn censor-words
  [words text]
  (if (empty? words)
    text
    (str-utils2/replace 
      (censor-words (rest words) text) (re-pattern (format "(?i:%s)" (first words))) "*BEEP*")))

(defn censor
  [blip]
  (let [document (.getDocument blip)
        text (.getText document)]
    (.replace document (censor-words swearwords text))))

(defn -processEvents
  [this bundle]
  (let [wavelet (.getWavelet bundle)]
    (welcome-if-new bundle wavelet)
    (doseq [event (blip-submitted-events (.getEvents bundle))]
      (censor (.getBlip event)))))
