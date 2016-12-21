(function () {
    'use strict';

    angular
        .module('translatorApp')
        .factory('User', User);

    User.$inject = ['$resource'];

    function User ($resource) {
        var service = $resource('api/users/:login', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': { method:'POST'},
            'createProjectMember': {
                method:'POST',
                params: {login: 'createProjectMember'}
            },
            'update': { method:'PUT' },
            'delete':{ method:'DELETE'}
        });

        return service;
    }
})();
