(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ProjectController', ProjectController);

    ProjectController.$inject = ['$scope', '$state', 'Project'];

    function ProjectController ($scope, $state, Project) {
        var vm = this;

        vm.projects = [];

        loadAll();

        function loadAll() {
            Project.query(function(result) {
                vm.projects = result;
                vm.searchQuery = null;
            });
        }

        vm.replaceNewLineWithBR = function (str) {
            return str.replace(/(?:\r\n|\r|\n)/g, '<br />');
        }
    }
})();
