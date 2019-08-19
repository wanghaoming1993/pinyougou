app.controller("baseController", function ($scope) {

    //分页引入
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };


    //刷新页面
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage)
    }


    $scope.selectIds = [];
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            //获取Id所在索引
            var idx = $scope.selectIds.indexof(id);
            $scope.selectIds.splice(id);
        }
    }

    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ",";
            }
            value += json[i][key]
        }
        return value;
    }

    $scope.searchObjectByKey = function (list, key, keyValue) {

        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }



})