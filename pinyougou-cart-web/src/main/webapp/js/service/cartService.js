app.service("cartService", function($http) {
  this.findCartList = function() {
    return $http.get("./cart/findCartList.do");
  };

  this.addCart = function(itemId, num) {
    return $http.get("./cart/addCart.do?itemId=" + itemId + "&&num=" + num);
  };

  //计算的方法
  this.calu = function(cartList) {
    var total = {
      totalNum: 0,
      totalMoney: 0.0
    };
    for (var i = 0; i < cartList.length; i++) {
      var cart = cartList[i];
      for (var j = 0; j < cart.orderItems.length; j++) {
        total.totalNum += cart.orderItems[j].num;
        total.totalMoney += cart.orderItems[j].totalFee;
      }
    }
    return total;
  };
  this.findAddressList=function(){
    return $http.get("./address/findAddressList.do");
  }


  this.submitOrder=function(order){
    return $http.post('./order/add.do',order);
  }





});
