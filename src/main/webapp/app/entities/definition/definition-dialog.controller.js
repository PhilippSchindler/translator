(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DefinitionDialogController', DefinitionDialogController);

    DefinitionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Definition', 'Translation', 'Project'];

    function DefinitionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Definition, Translation, Project) {
        var vm = this;

        vm.definition = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.translations = Translation.query();
        vm.projects = Project.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.definition.id !== null) {
                Definition.update(vm.definition, onSaveSuccess, onSaveError);
            } else {
                Definition.save(vm.definition, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('translatorApp:definitionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.createdAt = false;
        vm.datePickerOpenStatus.updatedAt = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
