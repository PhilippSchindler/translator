(function() {
    'use strict';

    angular
        .module('translatorApp')
        .controller('TranslationController', TranslationController);

    TranslationController.$inject = ['$scope', '$state', '$filter', 'Translation', 'Project', 'Definition'];

    function TranslationController ($scope, $state, $filter, Translation, Project, Definition) {
        var vm = this;

        vm.userId = 5; //TODO replace with real logged in user id

        vm.definitions = [];
        vm.project = null;
        vm.changedDefinitionIds = new Set();

        loadAll();

        function loadAll() {
            vm.changedDefinitionIds.clear();
            Project.getByUser({userId: vm.userId}, function(result){
                vm.project = result;

                Definition.queryLatestByProject({projectId: vm.project.id}, function(resultDefinition) {
                    vm.definitions = $filter('orderBy')(resultDefinition, 'label');
                });
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
