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
            'update': { method:'PUT' },
            'updateChangedTranslations': { method: 'PUT', url: 'api/translations/updateChangedTranslations'},
            'import': { method: 'PUT', url: 'api/translations/import/:format/:languageId'},
            'export': {
                method: 'GET',
                url: 'api/translations/export/:format/:languageId/:releaseId',
                transformResponse: function (data) {
                    return {content: data};
                }
            }
        });
    }
})();
