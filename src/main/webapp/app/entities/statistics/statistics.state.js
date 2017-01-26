(function () {
    'use strict';

    angular
        .module('translatorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('statistics', {
                parent: 'entity',
                url: '/project/{projectId}/statistics',
                data: {
                    authorities: ['ROLE_CUSTOMER', 'ROLE_TRANSLATOR'],
                    pageTitle: 'translatorApp.statistics.home.title'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/entities/statistics/statistics.html',
                        controller: 'StatisticsController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('home');
                        $translatePartialLoader.addPart('statistics');
                        return $translate.refresh();
                    }],
                    notTranslatedTexts: ['$stateParams', 'Statistics', function ($stateParams, Statistics) {
                        return Statistics.getNotTranslatedTexts({projectId: $stateParams.projectId}).$promise;
                    }],
                    usersByRole: ['$stateParams', 'Statistics', function ($stateParams, Statistics) {
                        return Statistics.getUsersByRole({projectId: $stateParams.projectId}).$promise;
                    }],
                }
            });
    }
})();
