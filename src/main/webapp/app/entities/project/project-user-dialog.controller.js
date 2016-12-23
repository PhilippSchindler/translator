(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectUserManagementDialogController', ProjectUserManagementDialogController);

    ProjectUserManagementDialogController.$inject = ['$stateParams', '$uibModalInstance', 'entity', 'User', 'JhiLanguageService'];

    function ProjectUserManagementDialogController($stateParams, $uibModalInstance, entity, User, JhiLanguageService) {
        var vm = this;

        vm.projectId = $stateParams.id;

        vm.clear = clear;
        vm.languages = null;
        vm.save = save;
        vm.user = entity;

        if(entity.$promise) {
            entity.$promise.then(function (resolved) {
                for (let i = 0; i < resolved.authorities.length; i++) {
                    if (resolved.authorities[i] !== 'ROLE_USER') {
                        vm.user.authority = resolved.authorities[i];
                    }
                }
            });
        }


        JhiLanguageService.getAll().then(function (languages) {
            vm.languages = languages;
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function onSaveSuccess(result) {
            vm.isSaving = false;
            $uibModalInstance.close(result);
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        function save() {
            vm.isSaving = true;
            if (vm.user.id !== null) {
                User.updateProjectMember(vm.user, onSaveSuccess, onSaveError);
            } else {
                vm.user.projectId = vm.projectId;
                User.createProjectMember(vm.user, onSaveSuccess, onSaveError);
            }
        }
    }
})();
