(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DiffVersionsController', DiffVersionsController);

    DiffVersionsController.$inject = ['$scope', '$rootScope', '$stateParams', 'project', 'DiffVersions', '$filter'];

    function DiffVersionsController($scope, $rootScope, $stateParams, project, DiffVersions, $filter) {
        var vm = this;
        vm.project = project;
        vm.version1 = 0;
        vm.version2 = 0;
        vm.onlyShowChanges = false;
        vm.definitions = null;
        vm.definitionsCompare = undefined;

        vm.versions = DiffVersions.listOfAllVersions({'projectId': vm.project.id});

        vm.displayText = function (version) {
            return "Version " + version;
        };

        $scope.languageSelectOptions = {
            title: "Sprachen",
            filterPlaceHolder: $filter('translate')('multiselect.filterPlaceHolder'),
            labelAll: $filter('translate')('multiselect.labelAll'),
            labelSelected: $filter('translate')('multiselect.labelSelected'),
            selectAll: $filter('translate')('multiselect.selectAll'),
            deselectAll: $filter('translate')('multiselect.deselectAll'),
            helpMessage: $filter('translate')('multiselect.helpMessage'),
            /* angular will use this to filter your lists */
            orderProperty: 'name',
            /* this contains the initial list of all items (i.e. the left side) */
            items: vm.project.languages.slice(),
            /* this list should be initialized as empty or with any pre-selected items */
            selectedItems: []
        };

        vm.loadVersions = function () {
           DiffVersions.getDefinitions({'projectId': vm.project.id, 'version': vm.version2}, function (resultDefinition) {
                vm.definitionsCompare = resultDefinition;
                vm.definitions = DiffVersions.getDefinitions({'projectId': vm.project.id, 'version': vm.version1});
            });
        };

        vm.isRowToHide = function (defintion) {
            if (vm.onlyShowChanges) {
                var definition2 = vm.getCompareDefinition(defintion.label);

                if (definition2 == null) {
                    return true;
                } else if (defintion.text != definition2.text) {
                    return false;
                }

                for (var i = 0; i < vm.project.languages.length; i++) {
                    var lang = vm.project.languages[i];

                    if (!vm.isLangToHide(lang)) {
                        var translation1 = vm.getTranslation(defintion.translations);
                        var translation2 = vm.getTranslation(defintion2.translations);

                        if (translation1 != null && translation2 != null && translation1.text != translation2.text) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return false;
            }

        };

        vm.isLangToHide = function (lang) {
            for (var i = 0; i < $scope.languageSelectOptions.selectedItems.length; i++) {
                var selectedLang = $scope.languageSelectOptions.selectedItems[i];
                if (selectedLang.name == lang.name)
                    return false;
            }
            return true;
        };

        vm.getTranslation = function (translations, language) {
            if(translations != undefined) {
                for (var i = 0; i < translations.length; i++) {
                    var t = translations[i];
                    if (t.language.id === language.id)
                        return t;
                }
            }
            return null;
        };

        vm.getCompareDefinition = function(label) {
            if(vm.definitionsCompare != undefined) {
                for (var i = 0; i < vm.definitionsCompare.length; i++) {
                    var definition = vm.definitionsCompare[i];
                    if (definition.label == label) {
                        return definition;
                    }
                }
            }

            return null;
        };

        vm.getColor = function (text1, text2) {
            if(text1 != text2 && text1 == undefined) {
                return "green";
            }

            if(text1 != text2 && text2 != undefined) {
                return "red";
            }

        };


    }
})();
