(function() {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('translator-view', {
            parent: 'entity',
            url: '/project/{projectId}/translator-view',
            data: {
                authorities: ['ROLE_ADMIN', 'ROLE_TRANSLATOR'],
                pageTitle: 'translatorApp.translatorView.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/translator-view/translator-view.html',
                    controller: 'TranslatorViewController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('translator-view');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }],
                project: ['$stateParams', 'Project', function ($stateParams, Project) {
                    return Project.get({id: $stateParams.projectId}).$promise;
                }]
            }
        })
    }
})();
