define(["dojo/_base/declare",
    "dojo/rpc/JsonService"
], function (declare, JsonService) {

    return declare(null, {
        constructor: function () {
            this._initUserSvc();
        },
        _initUserSvc: function () {
            this.svc = new JsonService({
                serviceUrl: 'rpc/users', // Adress of the RPC service end point
                timeout: 10000,
                strictArgChecks: true,
                methods: [{
                        name: 'setHomeUrl',
                        parameters: [
                            {
                                name: 'homeUrl',
                                type: 'STRING'
                            }
                        ]
                    }
                ]
            });
        },
        setHomeUrl: function (homeUrl) {
            return this.svc.setHomeUrl(homeUrl);
        }

    });
})