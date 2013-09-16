'use strict';

/* Controllers */


function HomeCtrl() {}
HomeCtrl.$inject = [];


gozerApp.controller('FileUploadCtrl',
    ['$scope', '$rootScope',
        function ($scope, $rootScope) {
            $scope.files = [];
            $scope.percentage = 0;

            $scope.upload = function () {
//                uploadManager.upload();
                $scope.files = [];
            };

            $rootScope.$on('fileAdded', function (e, call) {
                $scope.files.push(call);
                $scope.$apply();
            });

            $rootScope.$on('uploadProgress', function (e, call) {
                $scope.percentage = call;
                $scope.$apply();
            });
        }]);



