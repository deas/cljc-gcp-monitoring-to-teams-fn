(ns gcp-monitoring-to-teams-test
  (:require [cljs.test
             :refer (deftest async is testing use-fixtures run-tests)
             ;; :refer-macros [deftest is testing run-tests]
             ]
            [gcp-monitoring-to-teams :refer (handle-request gcp-to-teams)]
            ["fs" :as fs]
            ["strftime" :as strftime]
            ;; ["js-time-diff" :as td]
            [goog.string :as gstring]
            goog.string.format
            ))


(defn json-load[path enc]
  (->> (fs/readFileSync path enc)
       (.parse js/JSON)))


(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))

(defn logging-test-fixture [f]
  ;; (enable-logging!)
  (f)
  )


;; Logs do no appear in tests?
;; (enable-console-print!)
;; (enable-logging!)

;; TODO: Fixture breaks tests with node?
;; (use-fixtures :once logging-test-fixture)

(deftest handler-test
  (testing "Handler works"
    (async done
           (done)
           #_(let [js-event (clj->js {:foo "bar"})
                 context nil
                 callback (fn [response]
                            (is (nil? (:error response)) (str response))
                            (done))]
             (handle-request nil nil ;; js-event context callback
                             ))))
  )

(comment
  (+ 1 1)
  
  (let [gcp-payload (-> (json-load "samples/incident.json" "utf-8")
                        (js->clj :keywordize-keys true))]
    (gcp-to-teams gcp-payload)
    ;; (js/Date. (:started_at gcp-payload))
    )
)