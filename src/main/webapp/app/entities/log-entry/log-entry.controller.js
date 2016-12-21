(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LogEntryController', LogEntryController);

    LogEntryController.$inject = ['$scope', '$state', 'LogEntry'];

    function LogEntryController ($scope, $state, LogEntry) {
        var vm = this;

        vm.logEntries = [];

        loadAll();

        function loadAll() {
            LogEntry.query(function(result) {
                vm.logEntries = result;
                vm.searchQuery = null;
            });
        }
    }
})();
