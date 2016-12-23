(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('PlatformController', PlatformController);

    PlatformController.$inject = ['$scope', '$state', 'Platform'];

    function PlatformController ($scope, $state, Platform) {
        var vm = this;

        vm.platforms = [];

        loadAll();

        function loadAll() {
            Platform.query(function(result) {
                vm.platforms = result;
                vm.searchQuery = null;
            });
        }
    }
})();
