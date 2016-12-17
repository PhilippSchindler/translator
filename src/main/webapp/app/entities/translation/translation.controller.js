(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslationController', TranslationController);

    TranslationController.$inject = ['$scope', '$state', 'Translation'];

    function TranslationController ($scope, $state, Translation) {
        var vm = this;

        vm.translations = [];

        loadAll();

        function loadAll() {
            Translation.query(function(result) {
                vm.translations = result;
                vm.searchQuery = null;
            });
        }
    }
})();
