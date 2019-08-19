app.controller("itemSearchController", function ($scope, itemSearchService, $location) {

    $scope.searchMap = { 'keywords': '', 'category': '', 'brand': '', 'spec': {}, 'price': '', 'pageNo': 1, 'pageSize': 40, 'sort': '', 'sortField': '' };
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo)
        itemSearchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            buildPageLoder();

        })
    }

    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.searchMap.pageNo = 1;
        $scope.search();
    }

    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.searchMap.pageNo = 1;
        $scope.search();
    }
    //构建页面栏
    buildPageLoder = function () {
        var start;
        var end;
        if ($scope.resultMap.totalPages <= 5) {
            start = 1;
            end = $scope.resultMap.totalPages;
        } else {
            start = $scope.searchMap.pageNo - 2;
            end = $scope.searchMap.pageNo + 2;
            if (start < 1) {
                start = 1
                end = start + 4;
            }
            if (end > $scope.resultMap.totalPages) {
                end = $scope.resultMap.totalPages;
                start = end - 4;
            }
        }
        $scope.pageLoder = [];
        for (var i = start; i <= end; i++) {
            $scope.pageLoder.push(i);
        }
    }

    $scope.buildPageNo = function (pageNo) {
        if ($scope.searchMap.pageNo - 1 < 0 || $scope.searchMap.pageNo + 1 > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.pageNo = 1;
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    }

    $scope.accpetEnter = function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        if ($scope.searchMap.keywords != null) {
            $scope.search();
        }
    }

    $scope.BrandIsKeywords = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    }


})