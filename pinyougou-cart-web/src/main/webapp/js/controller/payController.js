app.controller('payController', function ($scope, payService, $location) {
  $scope.createNative = function () {
    payService.createNative().success(function (response) {
      $scope.resultMap = response;
      $scope.resultMap.totalFee = (response.total_fee / 100);

      var qr = new QRious({
        element: document.getElementById('qrious'),
        size: 250,
        value: response.code_url,
        level: "H"
      })
      //查询是否支付成功
      quertStatu($scope.resultMap.out_trade_no);

    })
  }
  //支付是否完成
  quertStatu = function (out_trade_no) {
    payService.quertStatu(out_trade_no).success(function (response) {

      if (response.success) {
        location.href = "paysuccess.html#?totalFee=" + $scope.resultMap.totalFee;
      } else {
        if (response.message == '支付超时') {
          $scope.createNative();
        } else {
          alert(response.message);
        }
      }
    })
  }



  //获取值
  $scope.GetFee = function () {
    return $location.search()['totalFee'];
  }

})