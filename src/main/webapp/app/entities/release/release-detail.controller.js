(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseDetailController', ReleaseDetailController);

    ReleaseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Release', 'Definition', 'Project', 'project'];

    function ReleaseDetailController($scope, $rootScope, $stateParams, previousState, entity, Release, Definition, Project, project) {
        var vm = this;

        vm.release = entity;
        vm.previousState = previousState.name;
        vm.project = project;

        vm.definitions = [];
        vm.selectedVersion = [];
        vm.selectedLabel = [];

        Release.getSelectedVersions({'releaseId': vm.release.id}, function (result) {
            vm.selectedVersion = result.selectedVersions;
            console.log(vm.selectedVersion);
            for (var label in vm.selectedVersion) {
                if (vm.selectedVersion.hasOwnProperty(label)) {
                    vm.selectedVersion[label] = true;
                }
            }

            Definition.getGroupedForProject({'projectId': vm.project.id}, function (result) {
                vm.definitions = result;
                if (vm.selectedVersion == undefined || vm.selectedVersion.length == 0) {
                    vm.selectedVersion = [];
                    for (var key in vm.definitions.definitions) {
                        if (vm.definitions.definitions.hasOwnProperty(key)) {
                            vm.selectedVersion[key] = vm.getLatestVersion(vm.definitions.definitions[key]);
                            vm.selectedLabel[key] = true;
                        }
                    }
                }
                console.log(vm.selectedVersion);
            });
        });

        vm.getLatestVersion = function (defs) {
            var version = 0;
            var allLanguagesTranslated = false;
            defs.forEach(function (definition) {
                if (definition.version > version) {
                    version = definition.version;
                    allLanguagesTranslated = true;
                    vm.project.languages.forEach(function (lang) {
                        var found = false;
                        definition.translations.forEach(function (translation) {
                            if (translation.language.id == lang.id && translation.text != "") {
                                found = true;
                            }
                        });
                        if (!found) {
                            allLanguagesTranslated = false;
                        }
                    })
                }
            });
            if (allLanguagesTranslated) {
                return version + "";
            }
            return "-1";
        };

        var unsubscribe = $rootScope.$on('translatorApp:releaseUpdate', function (event, result) {
            vm.release = result;
        });
        $scope.$on('$destroy', unsubscribe);

        vm.getTranslationText = function (definition, lang) {
            var text = "";
            definition.translations.forEach(function (translation) {
                if (translation.language.id == lang.id) {
                    text = translation.text;
                }
            });
            return text;
        };

        vm.save = function () {
            var selectedVersions = [];
            for (var label in vm.selectedLabel) {
                if (vm.selectedLabel.hasOwnProperty(label) && vm.selectedLabel[label]) {
                    selectedVersions[label] = vm.selectedVersion[label];
                }
            }
            console.log(selectedVersions);
            Release.saveSelectedVersions({'releaseId': vm.release.id}, {selectedVersions: selectedVersions});
        }

    }
})();
