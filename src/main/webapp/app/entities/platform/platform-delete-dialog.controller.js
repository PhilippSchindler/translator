(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('PlatformDeleteController',PlatformDeleteController);

    PlatformDeleteController.$inject = ['$uibModalInstance', 'entity', 'Platform'];

    function PlatformDeleteController($uibModalInstance, entity, Platform) {
        var vm = this;

        vm.platform = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Platform.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
