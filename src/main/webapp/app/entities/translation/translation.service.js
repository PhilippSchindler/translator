(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Translation', Translation);

    Translation.$inject = ['$resource', 'DateUtils'];

    function Translation ($resource, DateUtils) {
        var resourceUrl =  'api/translations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.updatedAt = DateUtils.convertDateTimeFromServer(data.updatedAt);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
