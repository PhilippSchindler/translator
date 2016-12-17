(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseDialogController', ReleaseDialogController);

    ReleaseDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Release', 'Definition', 'Project'];

    function ReleaseDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Release, Definition, Project) {
        var vm = this;

        vm.release = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.definitions = Definition.query();
        vm.projects = Project.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.release.id !== null) {
                Release.update(vm.release, onSaveSuccess, onSaveError);
            } else {
                Release.save(vm.release, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('translatorApp:releaseUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.deadline = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
