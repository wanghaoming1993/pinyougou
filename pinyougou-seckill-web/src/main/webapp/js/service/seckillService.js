app.service('seckillService',function($http){
    this.findSeckillGoods=function(){
      return $http.get('./seckillGoods/findSekillGoods.do');
    }
    this.findSeckillGoodByRedis=function(goodId){
      return $http.get('./seckillGoods/findSeckillGoodsByRedis.do?goodId=' + goodId)
    }
    this.submitOrder=function(OrderId){
      return $http.get('./seckillGoods/sumbitOrder.do?orderId=' + OrderId);
    }

})