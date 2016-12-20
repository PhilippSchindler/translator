(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslatorViewController', TranslatorViewController);

    TranslatorViewController.$inject = ['$scope', '$state', '$filter', 'project', 'Translation', 'Project', 'Definition'];

    function TranslatorViewController ($scope, $state, $filter, project, Translation, Project, Definition) {
        var vm = this;

        vm.definitions = [];
        vm.project = project;
        vm.changedDefinitionIds = new Set();
        vm.onlyShowNotTranslated = false;
        vm.definitionIdsFullyTranslatedToHide = new Set();
        vm.definitionIdsSearchToHide = new Set();
        vm.searches = {};

        loadAll();

        function loadAll() {
            vm.changedDefinitionIds.clear();
            Definition.queryLatestByProject({projectId: vm.project.id}, function(resultDefinition) {
                vm.definitions = $filter('orderBy')(resultDefinition, 'label');
            });
        }

        vm.getTranslation = function(translations, language){
            for(var i=0; i < translations.length; i++){
                var t = translations[i];
                for(var j=0; j< t.languages.length; j++){
                    var l = t.languages[j];
                    if(l.id === language.id)
                        return t;
                }
            }
            return "";
        }

        vm.markDefinitionAsChanged = function(definition){
            vm.changedDefinitionIds.add(definition.id);
        }

        vm.onlyShowNotTranslatedChanged = function(){
            if(vm.onlyShowNotTranslated){
                for(var j=0; j<vm.definitions.length; j++){
                    let definition = vm.definitions[j];
                    let foundEmptyCell = false;
                    for(var i=0; i<vm.project.languages.length; i++){
                        let lang = vm.project.languages[i];
                        if($('#' + definition.id + lang.name).val() === ""){
                            foundEmptyCell = true;
                            break;
                        }
                    }
                    if(!foundEmptyCell)
                        vm.definitionIdsFullyTranslatedToHide.add(definition.id);
                }
            } else {
                vm.definitionIdsFullyTranslatedToHide.clear();
            }
        }

        vm.searchChanged = function(){
            vm.definitionIdsSearchToHide.clear();
            for(var j=0; j<vm.definitions.length; j++){
                let definition = vm.definitions[j];
                let labelSearch = vm.searches['Label'];
                if(definition.label.toLowerCase().search(labelSearch == undefined ? "" : labelSearch.toLowerCase()) < 0){
                    vm.definitionIdsSearchToHide.add(definition.id);
                    continue;
                }
                let englishSearch = vm.searches['English'];
                if(definition.text.toLowerCase().search(englishSearch == undefined ? "" : englishSearch.toLowerCase()) < 0){
                    vm.definitionIdsSearchToHide.add(definition.id);
                    continue;
                }
                let foundWrongCell = false;
                for(var i=0; i<vm.project.languages.length; i++){
                    let lang = vm.project.languages[i];
                    let inputValue = $('#' + definition.id + lang.name).val();
                    let searchLang = vm.searches[lang.name];
                    if(inputValue.toLowerCase().search(searchLang == undefined ? "" : searchLang.toLowerCase()) < 0){
                        foundWrongCell = true;
                        break;
                    }
                }
                if(foundWrongCell)
                    vm.definitionIdsSearchToHide.add(definition.id);
            }
        }

        vm.isRowToHide = function(definition){
            if(vm.definitionIdsFullyTranslatedToHide.has(definition.id))
                return true;

            if(vm.definitionIdsSearchToHide.has(definition.id))
                return true;

            return false;
        }

        vm.save = function(){
            let definitionsToUpdate = [];
            for(let definitionId of vm.changedDefinitionIds){
                let translations = [];
                for(var i=0; i<vm.project.languages.length; i++){
                    let lang = vm.project.languages[i];
                    translations.push({langId: lang.id, text: $('#' + definitionId + lang.name).val()});
                }
                definitionsToUpdate.push({definitionId: definitionId, translations: translations});
            }
            Translation.updateChangedTranslations(definitionsToUpdate,
                function(){loadAll();});
        }
    }
})();
