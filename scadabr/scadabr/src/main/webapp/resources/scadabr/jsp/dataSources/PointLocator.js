define(["dojo/_base/declare",
    "dojo/dom",
    "dojo/dom-form",
    "dojo/request",
    "dijit/registry",
    "dojo/on",
    "dojo/ready"
], function (declare, dom, domForm, request, registry, on, ready) {

    return declare(null, {
        form: null,
        constructor: function (formId, id) {
            var form = dom.byId(formId);
            // Attach the onsubmit event handler of the form

            on(form, "submit", function (evt) {
                // prevent the page from navigating after submit
                evt.stopPropagation();
                evt.preventDefault();
                // Post the data to the server
                request.post("dataSources/pointLocator", {
                    query: {
                        id: id
                    },
                    // Send the username and password
                    data: domForm.toObject(formId)
                            // Wait 10 seconds for a response
                            //timeout: 10000

                }).then(function (response) {
                    alert(response);
                }, function (error) {
                    alert(error);
                });
            });
        }
    });
});