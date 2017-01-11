(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Release', Release);

    Release.$inject = ['$resource', 'DateUtils'];

    function Release ($resource, DateUtils) {
        var resourceUrl =  'api/releases/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.deadline = DateUtils.convertLocalDateFromServer(data.deadline);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.deadline = DateUtils.convertLocalDateToServer(copy.deadline);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.deadline = DateUtils.convertLocalDateToServer(copy.deadline);
                    return angular.toJson(copy);
                }
            },
            'getByProject': {
                method: 'GET',
                url:  'api/releases/project/:projectId',
                isArray: true
            },
            'getSelectedVersions': {
                method: 'GET',
                url: 'api/releases/:releaseId/selectedVersions'
            },
            'saveSelectedVersions': {
                method: 'POST',
                url: 'api/releases/:releaseId/selectedVersions'
            }
        });
    }
})();
