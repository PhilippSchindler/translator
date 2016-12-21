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

        vm.users = [];
        vm.currentAccount = null;

        var unsubscribe = $rootScope.$on('translatorApp:projectUpdate', function(event, result) {
            vm.project = result;
        });
        $scope.$on('$destroy', unsubscribe);

        vm.replaceNewLineWithBR = function (str) {
            return str.replace(/(?:\r\n|\r|\n)/g, '<br />');
        }

        User.query({
            page: 0,
            size: 1000
        }, onSuccess, onError);

        function onSuccess(data, headers) {
            vm.users = data;
        }

        function onError(error) {
            AlertService.error(error.data.message);
        }
    }
})();
