;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.helpers
  "Convienience utility functions - lib-noir dependent."
  (:require [noir.request :as request]))

(defn req-params
  "Access :params from noir.request/*request*."
  []
  (:params request/*request*))

(defn current-request-url
"Extract current request URL (without query or anchor) from noir.request/*request*."
  []
  (let [r request/*request*]
    (println r)
    (str (name (:scheme r)) "://" (:server-name r) ":" (:server-port r) (:uri r))))

(defn get-request-parameter
"Get noir.request/*request* parameter by name."
  [name]
  (get (:params request/*request*) name))

(defn current-request-host-part
"Extract current request URL (without local part, query or anchor) from noir.request/*request*."
  []
    (let [r request/*request*]
      (str (name (:scheme r)) "://" (:server-name r) ":" (:server-port r))))
