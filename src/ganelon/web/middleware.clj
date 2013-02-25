;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.middleware
"Simple middleware utility functions."
  )

;(defn fix-utf-8-header [request handler]
;  "Noir utf-8 middleware does not support other content types then text/html - e.g. json responses"
;  (let [resp (handler request)
;        ct (get-in resp [:headers "Content-Type"])
;        new-ct (if (and ct (re-matches #".*charset=.*" ct))
;      ct
;      (str ct "; charset=utf-8"))]
;    (assoc-in resp [:headers "Content-Type"] new-ct)))

(defn- cdnify-images [request handler cdn-home-url]
  (let [resp (handler request)
        ct (get-in resp [:headers "Content-Type"])]
    (if (and (or (.matches (or ct "") "^text/html.*") (.matches (or ct "") "^application/json.*")) (string? (:body resp)))
      (assoc resp :body (.replaceAll
                          (.replaceAll
                            (.replaceAll (:body resp) "src\\=\\\"\\/" (str "src=\"" cdn-home-url))
                            "src\\=\\'\\/" (str "src='" cdn-home-url))
                          "src\\=\\\\\\\"\\/" (str "src=\\\\\\\"" cdn-home-url)))
      resp)))

(defn wrap-cdnify-images
"For a response type of text/html or application/json, alters all src attribute values, which start with / by
prepending cdn-home-url prefix."
  [handler cdn-home-url]
  (fn [request]
    (cdnify-images request handler cdn-home-url)))

(defn wrap-x-forwarded-for
"Replace :remote-addr in request with a value of x-forwarded-for request header.

CAUTION: use only when the Java process' port is accessible from localhost only."
  [handler]
  (fn [request]
    (if-let [xff (get-in request [:headers "x-forwarded-for"])]
      (handler (assoc request :remote-addr (last (.split xff "\\s*,\\s*"))))
      (handler request))))

;(defn wrap-fix-uft8-handler [handler]
;  (fn [request] (fix-utf-8-header request handler)))

