(function() {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('release', {
            parent: 'entity',
            url: '/project/{projectId}/release',
            data: {
                authorities: ['ROLE_CUSTOMER'],
                pageTitle: 'translatorApp.release.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/release/releases.html',
                    controller: 'ReleaseController',
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
        })
        .state('release-detail', {
            parent: 'entity',
            url: '/project/{projectId}/release/{id}',
            data: {
                authorities: ['ROLE_CUSTOMER'],
                pageTitle: 'translatorApp.release.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/release/release-detail.html',
                    controller: 'ReleaseDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('release');
                    return $translate.refresh();
                }],
                project: ['$stateParams', 'Project', function ($stateParams, Project) {
                    return Project.get({id: $stateParams.projectId}).$promise;
                }],
                entity: ['$stateParams', 'Release', function($stateParams, Release) {
                    return Release.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    return {
                        name: $state.current.name || 'release',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                }]
            }
        })
        .state('release.new', {
            parent: 'release',
            url: '/new',
            data: {
                authorities: ['ROLE_CUSTOMER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/release/release-dialog.html',
                    controller: 'ReleaseDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                deadline: null,
                                id: null
                            };
                        },
                        project: ['$stateParams', 'Project', function ($stateParams, Project) {
                            return Project.get({id: $stateParams.projectId}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('release', null, { reload: 'release' });
                }, function() {
                    $state.go('release');
                });
            }]
        })
    }

})();
