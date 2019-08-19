app.controller("indexController", function ($scope, loginService) {


    $scope.showLoginName = function () {
        loginService.loginName().success(function (response) {
            $scope.entity = response;
        })
    }
})