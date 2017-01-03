(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DiffVersionsController', DiffVersionsController);

    DiffVersionsController.$inject = ['$scope', '$rootScope', '$stateParams', 'project', 'DiffVersions'];

    function DiffVersionsController($scope, $rootScope, $stateParams, project, DiffVersions) {
        var vm = this;
        vm.project = project;
        vm.version1 = 0;
        vm.version2 = 0;

        vm.versions = DiffVersions.listOfAllVersions({'projectId': vm.project.id});

        vm.displayText = function (version) {
            return "Version " + version;
        }



    }
})();
