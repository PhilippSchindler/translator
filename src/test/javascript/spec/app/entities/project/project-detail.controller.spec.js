'use strict';

describe('Controller Tests', function() {

    describe('Project Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockProject, MockDefinition, MockRelease, MockUser, MockPlatform, MockLanguage;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockProject = jasmine.createSpy('MockProject');
            MockDefinition = jasmine.createSpy('MockDefinition');
            MockRelease = jasmine.createSpy('MockRelease');
            MockUser = jasmine.createSpy('MockUser');
            MockPlatform = jasmine.createSpy('MockPlatform');
            MockLanguage = jasmine.createSpy('MockLanguage');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Project': MockProject,
                'Definition': MockDefinition,
                'Release': MockRelease,
                'User': MockUser,
                'Platform': MockPlatform,
                'Language': MockLanguage
            };
            createController = function() {
                $injector.get('$controller')("ProjectDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'translatorApp:projectUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
