(ns gcp-monitoring-to-teams
  (:require [taoensso.timbre :as timbre
             :refer [infof warnf]]
            [clojure.string :refer [upper-case]]
            [httpurr.client.node :as http]
            [promesa.core :as p]
            #?(:cljs ["strftime" :as strftime])))

(defn start [] (pr "Dev Started"))

(defn reload [] (pr "Dev Reloaded"))

(defn to-json [clj]
  #?(:cljs (.stringify js/JSON (clj->js clj))))

(defn to-clj [json]
  #?(:cljs (js->clj json :keywordize-keys true)))

(defn to-date-string [millis]
  #?(:cljs (strftime "%B %d, %Y %H:%M:%S" (js/Date. millis))))

(timbre/merge-config!
   {:output-fn
    (fn [data]
      (let [{:keys [level ?err #_vargs msg_ ?ns-str ?file hostname_
                    timestamp_ ?line]} data
            output-data (cond->
                         {:timestamp (force timestamp_)
                          :host (force hostname_)
                          :severity (upper-case (name level))
                          :message (force msg_)}
                          (or ?ns-str ?file) (assoc :ns (or ?ns-str ?file))
                          ?line (assoc :line ?line)
                          ?err (assoc :err (timbre/stacktrace ?err {:stacktrace-fonts {}})))]
        (to-json output-data)))})


(defn gcp-to-teams
 "https://docs.microsoft.com/de-de/graph/api/resources/chatmessage?view=graph-rest-1.0
  https://docs.microsoft.com/de-de/outlook/actionable-messages/message-card-reference"
  [payload]
  (let [incident (:incident payload)
        started_at (* 1000 (:started_at incident))
        ended_at (when-let [ended_at (:ended_at incident)] (* 1000 ended_at))
        {:keys [state incident_id summary url policy_name condition_name resource_id resource_name]} incident
        title (str "[Incident"
                   (if (= "open" state)
                     " opened "
                     " closed ")
                   "for " policy_name "](" url ")")
        facts (cond-> [{:title "Incident ID" :value incident_id}
                       {:title "Condition" :value condition_name}
                       {:title "Started at" :value (to-date-string started_at)}]
                resource_name (conj {:title "Resource name" :value resource_name})
                ended_at (conj {:title "Ended at" :value (to-date-string ended_at)}))

        body (cond-> [{:type "TextBlock" :text title :color (if (= "open" state) "attention" "default")}
                      {:type "FactSet"
                       :facts facts}]
               summary (conj {:type "TextBlock" :text summary}))]
    (infof "Creating teams message for incident %s, state is %s" incident_id state)
    {:type "message"
     :importance (if (= "open" state) "urgent" "normal")
     :attachments [{:contentType "application/vnd.microsoft.card.adaptive"
                    :contentUrl nil
                    :content {:$schema "http://adaptivecards.io/schemas/adaptive-card.json"
                              :type "AdaptiveCard"
                              :version "1.2"
                              :body body}}]}))

(defn handle-request
  "https://expressjs.com/en/api.html"
  [req res]
  (let [auth-token (.. js/process -env -AUTH_TOKEN)
        query (.-query req)
        teams-endpoint (goog.object/get query "teams-endpoint")
        req-auth-token (goog.object/get query "auth-token")
        auth-valid (= auth-token req-auth-token)]
    (infof "Auth valid %s" auth-valid)
    (if (= auth-token req-auth-token)
      (let [payload (-> (.-body req) to-clj)
            {{:keys [incident_id started_at]} :incident} payload
            payload-valid (and incident_id #_started_at)] ;; The test from the UI does not send started_at
        (infof "Got incident %s, started at %s" incident_id started_at)
        (cond
          payload-valid (let [teams-payload (-> (gcp-to-teams payload) to-json)
                              opts {:timeout 2000}
                              teams-req {:headers {"Content-Type" "application/json"
                                                   "Content-Length" (count teams-payload)}
                                         :body teams-payload}]
                          (-> (http/post teams-endpoint teams-req opts)
                              (p/then (fn [r]
                                        (infof "Received %s" r)
                                        (doto res
                                          (.status (:status r))
                                          (.send "Status forwarded"))))
                              (p/catch (fn [r]
                                         (warnf "Received %s" r)
                                         (doto res
                                           (.status 502)
                                           (.send "Exception"))))))
          :else (doto res
                  (.status 400)
                  (.send "Invalid request"))))
      (doto res
        (.status 401)
        (.send "Invalid auth_token")))))


(comment
  ;; (.stringify js/JSON #js {:a 1})
  (.. js/process -env -HOME)
  (def s (atom nil))
  (let [url  "http://localhost:9876"
        opts {:timeout 2000}
        req {:headers {"Content-Type" "application/json"}
             :body "{}"}]
    (-> (http/post url req opts)
        (p/then #(reset! s %))
        (p/catch #(reset! s %))))
  @s
  )