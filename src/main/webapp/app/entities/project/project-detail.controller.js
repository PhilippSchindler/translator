(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectDetailController', ProjectDetailController);

    ProjectDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Project', 'Definition', 'Release', 'User', 'Platform', 'Language'];

    function ProjectDetailController($scope, $rootScope, $stateParams, previousState, entity, Project, Definition, Release, User, Platform, Language) {
        var vm = this;

        vm.project = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('translatorApp:projectUpdate', function(event, result) {
            vm.project = result;
        });
        $scope.$on('$destroy', unsubscribe);

        vm.replaceNewLineWithBR = function (str) {
            return str.replace(/(?:\r\n|\r|\n)/g, '<br />');
        }
    }
})();
