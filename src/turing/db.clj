(ns turing.db
  (:require [clojure.java.jdbc :refer :all]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "database.db"})

(defn exists? [db table-name]
  (try
    (query db (str "select 1 from " table-name))
    true
    (catch Exception e false)))

(defn create-if-absent [db table-name & specs]
  (when-not (exists? db table-name)
    (db-do-commands
      db
      (apply create-table-ddl table-name specs))))

(defn init-db []
  (create-if-absent db 
    "entry"
    [:id "integer primary key"]
    [:email :text]
    [:sincere :text]
    [:essay :text]
    [:sincere_votes :integer]
    [:insincere_votes :integer]))

(defn insert-essay [{:strs [email sincere essay]}]
  (insert!
    db
    :entry
    {:email email
     :essay essay
     :sincere sincere
     :sincere_votes 0
     :insincere_votes 0}))

(defn get-essay-ids []
  (map :id (query db "select id from entry")))

(defn get-essay [id]
  (first (query db ["select essay, sincere from entry where id = ?" id])))

(defn vote-sincere [id]
  (db-do-commands
    db
    (str "update entry set sincere_votes=sincere_votes+1 where id = " (long id))))

(defn vote-insincere [id]
  (db-do-commands
    db
    (str "update entry set insincere_votes=insincere_votes+1 where id = " (long id))))
