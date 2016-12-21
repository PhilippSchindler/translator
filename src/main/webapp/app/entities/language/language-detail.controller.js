(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LanguageDetailController', LanguageDetailController);

    LanguageDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Language', 'User'];

    function LanguageDetailController($scope, $rootScope, $stateParams, previousState, entity, Language, User) {
        var vm = this;

        vm.language = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('translatorApp:languageUpdate', function(event, result) {
            vm.language = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
