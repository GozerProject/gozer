'use strict';


// Declare app level module which depends on filters, and services
var gozerApp = angular.module('gozerApp', []);

gozerApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/home', {templateUrl: 'views/home.html', controller: HomeCtrl});
    $routeProvider.otherwise({redirectTo : '/home'});
  }]);

