//控制层 
app.controller('userController', function ($scope, $controller, userService) {

	$controller('baseController', {
		$scope: $scope
	}); // 继承
	//添加的方法
	$scope.reg = function () {
		if ($scope.password != $scope.entity.password) {
			alert("密码不一致清重新输入");
			$scope.password = '';
			$scope.entity.password = "";
			return;
		}

		userService.add($scope.entity,$scope.checkcode).success(function (response) {
			if (response.success) {
				alert("注册成功");
			} else {
				alert(response.message);
			}
		})
	}

	$scope.sendCode = function () {
		userService.sendCode($scope.entity.phone).success(function (response) {

			if (response.success) {} else {
				alert(response.message);
			}

		})
	}



});