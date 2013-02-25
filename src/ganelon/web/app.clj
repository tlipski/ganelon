;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.app
  "This namespace provides additional handlers for Ganelon web apps."
  (:require [noir.util.middleware]
            [ring.middleware.resource]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [ganelon.web.actions :as actions]
            [ganelon.web.dyna-routes :as dyna-routes]))


(defn app-handler
"Provide basic Ganelon features:
* :default routes from ganelon.web.dyna-routes, providing actions and other routes defined in this group (e.g. using defpage or defjsonaction)
* Access to static resources for package 'public' with (compojure.route/resources \"/\")
* keywordized params through ring.middleware.keyword-params/wrap-keyword-params and ring.middleware.params/wrap-params

Takes additional Ring routes/handlers as parameters.

It is not required to use app-handler to access all of the Ganelon's features - you can assemble them in a different way,
according to the requirements of the application being developed.

Example usage:

(jetty/run-jetty
  (->
    (ganelon.web.app/app-handler
      (compojure.core/GET \"/t1\" [] \"Hi!\")
      (ganelon.web.app/javascript-actions-route))))"

  [& routes]
  (->
    (noir.util.middleware/app-handler
      (conj
        routes
        (dyna-routes/route-ns-fn :default)
        (route/resources "/")))
    (ring.middleware.keyword-params/wrap-keyword-params)
    (ring.middleware.params/wrap-params)))

(defn javascript-actions-route
"Return compojure route with access to JavaScript interface for actions, handled by ganelon.web.actions/javascript-actions-handler.

The route responds to GET /ganelon/actions.js.

Example usage:
(jetty/run-jetty
  (->
    (ganelon.web.app/app-handler
      (compojure.core/GET \"/t1\" [] \"Hi!\")
      (ganelon.web.app/javascript-actions-route))))"
  []
  (compojure/GET "/ganelon/actions.js" []
    (actions/javascript-actions-handler)))
