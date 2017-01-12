(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectLogController', ProjectLogController);

    ProjectLogController.$inject = ['$scope', '$rootScope', '$stateParams', 'project', 'Project', '$filter'];

    function ProjectLogController($scope, $rootScope, $stateParams, project, Project, $filter) {
        var vm = this;
        vm.project = project;
    }
})();
