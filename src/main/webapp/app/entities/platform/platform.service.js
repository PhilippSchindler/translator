(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Platform', Platform);

    Platform.$inject = ['$resource'];

    function Platform ($resource) {
        var resourceUrl =  'api/platforms/:id';

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
            'update': { method:'PUT' }
        });
    }
})();
