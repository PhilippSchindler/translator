(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseController', ReleaseController);

    ReleaseController.$inject = ['$scope', '$state', 'Release', 'project', 'Principal'];

    function ReleaseController ($scope, $state, Release, project, Principal) {
        var vm = this;

        vm.releases = [];
        vm.project = project;
        vm.showButton = false;

        loadAll();
        showNewReleaseButton();

        function loadAll() {
            Release.getByProject({projectId: vm.project.id}, function(result) {
                vm.releases = result;
                vm.searchQuery = null;
            });
        }

        function showNewReleaseButton() {
            Principal.identity().then(function(account) {
                let userAuthority = account.authorities[0];
                if (userAuthority === 'ROLE_CUSTOMER') {
                    vm.showButton = true;
                 }
            });
        }
    }
})();
