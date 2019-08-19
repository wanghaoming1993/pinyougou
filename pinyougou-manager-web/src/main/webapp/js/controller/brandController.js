app.controller("brand", function ($scope, $controller, brandService) {
    //查询所有

    //继承
    $controller("baseController", { $scope: $scope })

    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        })
    };



    //分页查询	
    $scope.findPage = function (page, pageSize) {
        brandService.findPage(page, pageSize).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        });
    }
    //存储
    $scope.save = function () {
        brandService.save($scope.entity).success(
            function (response) {
                if (response.flag) {
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            })
    }

    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    }

    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(function (response) {
            if (response.flag) {
                $scope.reloadList();
            } else {
                alter(response.message);
            }
        })

    }
    //搜索的方法
    $scope.tbBrand = {};
    $scope.search = function (page, pageSize) {
        brandService.search(page, pageSize, $scope.tbBrand).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;
        })
    }

}); 