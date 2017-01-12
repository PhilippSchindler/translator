(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Project', Project);

    Project.$inject = ['$resource'];

    function Project ($resource) {
        var resourceUrl =  'api/projects/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'getByUser': {method: 'GET', url: 'api/users/:userLogin/singleproject'},
            'getLogByProjectId': {method: 'GET', url: 'api/project/:projectId/log', isArray: true}
        });
    }
})();
