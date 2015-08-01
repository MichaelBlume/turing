(ns turing.service
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :refer [info]]
    [clojure.string :refer [join split]]
    [clojure.data.codec.base64 :as b64]
    [compojure
     [core :refer [defroutes GET POST]]
     [route :as route]]
    [hiccup.core :refer [html]]
    [ring.util.codec :refer [form-decode]]
    [turing
     [html :refer [essay-submit-page
                   essay-vote-page
                   review-vote-page]]
     [db :refer [init-db insert-essay get-essay-ids
                 vote-sincere vote-insincere get-essay]]]))

(defn init []
  (info "starting turing")
  (init-db))

(defn handler [r]
  ((resolve `handler*) r))

(defn destroy []
  (info "stopping turing"))

(defn maybe-decode [s]
  (when s (form-decode s)))

(defn comma-split [s]
  (when (seq s)
    (split s #",")))

(comma-split "5")

(defn get-remaining [qs]
  (or
    (when-let [query (maybe-decode qs)]
      (when-let [rems (get query "remaining")]
        (if (seq rems)
          (->
            query
            (get "remaining")
            .getBytes
            b64/decode
            String.
            comma-split
            (->> (map #(Long/parseLong %))))
          [])))
    (shuffle
      (get-essay-ids))))

(defn read-controller [r]
  (let [remaining (get-remaining (:query-string r))]
    (if (seq remaining)
      (essay-vote-page
        (-> remaining first get-essay :essay)
        remaining)
      "Looks like you voted on all the essays, good job")))

(defn vote-controller [{:keys [body]}]
  (let [remaining (get-remaining body)
        sincere (get (form-decode body) "sincere")
        essay (get-essay (first remaining))]
    (if (= sincere "mu")
      "please go back and make a choice"
      (do
        (case sincere
          "sincere" (vote-sincere (first remaining))
          "insincere" (vote-insincere (first remaining))
          nil)
        (review-vote-page
          (:essay essay)
          remaining
          sincere
          (case (:sincere essay)
            "true" "sincere"
            "false" "insincere"
            "db read error"))))


    
    )
  )

(defroutes handler**
  (GET "/" [] "hello world")
  (GET "/read-essays" r
    (read-controller r))
  (GET "/submit-essay" []
    essay-submit-page) 
  (POST "/vote-essay" r
    (vote-controller r))
  (POST "/submit-essay" {:keys [body]}
    (let [submission (form-decode body)]
      (if (#{"true" "false"} (get submission "sincere"))
        (do
          (insert-essay submission)
          (html [:p "Thanks for submitting your essay!
                     Would you like to read some essays?"]))
        {:status 400 :body "sorry, you didn't indicate whether your essay was sincere or not, please go back"})))
  (route/not-found "wow you are very lost are you ok"))

(defn print-body [h] (fn [r] (pprint r) (h r)))

(defn slurp-body [h] (fn [r] (h (update-in r [:body] slurp))))

(def handler*
  (->> handler** print-body slurp-body))
