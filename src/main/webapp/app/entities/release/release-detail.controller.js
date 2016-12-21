(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseDetailController', ReleaseDetailController);

    ReleaseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Release', 'Definition', 'Project', 'project'];

    function ReleaseDetailController($scope, $rootScope, $stateParams, previousState, entity, Release, Project, project) {
        var vm = this;

        vm.release = entity;
        vm.previousState = previousState.name;
        vm.project = project;

        var unsubscribe = $rootScope.$on('translatorApp:releaseUpdate', function(event, result) {
            vm.release = result;
        });
        $scope.$on('$destroy', unsubscribe);


    }
})();
