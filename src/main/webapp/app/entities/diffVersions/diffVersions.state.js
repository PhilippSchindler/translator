(function () {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('diffVersions', {
                parent: 'entity',
                url: '/project/{projectId}/diffVersions',
                data: {
                    authorities: ['ROLE_CUSTOMER', 'ROLE_ADMIN'],
                    pageTitle: 'Diff Versions'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/diffVersions/diffVersions.html',
                        controller: 'DiffVersionsController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('release');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    project: ['$stateParams', 'Project', function ($stateParams, Project) {
                        return Project.get({id: $stateParams.projectId}).$promise;
                    }]
                }
            });
    }
})();
