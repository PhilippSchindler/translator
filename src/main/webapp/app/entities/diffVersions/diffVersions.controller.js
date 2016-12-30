(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DiffVersionsController', DiffVersionsController);

    DiffVersionsController.$inject = ['$scope', '$rootScope', '$stateParams', 'project'];

    function DiffVersionsController($scope, $rootScope, $stateParams, project) {
        var vm = this;
        vm.project = project;

    }
})();
