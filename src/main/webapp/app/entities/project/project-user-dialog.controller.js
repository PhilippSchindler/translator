(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectUserManagementDialogController',ProjectUserManagementDialogController);

    ProjectUserManagementDialogController.$inject = ['$stateParams', '$uibModalInstance', 'entity', 'User', 'JhiLanguageService'];

    function ProjectUserManagementDialogController ($stateParams, $uibModalInstance, entity, User, JhiLanguageService) {
        var vm = this;

        vm.authorities = ['ROLE_USER', 'ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_DEVELOPER', 'ROLE_TRANSLATOR', 'ROLE_RELEASE_MANAGER'];
        vm.clear = clear;
        vm.languages = null;
        vm.save = save;
        vm.user = entity;


        JhiLanguageService.getAll().then(function (languages) {
            vm.languages = languages;
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function onSaveSuccess (result) {
            vm.isSaving = false;
            $uibModalInstance.close(result);
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        function save () {
            vm.isSaving = true;
            if (vm.user.id !== null) {
                User.update(vm.user, onSaveSuccess, onSaveError);
            } else {
                vm.user.langKey
                User.save(vm.user, onSaveSuccess, onSaveError);
            }
        }
    }
})();
