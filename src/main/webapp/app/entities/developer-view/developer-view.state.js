(function () {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('developer-view', {
                parent: 'entity',
                url: '/project/{projectId}/developer-view',
                data: {
                    authorities: ['ROLE_DEVELOPER'],
                    pageTitle: 'translatorApp.developerView.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/developer-view/developer-view.html',
                        controller: 'DeveloperViewController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('developer-view');
                        $translatePartialLoader.addPart('definition');
                        $translatePartialLoader.addPart('project');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }],
                    project: ['$stateParams', 'Project', function ($stateParams, Project) {
                        return Project.get({id: $stateParams.projectId}).$promise;
                    }],
                    definitions: ['$stateParams', 'Definition', function ($stateParams, Definition) {
                        return Definition.getForProject({projectId: $stateParams.projectId}).$promise;
                    }],
                    releases: ['$stateParams', 'Release', function ($stateParams, Release) {
                        return Release.getByProject({projectId: $stateParams.projectId}).$promise;
                    }]
                }
            })
            .state('definition.edit', {
                parent: 'developer-view',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_DEVELOPER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/definition/definition-dialog.html',
                        controller: 'DefinitionDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: ['Definition', function (Definition) {
                                return Definition.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('developer-view', null, {reload: 'developer-view'});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('definition.delete', {
                parent: 'developer-view',
                url: '/{id}/delete',
                data: {
                    authorities: ['ROLE_DEVELOPER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/definition/definition-delete-dialog.html',
                        controller: 'DefinitionDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['Definition', function (Definition) {
                                return Definition.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('developer-view', null, {reload: 'developer-view'});
                    }, function () {
                        $state.go('^');
                    });
                }]
            });
    }

})();
