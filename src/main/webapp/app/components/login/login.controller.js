(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$rootScope', '$state', '$timeout', 'Principal', 'Auth', '$uibModalInstance', 'Project'];

    function LoginController ($rootScope, $state, $timeout, Principal, Auth, $uibModalInstance, Project) {
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

        $timeout(function (){angular.element('#username').focus();});

        function cancel () {
            vm.credentials = {
                username: null,
                password: null,
                rememberMe: true
            };
            vm.authenticationError = false;
            $uibModalInstance.dismiss('cancel');
        }

        function login (event) {
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

                Principal.identity().then(function(account) {
                    let userAuthority = account.authorities[0];
                    if(userAuthority === 'ROLE_CUSTOMER')
                        $state.go('project');
                    else {
                        Project.getByUser({userLogin: account.login}, function(project){
                            if(userAuthority === 'ROLE_DEVELOPER')
                                $state.go('developer-view', {projectId: project.id});
                            else if(userAuthority === 'ROLE_TRANSLATOR')
                                $state.go('translator-view', {projectId: project.id});
                            else if(userAuthority === 'ROLE_RELEASE_MANAGER')
                                $state.go('release', {projectId: project.id});
                            else
                                $state.go('home');
                        });
                    }
                });
            }).catch(function () {
                vm.authenticationError = true;
            });
        }

        function register () {
            $uibModalInstance.dismiss('cancel');
            $state.go('register');
        }

        function requestResetPassword () {
            $uibModalInstance.dismiss('cancel');
            $state.go('requestReset');
        }
    }
})();
