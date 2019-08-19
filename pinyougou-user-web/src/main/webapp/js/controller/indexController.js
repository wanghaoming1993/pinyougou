app.controller("indexController", function ($scope, indexService) {

  $scope.login = function () {
    indexService.login().success(function (response) {

      $scope.loginName = response.loginName;

    })
  }


})