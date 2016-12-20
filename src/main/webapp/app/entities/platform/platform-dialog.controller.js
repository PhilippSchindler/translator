(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('PlatformDialogController', PlatformDialogController);

    PlatformDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Platform'];

    function PlatformDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Platform) {
        var vm = this;

        vm.platform = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.platform.id !== null) {
                Platform.update(vm.platform, onSaveSuccess, onSaveError);
            } else {
                Platform.save(vm.platform, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('translatorApp:platformUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
