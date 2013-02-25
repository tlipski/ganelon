;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.util
  "Convienience utility functions."
  (:require [clojure.core.memoize :as memoize]
            [noir.request :as request]))

(defn nvl
  "If v is null, return empty string, otherwise return v.

Example:
[(nvl \"!\") (nvl nil)]
;=> [\"!\" \"\"]"
  [v]
  (if (nil? v) "" v))

(defn req-params
  "Access :params from noir.request/*request*."
  []
  (:params request/*request*))

(defn make-query
  "Build URL query string from provided map.

Example:
(make-query {:a \"1\" :b \"%\" :c \" \"})
;=> \"a=1&c=+&b=%25\""
  [params]
  (clojure.string/join "&" (map #(str (name (first %)) "=" (java.net.URLEncoder/encode (str (second %))))
                             params)))

(defn current-request-url
"Extract current request URL (without query or anchor) from noir.request/*request*."
  []
  (let [r request/*request*]
    (str (:scheme r) "://" (:server-name r) ":" (:server-port r) (:uri r))))

(defn get-request-parameter
"Get noir.request/*request* parameter by name."
  [name]
  (get (:params request/*request*) name))

(defn current-request-host-part
"Extract current request URL (without local part, query or anchor) from noir.request/*request*."
  []
    (let [r request/*request*]
      (str (name (:scheme r)) "://" (:server-name r) ":" (:server-port r))))

(defn make-sorted-map
"Transform standard map into sorted - assuring the same order of keywords.

Example:
(make-sorted-map {:z :2 :a :b :c :d :x :y})
;=> {:a :b, :c :d, :x :y, :z :2}
"
  [m]
  (into (sorted-map) m))

(defn find-substring
"Find a substring index nearest to a designated position.

Example:
(find-substring \"abc abc abc abc\" \"abc\" 3)
;=> 4
(find-substring \"abc abc abc abc\" \"abc\" 5)
;=> 4
(find-substring \"abc abc abc abc\" \"abc\" 7)
;=> 8"
  [text substring position]
  (if (< position (.length text))
    (let [first-after (.indexOf text substring position)
          last-before (.lastIndexOf text substring position)
          first-distance (- first-after position)
          last-distance (- position last-before)]
      (if (> first-distance last-distance)
        last-before
        first-after))
    (.length text)))


(defn try-to-extract-sentence
"Attempt to extract a sentence ending with a '.' as near to expected length as possible,
but not further than max-distance. If '.' is not found, look for a nearest ' ' (space) to expected-length in text.

Example:
(try-to-extract-sentence \"abc.abc.abc abc.\" 10 1)
;=> \"abc.abc.abc\"
(try-to-extract-sentence \"abc.abc.abc abc.\" 10 5)
;=> \"abc.abc.\""
  [text expected-length max-distance]
  (when text
    (let [dot-position (find-substring text "." expected-length)]
      (if (<= (Math/abs (- dot-position expected-length)) max-distance)
        (str (.substring text 0 dot-position) ".")
        (.substring text 0 (find-substring text " " expected-length))))))

(defn smart-subvec
"Just like clojure.core/subvec, but when vec parameter is not a vector, attempt to convert it.
If end is greater than vec size, extract less elements.

Example:
(smart-subvec [:a :b :c] 0 2)
;=> [:a :b]
(smart-subvec [:a :b :c] 0 1000)
;=> [:a :b :c]
(smart-subvec {:a :b :c :d} 0 1)
;=> [[:a :b]]"
  [vec start end]
  (when vec
    (let [vec (if (vector? vec) vec (into [] vec))]
      (if (>= (or end (count vec)) (count vec))
        (subvec vec (or start 0) (count vec))
        (subvec vec (or start 0) end)))))


(defn clear-url
"Naive (regex-based) function removing everything starting with # and ? from a string.

Example:
(clear-url \"http://localhost:8097/a/i18n-text-edit?key=test&widget-id=9#123\")
;=> \"http://localhost:8097/a/i18n-text-edit\""
  [url]
  (when url
    (.replaceAll (.replaceAll url "\\?.*" "") "\\#.*" "")))

(def ^:dynamic *for-count*)
(def ^:dynamic *for-arr*)
(def ^:dynamic *for-size*)

(defmacro for!
"Enhanced for macro, which introduces count, first? and last? flags.

Example:
(for! x [:a :b :c] [x (for!-count) (for!-first?) (for!-last?)])
;=> ([:a 0 true false] [:b 1 false false] [:c 2 false true])
"
  [var arr & body]
  `(let [arr# ~arr]
     (binding [*for-count* (atom -1)
               *for-arr* arr#
               *for-size* (count arr#)]
       (doall (for [~var *for-arr*]
                (do
                  (swap! *for-count* inc)
                  ~@body
                  ))))))

(defn for!-count
"See for! macro for details."
  []
  @*for-count*)

(defn for!-first?
"See for! macro for details."
  []
  (= @*for-count* 0))

(defn for!-last?
"See for! macro for details."
  []
  (>= @*for-count* (- *for-size* 1)))

(defn with-timeout
"Execute callback with timeout specified in seconds.

Example:
(with-timeout 1 #(Thread/sleep 1200))
;=> TimeoutException   java.util.concurrent.FutureTask$Sync.innerGet (FutureTask.java:258)
(with-timeout 1 #(Thread/sleep 900))
;=> nil"
  [timeout callback]
  (.get (future (callback)) timeout java.util.concurrent.TimeUnit/SECONDS))

(defn schedule-with-fixed-delay!
"Schedule single-threaded execution of callback with a delay specified in seconds.
If exception occurs during callback execution, error-callback is called.

Example:
(schedule-with-fixed-delay! 5 #(println (java.util.Date.)) #(println %))
;=> #<ScheduledFutureTask java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@60bf7a4d>
;=> #inst \"2013-02-15T20:15:24.278-00:00\"
;=> #inst \"2013-02-15T20:15:29.293-00:00\"
;=> #inst \"2013-02-15T20:15:34.294-00:00\""
  [delay callback error-callback]
  (.scheduleWithFixedDelay (java.util.concurrent.Executors/newSingleThreadScheduledExecutor)
    (proxy [Runnable] []
      (run []
        (try
          (callback)
          (catch Exception e
            (try
              (error-callback e)
              (catch Exception e1
                (.printStackTrace e1)))))))
    0 delay java.util.concurrent.TimeUnit/SECONDS))

(defn indexes-of
"Return indexes of vec elements for which (f %) returns trueish value.

Example:
(indexes-of keyword? [1 2 'x :k 'az 3])
;=> (3 4)"
  [f vec]
  (keep-indexed #(when (f %2) %1) vec))

(defn gzip?
"Returns true when the byte array contains gzip magic value in first two bytes."
  [b-array]
  (when (> (count b-array) 2)
    (= (int (bit-or (bit-and (get b-array 0) 0xff) (bit-and (bit-shift-left (get b-array 1) 8) 0xff00)))
    java.util.zip.GZIPInputStream/GZIP_MAGIC)))


(defn map-contains?
"Returns true if supplied map contains all of the supplied keys and values.

Example:
(map-contains? {:a :b :c :d :e :f} {:a :b :e :f})
;=> true
(map-contains? {:a :b :c :d :e :f} {:a :b :e :d})
;=> false"
  [map kvs]
  (every? (fn [[k v]] (= v (get map k))) kvs))

(defn keyword-params-str
"Replace keywords in string with supplied values.

Example:
(keyword-params-str \":1/:2/:3\" {:1 \"I\" :2 :2 :3 \"three\"})
\"I/:2/three\""
  [action params]
  (reduce (fn [s [k v]] (clojure.string/replace s (str k) (str v))) action params))

(defn mreplace
"Perform multi-replace on a string. If a specific order of replacement is needed, [[k1 v1] [k2 v2]] structure can be used.

Example:
(mreplace \":1/:2/:3\" [[#\"[0-9]\" \"I\"] [\":\" \"22\"] [\":3\" \"three\"]])
\"22I/22I/22I\""
  [str rmap]
  (reduce (fn [s [k v]] (clojure.string/replace s k v)) str rmap))

(defn substring
"Smarter substring, which detects if string is shorter than end and returns only appropriate portion of it.

Example:
(substring \"abc\" 1 5)
\"bc\""
  [str start end]
  (when str
    (.substring str start (if (> (.length str) end) end (.length str)))))

(defn to-map
"Transform collection to a map using key-fn to generate keys.

Example
(to-map [{:a :b :c :d} {:a :x :c :y}] :a)
;=> {:b {:a :b, :c :d}, :x {:a :x, :c :y}}"
  [elements key-fn]
  (into {} (map (fn [x] [(key-fn x) x]) elements)))