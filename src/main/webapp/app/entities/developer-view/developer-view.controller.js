(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('DeveloperViewController', DeveloperViewController);

    DeveloperViewController.$inject = ['$scope', '$rootScope', '$stateParams', 'project', 'definitions', 'Project', 'Definition', 'Release', 'User', 'Platform', 'Language', 'Translation'];

    function DeveloperViewController($scope, $rootScope, $stateParams, project, definitions, Project, Definition, Release, User, Platform, Language, Translation) {
        var vm = this;

        var importEnglish = {id: 0, name: "English"};
        vm.project = project;
        vm.newDefinition = {
            project: vm.project
        };
        vm.definitions = definitions;
        vm.importOrExport = 'import';
        vm.format = 'android';
        vm.language = importEnglish;
        vm.file = null;
        vm.numberOfImportedTranslations = null;
        vm.importLanguages = vm.project.languages.slice();
        vm.importLanguages.unshift(importEnglish);

        vm.createDefinition = function () {
            Definition.save(vm.newDefinition, onSaveSuccess, onSaveError);
        };

        function onSaveSuccess(result) {
            vm.definitions.push(result);
            vm.newDefinition = {
                project: vm.project
            };
            vm.isSaving = false;
        }

        function onSaveError(error) {
            console.log(error);
            vm.isSaving = false;
        }

        vm.importExport = function() {
            var file = $('#file')[0].files[0];
            if(file == undefined)
                return;

            var reader = new FileReader();
            reader.readAsText(file);
            reader.onload = function(){
                var fileContent = reader.result;
                Translation.import({format: vm.format, languageId: vm.language.id}, fileContent,
                function (response) {
                    vm.numberOfImportedTranslations = response.numberOfImportedTranslations;
                    Definition.getForProject({projectId: vm.project.id}, function(response){
                        vm.definitions = response;
                    });
                });
            }
        }
    }
})();
