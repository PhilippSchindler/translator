(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Definition', Definition);

    Definition.$inject = ['$resource', 'DateUtils'];

    function Definition ($resource, DateUtils) {
        var resourceUrl =  'api/definitions/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.createdAt = DateUtils.convertDateTimeFromServer(data.createdAt);
                        data.updatedAt = DateUtils.convertDateTimeFromServer(data.updatedAt);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'queryLatestByProject': { method:'GET', url:'api/projects/:projectId/definitions/latest', isArray:true},
            'getForProject': {
                url: 'api/project/:projectId/definitions',
                method: 'GET',
                isArray: true,
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.createdAt = DateUtils.convertDateTimeFromServer(data.createdAt);
                        data.updatedAt = DateUtils.convertDateTimeFromServer(data.updatedAt);
                    }
                    return data;
                }
            },
            'getGroupedForProject': {
                url: 'api/project/:projectId/groupedDefinitions',
                method: 'GET'
            }
        });
    }
})();
