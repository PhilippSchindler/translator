(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseDeleteController',ReleaseDeleteController);

    ReleaseDeleteController.$inject = ['$uibModalInstance', 'entity', 'Release'];

    function ReleaseDeleteController($uibModalInstance, entity, Release) {
        var vm = this;

        vm.release = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Release.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
