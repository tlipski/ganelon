;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.util.logging
  "Additional webapp-related utility logging functions, with features such as noir.request/*request* capture
  or logging and returning of a value or execution duration logging."
  (:require [clojure.tools.logging :as logging]
            [ganelon.util :as common]
            [noir.request :as request]
            [noir.session :as sess]))

(defn enrich-log-msg
"Enhance log message with :remote-addr and :uri from noir.request/*request* and
session attribute named __user.

Parameters:
  msg - log message

Example:
(enrich-log-msg \"This is a test!\")
;=> \"This is a test! REQUEST: {:remote-addr \\\"127.0.0.1\\\", :uri \\\"/routing/samplel\\\"} USER: {:screenname \\\"admin\\\"}\""
  [msg]
  (let [r request/*request*
        msg (if (coll? msg) (doall (interpose " " msg)) [msg])
        ]
    (if-not (instance? clojure.lang.Var$Unbound r)
      (str (reduce str msg)
        " REQUEST: "
        (common/make-sorted-map (select-keys r [:remote-addr :uri ]))
        " USER: "
        (common/make-sorted-map (sess/get "__user")))
      (reduce str msg))))

(defmacro log
  "Log enriched message with appropriate clojure.tools.logging log level, taking potential noir.request/*request*
  into account
Parameters:
 level - log level keyword, e.g. :info, :debug, :warn, :error
 & msg - values to be logged

Example:
(log :info \"Logged in user \" {:login \"test\"})
;INFO: Logged in user {:login \"test\"}
;=> nil
"
  [level & msg]
  `(logging/log ~*ns* ~level nil (enrich-log-msg ~(into [] msg))))

(defmacro handle-exception
  "Log exception using clojure.tools.logging with :error log level and enriched message.
Parameters:
  msg - diagnostic message, possibly explaining the conditions in which the error has occured
  e - java.lang.Throwable, which will be logged

Example:
(handle-exception \"No need to panic!\" (java.lang.Exception. \"ooops\"))
;SEVERE: No need to panic!
;java.lang.Exception: ooops
;	at ganelon.util.logging$eval4008.invoke(NO_SOURCE_FILE:25)
;	at clojure.lang.Compiler.eval(Compiler.java:6511)
;	at clojure.lang.Compiler.eval(Compiler.java:6477)
;=> nil"

  ([e] `(let [e# ~e] (handle-exception (.getMessage e#) e#)))
  ([msg e] `(logging/log ~*ns* :error ~e (enrich-log-msg ~msg))))

(defmacro log-val
  "Log message with returned value. Then return (last val). Useful for tracing values without introducing unnecessary scopes.
Parameters:
  level - log level, e.g. :info, :debug, :warn, :error
  msg - message text
  val - values that will be logged and the last of them will be returned

Example:
(log-val :info \"test! \" 1 2 3)
;INFO: test! 123
;=> 3"
  [level msg & val]
  `(do
     (let [val# (vector ~@val)]
       (logging/log ~*ns* ~level nil (str ~msg " " (enrich-log-msg val#)))
       (last val#))))

(defmacro log-start-end
  "Log execution time, generating random UUID to mark start and end entries in logs.
Parameters:
  level - log level, e.g. :info, :debug, :warn, :error
  msg - message text
  & body - body to be evaluated (and returned)

Example:
(log-start-end :info :operation-x (Thread/sleep 1000) :result-y)
;INFO: 13608266406980 1360826640698 0e4ba5ba-1e93-43d1-adcd-78eb6d37f3c9;Started :operation-x Thread[main,5,main]
;INFO: 13608266406981 1360826640698 0e4ba5ba-1e93-43d1-adcd-78eb6d37f3c9;Ended :operation-x Thread[main,5,main], time: 1003ms
;=> :result-y"
  [level msg & body]
  `(do
     (let [tim# (System/currentTimeMillis)
           id# (str tim# " " (java.util.UUID/randomUUID))
           lvl# ~level
           msg# ~msg]
       (logging/log ~*ns* lvl# nil (str tim# "0 " id# ";Started " msg# " " (Thread/currentThread)))
       (try
         ~@body
         (finally
           (logging/log ~*ns* lvl# nil (str tim# "0 " id# ";Ended " msg# " " (Thread/currentThread)
                                      ", time: " (- (System/currentTimeMillis) tim#) "ms")))))))
