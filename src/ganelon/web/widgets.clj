;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.widgets
  (:require [ganelon.util.logging :as logging]
            [compojure.core :as compojure]
            [ganelon.util :as common]
            [noir.response :as response]
            [ganelon.web.dyna-routes :as dr])
  (:use [hiccup.core :only [html]]))

(def ^:dynamic *widget-scope* [])
(def ^:dynamic *current-widget* nil)

(defmacro with-div
"Generate random widget UID, and wrap its body with a DIV tag. Establishes widget-id scope, which can
be used for example by action links to pass current widget id.

Wraps & rest with hiccup.core/html for more consise code. If hiccup is not used/necessary, with-id or with-set-id macros
should be used.

Example:

(with-div
  [:h4 \"Hello\"])"
  [& rest]
  `(with-el :div ~@rest))

(defmacro with-widget
"Take widget UID as parameter, and wrap its body with a DIV tag. Establishes widget-id scope, which can
be used for example by action links to pass current widget id.

Wraps & rest with hiccup.core/html for more consise code. If hiccup is not used/necessary, with-id or with-set-id macros
should be used.

Example:

(with-id \"widget1\"
  [:h4 \"Hello\"])"
  [id & rest]
  `(with-set-id ~id
     (html [:div {:id ~id}
            ~@rest])))

(defmacro with-span
"Generate random widget UID, and wrap its body with a SPAN tag. Establishes widget-id scope, which can
be used for example by action links to pass current widget id.

Wraps & rest with hiccup.core/html for more consise code. If hiccup is not used/necessary, with-id or with-set-id macros
should be used.

Example:

(with-span  \"Hello\")"
  [& rest]
  `(with-el :span ~@rest))

(defmacro with-set-id
"Establishes widget-id scope for a supplied id,
which can be used for example by action links to pass current widget id.

Example:

(with-set-id \"test1\" (str \"<p>Hello \" (current-widget-id) \"</p>\"))
;=> \"<p>Hello test1</p>\""
  [id & rest]
  `(let [id# ~id]
     (binding [*widget-scope* (conj *widget-scope* id#)
               *current-widget* id#]
       ~@rest)))

(defmacro with-id
"Establishes widget-id scope, which can be used for example by action links to pass current widget id.

Example:

(with-id (str \"<p>Hello \" (current-widget-id) \"</p>\"))
;=> \"<p>Hello 6a70fd73-a39f-423e-9fe0-3339ea75376a</p>\""

  [& rest]
  `(with-set-id (str (java.util.UUID/randomUUID))
     ~@rest))

(defmacro with-el
"Generate random widget UID, and wrap its body with a tag of a supplied type. Establishes widget-id scope, which can
be used for example by action links to pass current widget id.

Wraps & rest with hiccup.core/html for more consise code. If hiccup is not used/necessary, with-id or with-set-id macros
should be used.

Example:

(with-el :h1 \"Hello\")
;=> \"<h1 id=\"b6d8d958-fb2e-4e29-94ba-bfb0a11f006b\">Hello</h1>\""
  [el & rest]
  `(with-id
     (html [~el {:id *current-widget*} ~@rest])))

(defn current-widget-id
"Return current widget id.

Example:

(with-id (str \"<p>Hello \" (current-widget-id) \"</p>\"))
;=> \"<p>Hello 6a70fd73-a39f-423e-9fe0-3339ea75376a</p>\""
  []
  *current-widget*)

(defn action
"Generate action tag using hiccup.core/html for a supplied tag. If *widget-id* scope is present, it is appended to params map.
The generated element has :onclick handler which invokes Ganelon.performButtonAction function.

Example:

(action :span \"test1\"  {:x 1} {:class \"btn\"} [:strong \"Invoke\"])
;=>\"<span class=\"btn\" onclick=\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\"><strong>Invoke</strong></span>\""
  [tag action-name params attributes & body]
  (html
    [tag (assoc attributes
      :onclick (str "return Ganelon.performButtonAction(this, '" action-name "', '"
                 (common/make-query (if (current-widget-id)
                                      (merge {:widget-id (str "#" (current-widget-id))} params)
                                      params)) "');"))
     body]))

(defn action-link
"Generate action link using hiccup.core/html. If *widget-id* scope is present, it is appended to params map - unless explictly provided.
The generated element has :onclick handler which invokes Ganelon.performButtonAction function.

Example:

(action-link \"test1\"  {:x 1} {:class \"btn\"} [:strong \"Invoke\"])
;=>\"<a class=\"btn\" href=\"#\" onclick=\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\"><strong>Invoke</strong></a>\""
  [action-name params attributes & body]
  (apply action :a action-name params (assoc attributes :href "#") body))

(defn action-button
"Generate action link using hiccup.core/html. If *widget-id* scope is present, it is appended to params map - unless explictly provided.
The generated element has :onclick handler which invokes Ganelon.performButtonAction function.

Example:

(action-button \"test1\"  {:x 1} {:class \"btn\"} [:strong \"Invoke\"])
;=>\"<button class=\"btn\" href=\"#\" onclick=\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\"><strong>Invoke</strong></button>\""
  [action-name params attributes & body]
  (apply action :button action-name params attributes body))

(defn with-ajax-loader
"Enhance attributes with Bootstrap's data-loading-text attribute set to  '<img src='/ganelon/img/loading.gif'/>'.

Example:

(action-link \"test1\" {:x 1} (with-ajax-loader {:class \"btn\"}) [:strong \"Invoke\"])
\"<a class=\\\"btn\\\" data-loading-text=\\\"&lt;img src='/ganelon/img/loading.gif'/&gt;\\\" href=\\\"#\\\" onclick=\\\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\\\"><strong>Invoke</strong></a>\""
  [attributes]
  (assoc attributes :data-loading-text "<img src='/ganelon/img/loading.gif'/>"))

(defn action-loader-link
"Just as action-link, but with with-ajax-loader applied to attributes.

(action-loader-link \"test1\" {:x 1} {:class \"btn\"} [:strong \"Invoke\"])
\"<a class=\\\"btn\\\" data-loading-text=\\\"&lt;img src='/ganelon/img/loading.gif'/&gt;\\\" href=\\\"#\\\" onclick=\\\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\\\"><strong>Invoke</strong></a>\""
  [action-name params attributes & body]
  (apply action-link action-name params (with-ajax-loader (assoc attributes :href "#")) body))

(defn action-loader-button
"Just as action-button, but with with-ajax-loader applied to attributes.

(action-loader-button \"test1\" {:x 1} {:class \"btn\"} [:strong \"Invoke\"])
\"<button class=\\\"btn\\\" data-loading-text=\\\"&lt;img src='/ganelon/img/loading.gif'/&gt;\\\" href=\\\"#\\\" onclick=\\\"return Ganelon.performButtonAction(this, 'test1', 'x=1');\\\"><strong>Invoke</strong></button>\""
  [action-name params attributes & body]
  (apply action-button action-name params (with-ajax-loader attributes) body))


(defn action-form
"Generate form which on submit invokes designated action.

If widget-id in scope is present, it is added to parameters map - unless explictly provided.

(action-form \"test1\" {:y 1} {:class \"well\"} [:input {:type \"submit\"}])
;=> \"<form class=\\\"well\\\" method=\\\"POST\\\" onsubmit=\\\"return Ganelon.performFormAction(this, 'test1', 'y=1&amp;widget-id=%23');\\\"><input type=\\\"submit\\\" /></form>\"
"
  [action-name params attributes & body]
  (html [:form (merge {:onsubmit (str "return Ganelon.performFormAction(this, '" action-name "', '"
                                   (common/make-query (merge {:widget-id (str "#" (current-widget-id))} params)) "');")
                       :method "POST"} attributes)
         body]))
