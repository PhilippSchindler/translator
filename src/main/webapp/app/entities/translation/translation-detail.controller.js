(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslationDetailController', TranslationDetailController);

    TranslationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Translation', 'Language', 'Definition'];

    function TranslationDetailController($scope, $rootScope, $stateParams, previousState, entity, Translation, Language, Definition) {
        var vm = this;

        vm.translation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('translatorApp:translationUpdate', function(event, result) {
            vm.translation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
