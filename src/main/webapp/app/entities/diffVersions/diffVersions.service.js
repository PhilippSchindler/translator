(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('DiffVersions', DiffVersions);

    DiffVersions.$inject = ['$resource', 'DateUtils'];

    function DiffVersions ($resource, DateUtils) {
        var resourceUrl =  'api/diffVersions/versions/:projectId';

        return $resource(resourceUrl, {}, {
            'listOfAllVersions': { method: 'GET', isArray: true},
            'getDefinitions': {
                url: 'api/diffVersions/definitions/:projectId/:version',
                method: 'GET',
                isArray: true
            }
        });
    }
})();
