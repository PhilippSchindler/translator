(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DefinitionController', DefinitionController);

    DefinitionController.$inject = ['$scope', '$state', 'Definition'];

    function DefinitionController ($scope, $state, Definition) {
        var vm = this;

        vm.definitions = [];

        loadAll();

        function loadAll() {
            Definition.query(function(result) {
                vm.definitions = result;
                vm.searchQuery = null;
            });
        }
    }
})();
