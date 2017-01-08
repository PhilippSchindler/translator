(function () {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/project',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER'],
                    pageTitle: 'translatorApp.project.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/project/projects.html',
                        controller: 'ProjectController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('project');
                        $translatePartialLoader.addPart('global');
                        return $translate.refresh();
                    }]
                }
            })
            .state('project-detail', {
                parent: 'entity',
                url: '/project/{id}',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER'],
                    pageTitle: 'translatorApp.project.detail.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/project/project-detail.html',
                        controller: 'ProjectDetailController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('project');
                        $translatePartialLoader.addPart('user-management');
                        $translatePartialLoader.addPart('language');
                        return $translate.refresh();
                    }],
                    entity: ['$stateParams', 'Project', function ($stateParams, Project) {
                        return Project.get({id: $stateParams.id}).$promise;
                    }],
                    previousState: ["$state", function ($state) {
                        var currentStateData = {
                            name: $state.current.name || 'project',
                            params: $state.params,
                            url: $state.href($state.current.name, $state.params)
                        };
                        return currentStateData;
                    }]
                }
            })
            .state('project-detail.edit', {
                parent: 'project-detail',
                url: '/detail/edit',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: ['Project', function (Project) {
                                return Project.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('^', {}, {reload: false});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('project-detail.newUser', {
                parent: 'project-detail',
                url: '/detail/newUser',
                data: {
                    authorities: ['ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-user-dialog.html',
                        controller: 'ProjectUserManagementDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    id: null, login: null, firstName: null, lastName: null, email: null,
                                    activated: true, langKey: null, createdBy: null, createdDate: null,
                                    lastModifiedBy: null, lastModifiedDate: null, resetDate: null,
                                    resetKey: null, authorities: null
                                };
                            }
                        }
                    }).result.then(function () {
                        $state.go('^', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('project-detail.newLanguage', {
                parent: 'project-detail',
                url: '/detail/newLanguage',
                data: {
                    authorities: ['ROLE_ADMIN', 'ROLE_CUSTOMER', 'ROLE_USER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/language/language-dialog.html',
                        controller: 'LanguageDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    name: null,
                                    shortName: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function() {
                        $state.go('^', null, { reload: true });
                    }, function() {
                        $state.go('^');
                    });
                }]
            })
            .state('project-detail.editUser', {
                parent: 'project-detail',
                url: '/detail/editUser/{login}',
                data: {
                    authorities: ['ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-user-dialog.html',
                        controller: 'ProjectUserManagementDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: ['User', function(User) {
                                return User.get({login : $stateParams.login});
                            }]
                        }
                    }).result.then(function () {
                        $state.go('^', null, {reload: true});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('project-detail.deleteUser', {
                parent: 'project-detail',
                url: '/detail/deleteUser/{login}',
                data: {
                    authorities: ['ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/admin/user-management/user-management-delete-dialog.html',
                        controller: 'UserManagementDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['User', function(User) {
                                return User.get({login : $stateParams.login});
                            }]
                        }
                    }).result.then(function() {
                        $state.go('^', null, { reload: true });
                    }, function() {
                        $state.go('^');
                    });
                }]
            })
            .state('project.new', {
                parent: 'project',
                url: '/new',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: function () {
                                return {
                                    name: null,
                                    description: null,
                                    id: null
                                };
                            }
                        }
                    }).result.then(function () {
                        $state.go('project', null, {reload: 'project'});
                    }, function () {
                        $state.go('project');
                    });
                }]
            })
            .state('project.edit', {
                parent: 'project',
                url: '/{id}/edit',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController',
                        controllerAs: 'vm',
                        backdrop: 'static',
                        size: 'lg',
                        resolve: {
                            entity: ['Project', function (Project) {
                                return Project.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('project', null, {reload: 'project'});
                    }, function () {
                        $state.go('^');
                    });
                }]
            })
            .state('project.delete', {
                parent: 'project',
                url: '/{id}/delete',
                data: {
                    authorities: ['ROLE_USER', 'ROLE_CUSTOMER']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'app/entities/project/project-delete-dialog.html',
                        controller: 'ProjectDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['Project', function (Project) {
                                return Project.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function () {
                        $state.go('project', null, {reload: 'project'});
                    }, function () {
                        $state.go('^');
                    });
                }]
            });
    }

})();
