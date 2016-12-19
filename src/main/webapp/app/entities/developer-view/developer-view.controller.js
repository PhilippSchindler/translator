(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DeveloperViewController', DeveloperViewController);

    DeveloperViewController.$inject = ['$scope', '$rootScope', '$stateParams', 'project', 'definitions', 'Project', 'Definition', 'Release', 'User', 'Platform', 'Language'];

    function DeveloperViewController($scope, $rootScope, $stateParams, project, definitions, Project, Definition, Release, User, Platform, Language) {
        var vm = this;
        vm.project = project;
        vm.newDefinition = {
            project: vm.project
        };
        vm.definitions = definitions;

        vm.createDefinition = function () {
            Definition.save(vm.newDefinition, onSaveSuccess, onSaveError);
        };

        function onSaveSuccess(result) {
            vm.definitions.push(result);
            vm.newDefinition = {
                project: vm.project
            };
            vm.isSaving = false;
        }

        function onSaveError(error) {
            console.log(error);
            vm.isSaving = false;
        }
    }
})();
