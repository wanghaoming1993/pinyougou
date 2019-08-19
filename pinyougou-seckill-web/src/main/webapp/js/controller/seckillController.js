app.controller('seckillController', function ($scope, seckillService, $location, $interval) {
  //查询秒杀商品列表
  $scope.findSeckillGoods = function () {
    seckillService.findSeckillGoods().success(function (response) {
      $scope.seckillGoodsList = response;

    })
  }
  //跳转页面
  $scope.loadDetail = function (goodsId) {
    location.href = 'seckill-item.html#?goodsId=' + goodsId;
  }
  //查询单个goods
  $scope.findseckillGoodsByRedis = function () {
    var goodId = $location.search()['goodsId'];
    seckillService.findSeckillGoodByRedis(goodId).success(function (response) {
      $scope.seckillGood = response
      $scope.time = Math.floor((new Date(response.endTime).getTime() - new Date().getTime()) / 1000);
      
    })
  }
  //计时器$interval
  time = $interval(function () {
    $scope.time = $scope.time - 1
    $scope.timeStr = coverString($scope.time);
    if ($scope.time <= 0) {
      $interval.cancel(time)
    }
  }, 1000)

  //计时器转化为字符串
  coverString = function (time) {
    var day = Math.floor(time / (60 * 60 * 24));
    var hour = Math.floor((time - day * (60 * 60 * 24)) / (60 * 60))
    var min = Math.floor((time - day * (60 * 60 * 24) - hour * (60 * 60)) / 60);
    var sec = Math.floor(time - day * (60 * 60 * 24) - hour * (60 * 60) - min * 60)
    if (day > 0) {
      day = day + "天"
    }
    if (hour <= 10) {
      hour = "0" + hour
    }
    if (min <= 10) {
      min = "0" + min
    }
    if (sec <= 10) {
      sec = "0" + sec
    }
    return day + "  " + hour + ":" + min + ":" + sec;
  }
  //提交订单
  $scope.submitOrder=function(orderId){
    seckillService.submitOrder(orderId).success(function (response) {
      if (response.success) {
        location.href='pay.html';
        alert("下单成功，请五分钟之内支付")
      }else{
       alert(response.message);
      }
    })
  }





})