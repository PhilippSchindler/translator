(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Language', Language);

    Language.$inject = ['$resource'];

    function Language ($resource) {
        var resourceUrl =  'api/languages/:id';

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
            'updateProjectAssignment': { method:'PUT', url:'api/languages/updateProjectAssignment/:projectId'}
        });
    }
})();
