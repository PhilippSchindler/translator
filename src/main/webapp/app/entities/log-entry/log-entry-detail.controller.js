(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LogEntryDetailController', LogEntryDetailController);

    LogEntryDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LogEntry', 'User'];

    function LogEntryDetailController($scope, $rootScope, $stateParams, previousState, entity, LogEntry, User) {
        var vm = this;

        vm.logEntry = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('translatorApp:logEntryUpdate', function(event, result) {
            vm.logEntry = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
