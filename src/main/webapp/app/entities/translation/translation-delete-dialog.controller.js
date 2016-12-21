(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslationDeleteController',TranslationDeleteController);

    TranslationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Translation'];

    function TranslationDeleteController($uibModalInstance, entity, Translation) {
        var vm = this;

        vm.translation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Translation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
