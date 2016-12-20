(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('StatisticsController', StatisticsController);

    StatisticsController.$inject = ['$scope', '$state', 'notTranslatedTexts'];

    function StatisticsController($scope, $state, notTranslatedTexts) {
        var vm = this;

        vm.notTranslatedTexts = {};

        vm.notTranslatedTexts.type = "ColumnChart";


        vm.notTranslatedTexts.data = {
            "cols": [
                {id: "t", label: "Sprache", type: "string"},
                {id: "s", label: "Noch nicht übersetzt", type: "number"}
            ], "rows": []
        };

        vm.notTranslatedTexts.options = {
            'title': 'Anzahl noch nicht übersetzter Texte je Sprache'
        };

        notTranslatedTexts.forEach(function (entry) {
            vm.notTranslatedTexts.data.rows.push({
                c: [
                    {v: entry.language},
                    {v: entry.missingTranslations}
                ]
            });
        });


    }
})();
