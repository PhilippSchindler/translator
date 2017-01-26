(function () {
    'use strict';

    angular
        .module('translatorApp')
        .controller('StatisticsController', StatisticsController);

    StatisticsController.$inject = ['$scope', '$state', 'notTranslatedTexts', 'usersByRole'];

    function StatisticsController($scope, $state, notTranslatedTexts, usersByRole) {
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

        vm.usersByRole = {};
        vm.usersByRole.type = "ColumnChart";
        vm.usersByRole.data = {
            "cols": [
                {id: "t", label: "Rolle", type: "string"},
                {id: "s", label: "Anzahl", type: "number"}
            ], "rows": [
                {c: [
                    {v: "Entwickler"},
                    {v: usersByRole.developers}
                ]},
                {c: [
                    {v: "Übersetzer"},
                    {v: usersByRole.translators},
                ]},
                {c: [
                    {v: "Release-Manager"},
                    {v: usersByRole.releaseManagers},
                ]}]
        };
        vm.usersByRole.options = {
                    'title': 'Anzahl der Benutzer je Rolle'
                };

        vm.googleChartSizeFix = function() {
            $('div[name=chart]').css({ opacity:"0" });
            $(window).resize();

        };

        $scope.displayGoogleCharts = function() {
            $('div[name=chart]').css({ opacity:"1" });
        };

    }
})();
