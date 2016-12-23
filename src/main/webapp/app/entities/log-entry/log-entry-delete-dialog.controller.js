(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LogEntryDeleteController',LogEntryDeleteController);

    LogEntryDeleteController.$inject = ['$uibModalInstance', 'entity', 'LogEntry'];

    function LogEntryDeleteController($uibModalInstance, entity, LogEntry) {
        var vm = this;

        vm.logEntry = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LogEntry.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
