;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.dyna-routes
"This names provides the equivalent of webnoir's defpage/add-middleware macros.
What is does, is to essentially provide dynamic list of routes and ability to define
multiple routes in many definition files.

The route paths should not overlap here, and default routes (files, not-found, etc.) should be set explicitly.

It is entirely possible to access other features of Ganelon without using dyna-routes. Or just use dyna-routes
and skip other features."
  (:require [compojure.core :as compojure]))

(defonce ^:dynamic *ROUTE-NAME-SPACES* (atom {}))

(defn setroute!
"Register route in a namespace. When no namespace is provided, use :default namespace.

Routes from a namespace can be referenced later on by route-ns-fn.

Example:

(setroute! :example2
  (compojure.core/GET \"/routing/sample2\" []
    \"Hello world !!!\"))"
  ([name route] (setroute! :default name route))
  ([name-space name route]
    (swap!
      *ROUTE-NAME-SPACES*
      (fn [x]
        (assoc x name-space
          (let [grp-defn (or (get x name-space) {:routes {} :middleware {}})]
            (assoc grp-defn :routes (assoc (:routes grp-defn) name route))))))))

(defn setmiddleware!
"Register middleware in a namespace. When no namespace is provided, use :default namespace.

This feature is an alternative to classical approach with Ring handlers/middleware chain.

Middleware functions from a namespace can be referenced later on by route-ns-fn.

Example usage:

(dr/setmiddleware! :i18n-detect
  #(fn [request] (detect-lang % request)))"
  ([name mw] (setmiddleware! :default name mw))
  ([name-space name mw]
    (swap!
      *ROUTE-NAME-SPACES*
      (fn [x]
        (assoc x name-space
          (let [grp-defn (or (get x name-space) {:routes {} :middleware {}})]
            (assoc grp-defn :middleware (assoc (:middleware grp-defn) name mw))))))))

(defn- build-middleware-fn [middlewares app]
  (if (not-empty middlewares)
    ((first middlewares) (build-middleware-fn (rest middlewares) app))
    app))

(defn- route-by-ns [name-space request]
  ((build-middleware-fn
     (vals (:middleware (get @*ROUTE-NAME-SPACES* name-space)))
     (fn [req]
       (apply compojure/routing
         req
         (vals (:routes (get @*ROUTE-NAME-SPACES* name-space))))))
    request))

(defn route-ns-fn 
"Return ring handler for a namespace provided as a parameter. Then handler provides
all of the routes and middleware registered in a designated namespace.

Example usage:

(compojure.core/GET \"/routing/sample5/*\" []
  (-> (ganelon.web.dyna-routes/route-ns-fn :sample-admin)
      wrap-fake-admin)))"
  [name-space]
  (fn [request] ;use of compojure/routes would leave us with empty handler list
    (route-by-ns name-space request)))

(defn default-routes-fn
"Return ring handler with middleware and routes registered for a :default namespace.

Example usage:

(compojure.core/ANY \"/*\" []
  (-> (ganelon.web.dyna-routes/default-routes-fn)
    wrap-fake-admin)))"
  []
  (route-ns-fn :default ))

(defmacro defpage-ns
"Macro inspired by Noir's defpage, but with a full access to Compojure's parameters support.
Registers route for a provided path and parameters using compojure.core/ANY macro in a supplied namespace.

Example usage:

(ganelon.web.dyna-routes/defpage-ns :default \"/routing/sample\" []
  \"Hello world!\")"
  ([name-space path args & body]
  `(setroute! ~name-space ~path
    (compojure/ANY ~path ~args
      ~@body))))

(defmacro defpage
"Macro inspired by Noir's defpage, but with a full access to Compojure's parameters support.
Registers route for a provided path and parameters using compojure.core/ANY macro in a :default namespace.

Example usage:

(ganelon.web.dyna-routes/defpage \"/routing/sample\" []
  \"Hello world!\")"
  ([path args & body]
    `(defpage-ns :default ~path ~args ~@body)))





