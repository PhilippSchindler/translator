(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$rootScope', '$state', '$timeout', '$translate', '$localStorage', 'Principal', 'Auth', '$uibModalInstance', 'Project'];

    function LoginController($rootScope, $state, $timeout, $translate, $localStorage, Principal, Auth, $uibModalInstance, Project) {
        var vm = this;

        vm.authenticationError = false;
        vm.cancel = cancel;
        vm.credentials = {};
        vm.login = login;
        vm.password = null;
        vm.register = register;
        vm.rememberMe = true;
        vm.requestResetPassword = requestResetPassword;
        vm.username = null;

        $timeout(function () {
            angular.element('#username').focus();
        });

        function cancel() {
            vm.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };
            vm.authenticationError = false;
            $uibModalInstance.dismiss('cancel');
        }

        function login(event) {
            event.preventDefault();
            Auth.login({
                username: vm.username,
                password: vm.password,
                rememberMe: vm.rememberMe
            }).then(function () {
                vm.authenticationError = false;
                $uibModalInstance.close();
                if ($state.current.name === 'register' || $state.current.name === 'activate' ||
                    $state.current.name === 'finishReset' || $state.current.name === 'requestReset') {
                    $state.go('home');
                }

                $rootScope.$broadcast('authenticationSuccess');

                Principal.identity().then(function (account) {
                    if (account.authorities.includes('ROLE_ADMIN')) {
                        $rootScope.homePath = '';
                        $state.go('home');
                    } else if (account.authorities.includes('ROLE_CUSTOMER')) {
                        $rootScope.homePath = 'project';
                        $state.go('project');
                    } else {
                        loadProjectAndNavigate(account, account.authorities);
                    }
                    $localStorage.homePath = $rootScope.homePath;
                });
            }).catch(function () {
                vm.authenticationError = true;
            });
        }

        function loadProjectAndNavigate(account, userAuthorities) {
            Project.getByUser({userLogin: account.login}, function (project) {
                if (!project.id) {
                    $state.go('error');
                    $rootScope.errorMessage = $translate.instant('error.couldNotLoadProject');
                    return;
                }

                if (userAuthorities.includes('ROLE_DEVELOPER')) {
                    $rootScope.homePath = 'project/' + project.id + '/developer-view';
                    $state.go('developer-view', {projectId: project.id});
                } else if (userAuthorities.includes('ROLE_TRANSLATOR')) {
                    $rootScope.homePath = 'project/' + project.id + '/translator-view';
                    $state.go('translator-view', {projectId: project.id});
                } else if (userAuthorities.includes('ROLE_RELEASE_MANAGER')) {
                    $rootScope.homePath = 'project/' + project.id + '/release-manager-view';
                    $state.go('release-manager-view', {projectId: project.id});
                } else {
                    $state.go('home');
                }
                $localStorage.homePath = $rootScope.homePath;
            });
        }

        function register() {
            $uibModalInstance.dismiss('cancel');
            $state.go('register');
        }

        function requestResetPassword() {
            $uibModalInstance.dismiss('cancel');
            $state.go('requestReset');
        }
    }
})();
