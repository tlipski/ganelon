// Copyright (c) Tomek Lipski. All rights reserved.  The use
// and distribution terms for this software are covered by the Eclipse
// Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
// which can be found in the file LICENSE.txt at the root of this
// distribution.  By using this software in any fashion, you are
// agreeing to be bound by the terms of this license.  You must not
// remove this notice, or any other, from this software.

var Ganelon = Ganelon || {};

Ganelon.performAction = function(action, data, onSuccess, onError) {
    jqxhr = $.ajax({
              url: '/a/' +action,
              data: data,
              dataType: 'json',
              type: 'POST',
              success: function(data) { if (onSuccess) { onSuccess(data);} $.each(data, Ganelon.dispatch); },
              error: function(jqXHR, textStatus, errorThrown) {
                        onError(jqXHR, textStatus, errorThrown);
                        Ganelon.onError(jqXHR, textStatus, errorThrown);
                     }
            });
}

Ganelon.performButtonAction = function(button, action, data) {
   Ganelon.performAction(action,
                         data,
                         function(data) { Ganelon.resetButton(button);},
                         function(data) { Ganelon.resetButton(button);});
   Ganelon.disableButton(button);
   return false;
}

Ganelon.performFormAction = function(form, action, query) {
   Ganelon.performAction(action + "?" + query,
                         $(form).serialize(),
                         function(data) { $.each($(form).find('button'), function(idx,x) { Ganelon.resetButton(x); });},
                         function(data) { $.each($(form).find('button'), function(idx,x) { Ganelon.resetButton(x); });});
   $.each($(form).find('button'), function(idx,x) { Ganelon.disableButton(x); });
   return false;
}


Ganelon.disableButton = function(button) {
    $(button).attr('disabled', 'disabled');
}

Ganelon.resetButton = function(button) {
    $(button).removeAttr('disabled');
}

Ganelon.onError = function(jqXHR, textStatus, errorThrown) {
    Ganelon.dispatch(null, {type: 'error', title: 'Error', message:
    '<p>The server has returned an unexpected response: <b>' + textStatus + '</b>, ' + errorThrown + '. \n'  +
    'Please try performing this action again later and if the problem persists, <a href="/contact">contact</a> us.</p>'});
}


Ganelon.dispatch = function(idx, o) {
    if (Ganelon.operations[o.type]) {
        Ganelon.operations[o.type](o);
    } else {
        alert('No function "' + o.type + '" defined!');
    }
}

Ganelon.operations = { }

Ganelon.registerOperation = function(name, fn) {
    Ganelon.operations[name] = fn;
}

Ganelon.registerOperation('notification', function(o) { alert(o.title + ':\n' + o.text); });
Ganelon.registerOperation('refresh-page', function(o) { location.reload(); });
Ganelon.registerOperation('open-page', function(o) { location.assign(o.url); });
Ganelon.registerOperation('open-window', function(o) { window.open(o.url, o.name, o.options); });
//useful functions from http://api.jquery.com/category/manipulation/
Ganelon.registerOperation('dom-add-class', function(o) {$(o.id).addClass(o.value);});
Ganelon.registerOperation('dom-after', function(o) {$(o.id).after(o.value);});
Ganelon.registerOperation('dom-append', function(o) {$(o.id).append(o.value);});
Ganelon.registerOperation('dom-set-attr', function(o) {if (o.name) {$(o.id).attr(o.name, o.value);} else {$(o.id).attr(o.properties);}});
Ganelon.registerOperation('dom-before', function(o) {$(o.id).before(o.value);});
Ganelon.registerOperation('dom-set-css', function(o) {if (o.name) {$(o.id).css(o.name, o.value);} else {$(o.id).css(o.properties);}});
Ganelon.registerOperation('dom-detach', function(o) {$(o.id).detach();});
Ganelon.registerOperation('dom-make-empty', function(o) {$(o.id).empty();});
Ganelon.registerOperation('dom-set-height', function(o) {$(o.id).height(o.height);});
Ganelon.registerOperation('dom-html', function(o) {$(o.id).html(o.value);});
Ganelon.registerOperation('dom-set-offset', function(o) {$(o.id).offset(o.coordinates);});
Ganelon.registerOperation('dom-prepend', function(o) {$(o.id).prepend(o.value);});
Ganelon.registerOperation('dom-set-prop', function(o) {if (o.name) {$(o.id).prop(o.name, o.value);} else {$(o.id).prop(o.properties);}});
Ganelon.registerOperation('dom-remove-element', function(o) {$(o.id).remove();});
Ganelon.registerOperation('dom-remove-attr', function(o) {$(o.id).removeAttr(o.name);});
Ganelon.registerOperation('dom-remove-class', function(o) {$(o.id).removeClass(o.name);});
Ganelon.registerOperation('dom-remove-prop', function(o) {$(o.id).removeProp(o.name);});
Ganelon.registerOperation('dom-replace-with', function(o) {$(o.id).replaceWith(o.value);});
Ganelon.registerOperation('dom-set-scroll-left', function(o) {$(o.id).scrollLeft(o.value);});
Ganelon.registerOperation('dom-set-scroll-top', function(o) {$(o.id).scrollTop(o.value);});
Ganelon.registerOperation('dom-text', function(o) {$(o.id).text(o.value);});
Ganelon.registerOperation('dom-toggle-class', function(o) {$(o.id).toggleClass(o.value);});
Ganelon.registerOperation('dom-set-width', function(o) {$(o.id).width(o.width);});


Ganelon.registerOperation('error',
    function (o) { Ganelon.dispatch(null, {type: 'notification', title: 'Error', text: o.message, sticky: true})});


Ganelon.registerOperation('dom-fade',
    function(o) {
        $(o.id).fadeOut('fast',
                         function() {
                            $(o.id).html(o.value);
                            $(o.id).fadeIn('slow');
                         });});



