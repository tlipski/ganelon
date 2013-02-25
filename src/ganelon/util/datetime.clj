;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.util.datetime
  "Convienience time and date manipulation functions."
)

(defn get-timezone-hour
  "Get current hour and minutes for a designated timezone.

Example:
(get-timezone-hour \"PST\")
;=> \"11:37\""
  [tz]
  (let [sdf (java.text.SimpleDateFormat. "HH:mm")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (.format sdf (java.util.Date.))))

(defn get-raw-hour-in-utc-seconds
  "Convert hour from a to seconds since 00:00 UTC. Can return value greater than (* 24 60 60).
  Disregards daylight saving settings for a timezone.

  Example:
  (get-raw-hour-in-utc-seconds 23 \"CET\")
  ;=> 79200"
  [hr tz]
  (when tz
    (let [tz (java.util.TimeZone/getTimeZone tz)
          tz-offset (int (/ (.getRawOffset tz) 1000))
          hr-seconds (* (Integer. hr) 60 60)]
      (cond
        (< (- hr-seconds tz-offset) 0) (+ (* 24 60 60) (- hr-seconds tz-offset))
        (> (- hr-seconds tz-offset) (* 24 60 60)) (- (- hr-seconds tz-offset) (* 24 60 60))
        true (- hr-seconds tz-offset)))))

(defn get-dst-hour-in-utc-seconds
  "Convert hour from a to seconds since 00:00 UTC. Can return value greater than (* 24 60 60).
Disregards daylight saving settings for a timezone.

Example:
(get-dst-hour-in-utc-seconds 23 \"CET\")
;=> 75600"
  [hr tz]
  (when tz
    (let [tz (java.util.TimeZone/getTimeZone tz)
          tz-offset (int (/ (+ (.getRawOffset tz) (.getDSTSavings tz)) 1000))
          hr-seconds (* (Integer. hr) 60 60)]
      (cond
        (< (- hr-seconds tz-offset) 0) (+ (* 24 60 60) (- hr-seconds tz-offset))
        (> (- hr-seconds tz-offset) (* 24 60 60)) (- (- hr-seconds tz-offset) (* 24 60 60))
        true (- hr-seconds tz-offset)))))

(defn get-hour-in-seconds
  "Get date's hour in seconds, for a specified timezone.

  Example:
  (str (java.util.Date.))
  ;=> \"Thu Feb 15 20:45:01 CET 2013\"
  (get-hour-in-seconds (java.util.Date.) \"CET\")
  ;=> 72000
  "
  [d tz]
  (let [sdf (java.text.SimpleDateFormat. "HH")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (let [hr (Integer. (.format sdf d))]
      (* hr 60 60))))

(defn format-date-and-time
  "Return date and time in 'yyyy-MM-dd HH:mm:ss tz' format.

Example:
(format-date-and-time (java.util.Date.) \"PST\")
;=> \"2013-02-15 11:46:32 PST\"
"
  [d tz]
  (let [sdf (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (str (.format sdf d) " " tz)))

(defn format-time
  "Return time in 'HH:mm:ss tz' format.

  Example:
  (format-date-and-time (java.util.Date.) \"PST\")
  ;=> \"11:46:32 PST\""
  [d tz]
  (let [sdf (java.text.SimpleDateFormat. "HH:mm:ss")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (str (.format sdf d) " " tz)))

(defn format-short-date-and-time
  "Return date and time in 'MM-dd_HH:mm' format.

Example:
(format-date-and-time (java.util.Date.) \"PST\")
;=> \"02-15_11:46\""
  [d tz]
  (let [sdf (java.text.SimpleDateFormat. "MM-dd_HH:mm")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (.format sdf d)))

(defn format-date
  "Return date in 'yyyy-MM-dd' format.

Example:
(format-date-and-time (java.util.Date.) \"PST\")
;=> \"2013-02-15\""
  [d tz]
  (let [sdf (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.setTimeZone sdf (java.util.TimeZone/getTimeZone tz))
    (str (.format sdf d))))
