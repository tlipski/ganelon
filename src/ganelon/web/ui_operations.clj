;; Copyright (c) Tomek Lipski. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE.txt at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ganelon.web.ui-operations
"This namespace provides wrapping functions for defining certain out of the box UI operations.

An operation is a command send from an AJAX action to thin JavaScript layer.
")

;we could use macros, but i.e. La Clojure plugin does not work well with that :(
(defn- ui-operation [type & params]
  (assoc (apply hash-map params) :type type))

(defn- ui-operation-with-id-value [type id value]
  {:type type :id id :value value})

(defn remove-modal
"Remove modal window, created by (modal id body & params) function.

Client side operation type is 'remove-modal'.

Example:

(remove-modal \"demo-action-source-modal\")"
  [id]
  (ui-operation "remove-modal" :id id))

(defn modal
"Display modal window. The default JavaScript operation uses Twitter Bootstrap's modal plugin.
Client side operation type is 'modal'.

With & params it is possible to pass additional options to the plugin itself.

Example:

(modal \"demo-action-source-modal\"
   (hiccup.core/html
     [:div {:class \"modal-header\"}
      [:a {:class \"close\" :data-dismiss \"modal\"} \"close\"]
      [:h3 \"Example action and widget\"]])
   :style \"width: 640px;\")"
  [id body & params]
  (merge (apply hash-map params) (ui-operation "modal" :id id :value body)))

(defn notification
"Display Growl-style notification. Client side operation type is 'notification'.

With & params, it is possible to provide additional options to the plugin itself.

Example:

(ui/notification \"Success\"  (h/html \"Message set to: \" [:b (hiccup.util/escape-html msg)]))"
  [title body & params]
  (merge (apply hash-map params) (ui-operation "notification" :title title :text body)))

(defn refresh-page
"Refresh browser's page through JavaScript.

Client side operation type is 'refresh-page'.

Example:

(refresh-page)"
  []
  (ui-operation "refresh-page"))

(defn open-page
"Open page URL through JavaScript.

Client side operation type is 'open-page'.

Example:

(open-page \"/another-page\")"
  [url]
  (ui-operation "open-page" :url url))

(defn fade
"Replace a part(s) of DOM referenced by id (which is a jQuery selector string).

Client side operation type is 'dom-fade', which uses jQuery's .fadeOut(), .fadeIn() and .html() methods.

Example:

(fade \"#widget1\" \"<p>Test!</p>\")"
  [id body]
  (ui-operation-with-id-value "dom-fade" id body))

(defn add-class
"
Add css class to elements matched by id.

Client side operation type is 'dom-add-class', which maps directly to jQuery .addClass() method.

Example:

(add-class \".warning\" \"large-warning\")"
  [id class-name]
  (ui-operation-with-id-value "dom-add-class" id class-name))

(defn after
"Insert DOM content after elements matched by id.

Client side operation type is 'dom-after', which maps directly to jQuery .after() method.

Example:

(after \"#widget1\" \"<p>Test!</p>\")"
  [id value]
  (ui-operation-with-id-value "dom-after" id value))

(defn append
"Insert DOM content at the end of elements matched by id.

Client side operation type is 'dom-append', which maps directly to jQuery .append() method.

Example:

(append \"#widget1\" \"<p>Test!</p>\")"
  [id value]
  (ui-operation-with-id-value "dom-append" id value))

(defn set-attr
"Set attribute value or values for elements matched by id.

Client side operation type is 'dom-set-attr', which maps directly to jQuery .attr() method (in write mode)."
  ([id name value] (ui-operation "dom-set-attr" :id id :name name :value value))
  ([id properties] (ui-operation "dom-set-attr" :id id :properties properties)))

(defn before
"Insert content before DOM elements matched by id.

Client side operation type is 'dom-before', which maps directly to jQuery .before() method."
    [id value]
    (ui-operation-with-id-value "dom-before" id value))

(defn set-css
"Set css attribute or attributes for elements matched by id.

Client side operation type is 'dom-set-css', which maps directly to jQuery .css() method (in write mode)."
  ([id name value] (ui-operation "dom-set-css" :id id :name name :value value))
  ([id properties] (ui-operation "dom-set-css" :id id :properties properties)))

(defn detach
"Detach elements matched by id from DOM tree. Please note, that it might me more readable to
add your own operations instead of composing them out of existing ones.

Client side operation type is 'dom-detach', which maps directly to jQuery .detach() method."
  [id]
  (ui-operation "dom-detach" :id id))

(defn make-empty
"Remove all children of elements matched by id in DOM tree.

Client side operation type is 'dom-make-empty', which maps directly to jQuery .empty() method."
  [id]
  (ui-operation "dom-make-empty" :id id))

(defn set-height
"Set height of elements matched by id in DOM tree.

Client side operation type is 'dom-set-height', which maps directly to jQuery .height() method (in write mode)."
  [id height]
  (ui-operation "dom-set-height" :id id :height height))

(defn html
"Set HTML contents of elements matched by id in DOM tree.

Client side operation type is 'dom-html', which maps directly to jQuery .html() method in write mode."
  [id value]
  (ui-operation-with-id-value "dom-html" id value))

(defn set-offset
"Set offset contents of elements matched by id in DOM tree.

Client side operation type is 'dom-set-offset', which maps directly to jQuery .offset() method in write mode."
  [id coordinates]
  (ui-operation "dom-set-offset" :id id :coordinates coordinates))


(defn prepend
"Insert content at the beginning of DOM elements matched by id.

Client side operation type is 'dom-prepend', which maps directly to jQuery .prepend() method."
  [id value]
  (ui-operation-with-id-value "dom-prepend" id value))

(defn set-prop
"Set properties of DOM tree elements match by id.

Client side operation type is 'dom-set-prop', which maps directly to jQuery .prop() method in write mode."
  ([id name value] (ui-operation "dom-set-prop" :id id :name name :value value))
  ([id properties] (ui-operation "dom-set-prop" :id id :properties properties)))


(defn remove-element
"Remove elements matched by id from DOM tree.

Client side operation type is 'dom-remove-element', which maps directly to jQuery .remove() method."
  [id]
  (ui-operation "dom-remove-element" :id id))

(defn remove-attr
"Remote attribute with a specified name from elements matched by id.

Client side operation type is 'dom-remove-attr', which maps directly to jQuery .removeAttr() method."
  [id name]
  (ui-operation "dom-remove-attr" :id id :name name))

(defn remove-class
"Remote CSS class with a specified name from elements matched by id.

Client side operation type is 'dom-remove-class', which maps directly to jQuery .removeClass() method."
  [id name]
  (ui-operation "dom-remove-class" :id id :name name))


(defn remove-prop
"Remote property with a specified name from elements matched by id.

Client side operation type is 'dom-remove-prop', which maps directly to jQuery .removeProp() method."
  [id value]
  (ui-operation "dom-remove-prop" :id id :name name))

(defn replace-with
"Remote property with a specified name from elements matched by id.

Client side operation type is 'dom-remove-prop', which maps directly to jQuery .removeProp() method."
  [id body]
  (ui-operation-with-id-value "dom-replace-with" id body))

(defn set-scroll-left
"Set horizontal position of a scrollbar for elements matched by id.

Client side operation type is 'dom-set-scroll-left', which maps directly to jQuery .scrollLeft() method."
  [id value]
  (ui-operation-with-id-value "dom-set-scroll-left" id value))

(defn set-scroll-top
"Set vertical position of a scrollbar for elements matched by id.

Client side operation type is 'dom-set-scroll-top', which maps directly to jQuery .scrollTop() method."
  [id value]
  (ui-operation-with-id-value "dom-set-scroll-top" id value))

(defn text
"Set text contents of elements matched by id in DOM tree.

Client side operation type is 'dom-html', which maps directly to jQuery .text() method in write mode."
  [id value]
  (ui-operation-with-id-value "dom-text" id value))

(defn toggle-class
"Toggle CSS class with a specified name for elements matched by id.

Client side operation type is 'dom-toggle-class', which maps directly to jQuery .toggleClass() method."
  [id value]
  (ui-operation-with-id-value "dom-toggle-class" id value))

(defn set-width
"Set width of elements matched by id in DOM tree.

Client side operation type is 'dom-set-width', which maps directly to jQuery .width() method (in write mode)."
  [id width]
  (ui-operation-with-id-value "dom-set-width" id width))
