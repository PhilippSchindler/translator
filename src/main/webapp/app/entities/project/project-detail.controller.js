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

        User.query({
            page: 0,
            size: 100,
            sort: 'id,asc'
        }, onSuccess, onError);

        function onSuccess(data, headers) {
            //hide anonymous user from user management: it's a required user for Spring Security
            var hiddenUsersSize = 0;
            for (var i in data) {
                if (data[i]['login'] === 'anonymoususer') {
                    data.splice(i, 1);
                    hiddenUsersSize++;
                    continue;
                }

            }
            //vm.links = ParseLinks.parse(headers('link'));
            //vm.totalItems = headers('X-Total-Count') - hiddenUsersSize;
            //vm.queryCount = vm.totalItems;
            //vm.page = pagingParams.page;
            vm.users = data;
        }

        function onError(error) {
            AlertService.error(error.data.message);
        }
    }
})();
