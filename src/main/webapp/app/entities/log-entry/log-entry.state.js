(function() {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('log-entry', {
            parent: 'entity',
            url: '/log-entry',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'translatorApp.logEntry.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/log-entry/log-entries.html',
                    controller: 'LogEntryController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('logEntry');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('log-entry-detail', {
            parent: 'entity',
            url: '/log-entry/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'translatorApp.logEntry.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/log-entry/log-entry-detail.html',
                    controller: 'LogEntryDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('logEntry');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'LogEntry', function($stateParams, LogEntry) {
                    return LogEntry.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'log-entry',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('log-entry-detail.edit', {
            parent: 'log-entry-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/log-entry/log-entry-dialog.html',
                    controller: 'LogEntryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LogEntry', function(LogEntry) {
                            return LogEntry.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('log-entry.new', {
            parent: 'log-entry',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/log-entry/log-entry-dialog.html',
                    controller: 'LogEntryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                timestamp: null,
                                message: null,
                                result: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('log-entry', null, { reload: 'log-entry' });
                }, function() {
                    $state.go('log-entry');
                });
            }]
        })
        .state('log-entry.edit', {
            parent: 'log-entry',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/log-entry/log-entry-dialog.html',
                    controller: 'LogEntryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['LogEntry', function(LogEntry) {
                            return LogEntry.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('log-entry', null, { reload: 'log-entry' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('log-entry.delete', {
            parent: 'log-entry',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/log-entry/log-entry-delete-dialog.html',
                    controller: 'LogEntryDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['LogEntry', function(LogEntry) {
                            return LogEntry.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('log-entry', null, { reload: 'log-entry' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
