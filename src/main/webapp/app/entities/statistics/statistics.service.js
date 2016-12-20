(function() {
    'use strict';
    angular
        .module('translatorApp')
        .factory('Statistics', Statistics);

    Statistics.$inject = ['$resource'];

    function Statistics ($resource) {
        var resourceUrl =  'api/projects/:projectId/statistics/';

        return $resource(resourceUrl, {}, {
            'getNotTranslatedTexts': { url: resourceUrl + 'notTranslatedTexts', method: 'GET', isArray: true }
        });
    }
})();
