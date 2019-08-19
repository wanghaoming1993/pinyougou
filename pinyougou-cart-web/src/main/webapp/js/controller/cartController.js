app.controller("cartController", function ($scope, cartService) {
  //遍历集合的方法
  $scope.findCartList = function () {
    cartService.findCartList().success(function (response) {
      $scope.cartList = response;
      $scope.total = cartService.calu($scope.cartList);
    });
  };

  $scope.addCart = function (itemId, num) {
    cartService.addCart(itemId, num).success(function (response) {
      if (response.success) {
        $scope.findCartList();
      } else {
        alert(response.message);
      }
    });
  };

  $scope.findAddressList = function () {
    cartService.findAddressList().success(function (response) {
      $scope.addressList = response;
      //遍历判断默认选项是否被选中
      for (var i = 0; i < $scope.addressList.length; i++) {
        if ($scope.addressList[i].isDefault == "1") {
          $scope.address = $scope.addressList[i];
          break;
        }
      }
    });
  };

  //是否选中的方法，用一个变量来区别，相等就选中，不相等就没有选中
  $scope.selectAddress = function (address) {
    $scope.address = address;
  };
  $scope.isSelectedAddress = function (address) {
    if ($scope.address == address) {
      return true;
    } else {
      return false;
    }
  };

  //支付方式
  $scope.order = {
    paymentType: "1"
  };
  $scope.selectPay = function (paymenType) {
    $scope.order.paymentType = paymenType;
  };

  $scope.submitOrder = function () {
    $scope.order.receiverAreaName = $scope.address.address;
    $scope.order.receiverMobile = $scope.address.mobile;
    $scope.order.receiver = $scope.address.contact;
    cartService.submitOrder($scope.order).success(function (response) {
      if (response.success) {
        if ($scope.order.paymentType == '1') {
          location.href = "pay.html";
        } else {
          location.href = "paysuccess.html";
        }
      } else {
        alert(response.message)
      }
    })
  }




});