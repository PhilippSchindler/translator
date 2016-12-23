(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslationDialogController', TranslationDialogController);

    TranslationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Translation', 'Language', 'Definition'];

    function TranslationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Translation, Language, Definition) {
        var vm = this;

        vm.translation = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.languages = Language.query();
        vm.definitions = Definition.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.translation.id !== null) {
                Translation.update(vm.translation, onSaveSuccess, onSaveError);
            } else {
                Translation.save(vm.translation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('translatorApp:translationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.updatedAt = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
