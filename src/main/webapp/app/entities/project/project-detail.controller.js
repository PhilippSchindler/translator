(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectDetailController', ProjectDetailController);

    ProjectDetailController.$inject = ['$scope', '$rootScope', '$stateParams', '$filter', 'Principal', 'previousState', 'entity', 'Project', 'Definition', 'Release', 'User', 'Platform', 'Language'];

    function ProjectDetailController($scope, $rootScope, $stateParams, $filter, Principal, previousState, entity, Project, Definition, Release, User, Platform, Language) {
        var vm = this;

        vm.project = entity;
        vm.previousState = previousState.name;

        vm.users = [];
        vm.currentAccount = null;
        vm.languagesOfCustomer = null;

        Language.query(function(result){
            vm.languagesOfCustomer = result;

            //Filter by already assigned to project
            let languagesNotAssignedToProject = [];
            for(var i=0; i < vm.languagesOfCustomer.length; i++){
                let langToCheck = vm.languagesOfCustomer[i];
                let alreadyAssignedToProject = false;
                for(var j=0; j < vm.project.languages.length; j++){
                    if(langToCheck.id === vm.project.languages[j].id){
                        alreadyAssignedToProject = true;
                        break;
                    }
                }
                if(!alreadyAssignedToProject){
                    languagesNotAssignedToProject.push(langToCheck);
                }
            }

            $scope.languageSelectOptions = {
                title: $filter('translate')('translatorApp.project.detail.languages'),
                filterPlaceHolder: $filter('translate')('multiselect.filterPlaceHolder'),
                labelAll: $filter('translate')('multiselect.labelAll'),
                labelSelected: $filter('translate')('multiselect.labelSelected'),
                selectAll: $filter('translate')('multiselect.selectAll'),
                deselectAll: $filter('translate')('multiselect.deselectAll'),
                helpMessage: $filter('translate')('multiselect.helpMessage'),
                /* angular will use this to filter your lists */
                orderProperty: 'name',
                /* this contains the initial list of all items (i.e. the left side) */
                items: languagesNotAssignedToProject,
                /* this list should be initialized as empty or with any pre-selected items */
                selectedItems: vm.project.languages.slice()
            };

            $scope.$watchCollection('languageSelectOptions.selectedItems', function() {
                Language.updateProjectAssignment({projectId: vm.project.id}, $scope.languageSelectOptions.selectedItems);
            });
        });

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
