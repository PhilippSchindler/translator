(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LogEntryDialogController', LogEntryDialogController);

    LogEntryDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'LogEntry', 'User'];

    function LogEntryDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, LogEntry, User) {
        var vm = this;

        vm.logEntry = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.logEntry.id !== null) {
                LogEntry.update(vm.logEntry, onSaveSuccess, onSaveError);
            } else {
                LogEntry.save(vm.logEntry, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('translatorApp:logEntryUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.timestamp = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
