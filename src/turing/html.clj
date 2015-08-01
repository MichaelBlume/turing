(ns turing.html
  (:require
    [clojure.string :refer [join split]]
    [ring.util.codec :refer [form-encode]]
    [clojure.data.codec.base64 :as b64]
    [hiccup.core :refer [html]]))

(def essay-submit-page
  (html
    [:html
     [:body
      [:p "submit your essay here!"]
      [:p "This is a very stupid essay submission page.
           It will not make any attempt to save your work.
           I strongly recommend writing in a proper text editing
           program
           and copy/pasting here."]
      [:form {:method "post"}
       [:div
        [:label {:for "email"} "E-mail:"]
        [:input {:type "email" :id "email" :name "email"}]]
       [:div
        [:label {:for "sincere"} "Is this essay your sincere opinion?"]
        [:select {:name "sincere" :id "sincere"}
         [:option {:value "mu"} "Please choose one"]
         [:option {:value "true"} "Yes, this is my sincere opinion"]
         [:option {:value "false"} "No, this is devil's advocacy"]]]
       [:div
        [:label {:for "essay"} "Your essay:"]
        [:textarea {:id "essay" :name "essay"}]]
       [:div {:class "button"}
        [:button {:type "submit"} "Submit your essay"]]]]]))

(defn encode-remaining [remaining]
  (->
    remaining
    (->> (join ","))
    .getBytes
    b64/encode
    String.))

(defn essay-vote-page [essay remaining]
  (html
    [:html
     [:body
      [:p "here's an essay!"]
      [:pre essay]
      [:form {:action "/vote-essay" :method "post"}
       [:div
        [:label {:for "sincere"} "Do you think this essay was sincere, or do you think it was devil's advocacy?"]
        [:select {:name "sincere" :id "sincere_vote"}
         [:option {:value "mu"} "Please choose one"]
         [:option {:value "sincere"} "I think this is sincere"]
         [:option {:value "insincere"} "I think this is devil's advocacy"]
         [:option {:value "decline"} "I decline to vote on this essay"]]]
       [:input {:type "hidden" :name "remaining" :value (encode-remaining remaining)}]
       [:div {:class "button"}
        [:button {:type "submit"} "Continue"]]]]]))

(defn review-vote-page [essay remaining vote actual]
  (html
    [:html
     [:body
      [:p
       "you chose " vote " for this essay, when "
       "actually this essay was " actual]
      [:p
       [:a {:href (str
                    "/read-essays?"
                    (form-encode
                      {"remaining" (encode-remaining
                                     (rest remaining))}))}
        "Next Essay"]]
      [:p
       "In case you'd like to reread the essay with this knowledge, here it is:"]
      [:p [:pre essay]]]]))
