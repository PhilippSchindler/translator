(function() {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('platform', {
            parent: 'entity',
            url: '/platform',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'translatorApp.platform.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/platform/platforms.html',
                    controller: 'PlatformController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('platform');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('platform-detail', {
            parent: 'entity',
            url: '/platform/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'translatorApp.platform.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/platform/platform-detail.html',
                    controller: 'PlatformDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('platform');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Platform', function($stateParams, Platform) {
                    return Platform.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'platform',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('platform-detail.edit', {
            parent: 'platform-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/platform/platform-dialog.html',
                    controller: 'PlatformDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Platform', function(Platform) {
                            return Platform.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('platform.new', {
            parent: 'platform',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/platform/platform-dialog.html',
                    controller: 'PlatformDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('platform', null, { reload: 'platform' });
                }, function() {
                    $state.go('platform');
                });
            }]
        })
        .state('platform.edit', {
            parent: 'platform',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/platform/platform-dialog.html',
                    controller: 'PlatformDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Platform', function(Platform) {
                            return Platform.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('platform', null, { reload: 'platform' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('platform.delete', {
            parent: 'platform',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/platform/platform-delete-dialog.html',
                    controller: 'PlatformDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Platform', function(Platform) {
                            return Platform.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('platform', null, { reload: 'platform' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
