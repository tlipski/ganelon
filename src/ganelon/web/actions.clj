;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.actions
  "This namespace contains actions support functions and macros."
  (:require [ganelon.util.logging :as logging]
            [compojure.core :as compojure]
            [noir.response :as response]
            [noir.request :as request]
            [hiccup.core :as hiccup]
            [ganelon.util :as common]
            [ganelon.web.dyna-routes :as dr]
            [ganelon.web.ui-operations :as ui]
            ))

(def ^:dynamic *widget-id*
  "Id of a current widget, for example provided as a 'widget-id' GET/POST parameter."
  nil)
(def ^:dynamic *operation-queue* (atom []))
(defonce ACTION-REGISTRY (atom {}))

(defmacro ACTION
"Define and register basic action, by adding: logging and exception handling to compojure.core/ANY handler with path
composed from '/a/' prefix and action name.

Example:

(ACTION \"sample4\" [x y]
  (noir.response/json {:x x :y y :z \"!\"}))

This macro does not register handler with ganelon.web.dyna-routes."
  [name params & body]
  `(do
    (swap! ACTION-REGISTRY #(assoc % ~name ~(into [] (map str params))))
    (compojure/ANY ~(str "/a/" name) ~params
       (try
         (logging/log :debug " invoke " ~(str name))
         (logging/log-val :trace " result " ~(str name) (do ~@body))
         (catch Throwable t#
           (logging/handle-exception (str "error while invoking action " ~(str name)) t#)
           (response/json
             [{:type "error"
               :message (hiccup/html
                          [:p "An error has occured during the processing of your request. We have logged what has happened, and will look into
                    the problem with details. If the problem persists and causes any trouble to you, you can also <a href=\"/contact\">contact us</a>."]
                          [:p "The error message was " [:b (.getMessage t#)]])
               }]))))))

(defn wrap-set-widget-id
"Bind 'widget-id' req parameter to a special variable *widget-id*.

This middleware requires wrap-keyword-params."
  [app]
  (fn [req]
    (binding [*widget-id* (:widget-id (:params req))]
      (app req))))

(defn put-operation!
"Register operation to be returned as a part of the action response. Returns nil in case the put-operation!
is used at the end of an action body.

Example:

(actions/put-operation! (ui/notification \"Success\"
  (h/html \"Message set to: \" [:b (hiccup.util/escape-html msg)])))"
  [operation]
  (swap! *operation-queue* #(conj % operation))
  nil)

(defmacro JSONACTION
"Define and register JSON action, by adding: logging and exception handling to compojure.core/ANY handler with path
composed from '/a/' prefix and action name.

If the body result is a not collection or is a map, it is wrapped in a vector. The final result is passed to
noir.response/json function.

Example:

(JSONACTION \"sample4\" [x y]
  {:x x :y y :z \"!\"})

This macro does not register handler with ganelon.web.dyna-routes."
  [name params & body]
  `(ACTION ~name ~params
     (binding [*operation-queue* (atom [])]
       (let [res# (do ~@body)]
         (if (empty? @*operation-queue*)
           (response/json (if (and (coll? res#) (not (map? res#))) res# [res#]))
           (response/json (if res#
                            (flatten [@*operation-queue* (if (and (coll? res#) (not (map? res#))) res# [res#])])
                            @*operation-queue*)))))))

(defmacro WIDGETACTION
"Define and register widget action, by adding: logging and exception handling to compojure.core/ANY handler with path
composed from '/a/' prefix and action name.

The result of body is used as a value for ganelon.web.ui-operations/replace-with. The id is taken from *widget-id*
special variable. The operation is wrapped with JSONACTION macro.

If ~body returns nil, the widget is not updated.

Example:

(WIDGETACTION \"sample4\" [x y]
  (str \"<p>Test!\" (int x) \"</p>\"))

This macro does not register handler with ganelon.web.dyna-routes."
  [name params & body]
  `(->
     (JSONACTION ~name ~params
      (when-let [res# (do ~@body)]
        (ui/replace-with
          (or (:widget-id (:params request/*request*)) (get (:params request/*request*) "widget-id"))
          res#)))
     wrap-set-widget-id))

(defmacro defaction
"This macro wraps ACTION with ganelon.web.dyna-routes/setroute! function, registering action for a default
dyna-routes handler.

Example:

(defaction \"sample4\" [x y]
  (noir.response/json {:x x :y y :z \"!\"}))"
  [name params & body]
  `(dr/setroute! ~name
     (ACTION ~name ~params ~@body)))

(defmacro defjsonaction
"This macro wraps JSONACTION with ganelon.web.dyna-routes/setroute! function, registering action for a default
dyna-routes handler.

Example:

(defjsonaction \"sample4\" [x y]
  {:x x :y y :z \"!\"})"
  [name params & body]
  `(dr/setroute! ~name
     (JSONACTION ~name ~params ~@body)))

(defmacro defwidgetaction
"This macro wraps JSONACTION with ganelon.web.dyna-routes/setroute! function, registering action for a default
dyna-routes handler.

Example:

(defwidgetaction \"sample4\" [x y]
  (str \"<p>Test!\" (int x) \"</p>\"))"
  [name params & body]
  `(dr/setroute! ~name
     (WIDGETACTION ~name ~params ~@body)))

(defn- javascriptize [v]
  (common/mreplace v
    [["-" "_"]
     ["/" "_"]]
    ))

(defn javascript-action-interface
"Generate action interface for JavaScript. Dash (-) and slash (/) characters are translated to an underscore (_).
If the action does not use widget-id parameter, it will be added implicitly at the end of params.

Example:

(javascript-action-interface ['test-action ['widget-id 'x 'y]])
;=> \"GanelonAction.test_action=function(widget_id,x,y, onSuccess, onError){Ganelon.performAction('test-action','widget-id='+encodeURIComponent(widget_id)+'&x='+encodeURIComponent(x)+'&y='+encodeURIComponent(y)+'',onSuccess,onError);};\"
(javascript-action-interface ['test-action ['x 'y 'z]])
;=> \"GanelonAction.test_action=function(x,y,z,widget_id, onSuccess, onError){Ganelon.performAction('test-action','x='+encodeURIComponent(x)+'&y='+encodeURIComponent(y)+'&z='+encodeURIComponent(z)+'&widget-id='+encodeURIComponent(widget_id)+'',onSuccess,onError);};\""
  [[name params]]
  (let [params (if (not (some #{"widget-id"} (map str params))) (conj params "widget-id") params)]
    (str
      "GanelonAction." (javascriptize name) "=function("
      (apply str (interpose "," (map javascriptize params))) ", onSuccess, onError){"
      "Ganelon.performAction('" name "','"
      (apply str (interpose "&" (map #(str %1 "='+encodeURIComponent(" (javascriptize %1) ")+'") params)))
      "',onSuccess,onError);};")))


(defn javascript-actions-handler
"Ring handler that returns a JavaScript file containing action interfaces for all registered actions.
Usually, it is more convienient to use ganelon.web.app/javascript-actions-handler, which provides a route
to this handler for GET on /ganelon/actions.js.

Example use:

(compojure/GET \"/ganelon/actions.js\" []
    (actions/javascript-actions-handler))"
  []
  (response/content-type "text/javascript"
    (apply str
      "var GanelonAction=GanelonAction||{};"
      (map javascript-action-interface @ACTION-REGISTRY))))