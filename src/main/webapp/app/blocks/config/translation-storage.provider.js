(function() {
    'use strict';

    angular
        .module('translatorApp')
        .factory('translationStorageProvider', translationStorageProvider);

    translationStorageProvider.$inject = ['$cookies', '$log', 'LANGUAGES'];

    function translationStorageProvider($cookies, $log, LANGUAGES) {
        return {
            get: get,
            put: put
        };

        function get(name) {
            if (LANGUAGES.indexOf($cookies.getObject(name)) === -1) {
                $log.info('Resetting invalid cookie language "' + $cookies.getObject(name) + '" to preferred language "de"');
                $cookies.putObject(name, 'de');
            }
            return $cookies.getObject(name);
        }

        function put(name, value) {
            $cookies.putObject(name, value);
        }
    }
})();
