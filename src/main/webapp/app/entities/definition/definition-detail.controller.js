(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DefinitionDetailController', DefinitionDetailController);

    DefinitionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Definition', 'Translation', 'Project'];

    function DefinitionDetailController($scope, $rootScope, $stateParams, previousState, entity, Definition, Translation, Project) {
        var vm = this;

        vm.definition = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('translatorApp:definitionUpdate', function(event, result) {
            vm.definition = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
