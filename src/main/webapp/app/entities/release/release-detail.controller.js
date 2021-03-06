(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('ReleaseDetailController', ReleaseDetailController);

    ReleaseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Release', 'Definition', 'Project', 'project', 'user'];

    function ReleaseDetailController($scope, $rootScope, $stateParams, previousState, entity, Release, Definition, Project, project, user) {
        var vm = this;

        vm.release = entity;
        vm.previousState = previousState.name;
        vm.project = project;

        vm.containsReleaseManager = function (authorities) {
            for (var index in authorities) {
                if (authorities.hasOwnProperty(index) && authorities[index] == 'ROLE_RELEASE_MANAGER') {
                    return true;
                }
            }
            return false;
        };
        vm.isReleaseManager = vm.containsReleaseManager(user.data.authorities);

        vm.loadData = function () {
            vm.definitions = [];
            vm.selectedVersion = [];
            vm.selectedLabel = [];

            Release.getSelectedVersions({'releaseId': vm.release.id}, function (result) {
                var selectedVersions = result.selectedVersions;
                for (var index in selectedVersions) {
                    if (selectedVersions.hasOwnProperty(index)) {
                        var selectedVersion = selectedVersions[index];
                        vm.selectedVersion[selectedVersion.label] = selectedVersion.version + "";
                    }
                }
                for (var label in vm.selectedVersion) {
                    if (vm.selectedVersion.hasOwnProperty(label)) {
                        vm.selectedLabel[label] = true;
                    }
                }

                Definition.getGroupedForProject({'projectId': vm.project.id}, function (result) {
                    vm.definitions = result;
                    var selectAll = Object.keys(vm.selectedVersion).length == 0 && vm.isReleaseManager;
                    for (var key in vm.definitions.definitions) {
                        if (vm.definitions.definitions.hasOwnProperty(key)) {
                            if (!(vm.selectedVersion[key])) {
                                vm.selectedVersion[key] = vm.getLatestVersion(vm.definitions.definitions[key]);
                                vm.selectedLabel[key] = selectAll;
                            }
                        }
                    }
                });
            });
        };
        vm.loadData();

        vm.getLatestVersion = function (definitions) {
            var version = 0;
            var allLanguagesTranslated = false;
            definitions.forEach(function (definition) {
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
                    selectedVersions.push({label: label, version: vm.selectedVersion[label]});
                }
            }
            Release.saveSelectedVersions({'releaseId': vm.release.id}, {selectedVersions: selectedVersions}, function () {
                vm.release = Release.get({'id': vm.release.id}, function () {
                    vm.loadData();
                });
            });
        }

    }
})();
