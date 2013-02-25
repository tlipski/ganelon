// Copyright (c) Tomek Lipski. All rights reserved.  The use
// and distribution terms for this software are covered by the Eclipse
// Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
// which can be found in the file LICENSE.txt at the root of this
// distribution.  By using this software in any fashion, you are
// agreeing to be bound by the terms of this license.  You must not
// remove this notice, or any other, from this software.
/*
 Dispatch functions for Twitter Bootstrap JavaScript plugins

*/
Ganelon.registerOperation('modal', function (o) {
    var id = o.id;
    var content = o.value;
    //just to be sure, since data-dismiss="modal" only hides the element, not removes it
    $("#modal-" + id).modal('hide');
    $("#modal-" + id).remove();
    //create div
    var newDiv = document.createElement('div');
    document.body.appendChild(newDiv);
    $(newDiv).addClass('modal');
    $(newDiv).attr({id: "modal-" + id});
    if (o.style) {
        $(newDiv).attr({style: o.style});
    }

    $(newDiv).modal(o.options);
    $(newDiv).prepend(content);
});

Ganelon.registerOperation('remove-modal',
    function(o) {
        $("#modal-" + o.id).modal('hide');
        $("#modal-" + o.id).remove();
    });

Ganelon.registerOperation('tab-show', function(o) { $(o.id).tab('show');});

Ganelon.disableButton = function(button) {
    $(button).button('loading');
}

Ganelon.resetButton = function(button) {
    $(button).button('reset');
}