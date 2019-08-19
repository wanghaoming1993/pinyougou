app.controller("contentController", function ($scope, contentService) {

    //通过categoryId查询
    $scope.contentList = [];
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId] = response;

        })
    }

    $scope.enterSearch = function () {
        location.href = "http://localhost:9083/#?keywords=" + $scope.searchMap.keywords;
    }
})