(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('LogEntry', LogEntry);

    LogEntry.$inject = ['$resource', 'DateUtils'];

    function LogEntry ($resource, DateUtils) {
        var resourceUrl =  'api/log-entries/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.timestamp = DateUtils.convertDateTimeFromServer(data.timestamp);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
