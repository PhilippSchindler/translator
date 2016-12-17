(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseController', ReleaseController);

    ReleaseController.$inject = ['$scope', '$state', 'Release'];

    function ReleaseController ($scope, $state, Release) {
        var vm = this;

        vm.releases = [];

        loadAll();

        function loadAll() {
            Release.query(function(result) {
                vm.releases = result;
                vm.searchQuery = null;
            });
        }
    }
})();
