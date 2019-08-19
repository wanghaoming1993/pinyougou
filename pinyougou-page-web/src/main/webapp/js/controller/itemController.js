//控制层 
app.controller('itemController', function ($scope,$http) {

  //修改收藏前的按钮
  $scope.num = 1;
  $scope.addNum = function (x) {
    $scope.num += x;
    if ($scope.num < 1) {
      $scope.num = 1;
    }
  }
  //定义用户选择的规格
  $scope.specificationItems = {};

  $scope.selectSpecification = function (key, value) {
    $scope.specificationItems[key] = value;
    searchSku();
  }

  $scope.isSelected = function (key, value) {
    if ($scope.specificationItems[key] == value) {
      return true;
    } else {
      return false;
    }
  }

  //加载默认的sku
  $scope.sku = {};
  $scope.loadSku = function () {
    $scope.sku = skuList[1];
    //深加载
    $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec))
  }

  //对比两个元素的值是否全部一样，一样的话，就显示sku的值，不是完全相等就不显示
  matchObj = function (map1, map2) {
    //防止集合1的元素比集合2的元素多
    for( var k in map1){
      if(map1[k]!=map2[k]){
        return false;
      }
    }

    //防止集合2中的元素比集合1的多
    for(var k in map2){
      if(map2[k]!=map1[k]){
        return false;
      }
    }
    return true;
  }

  searchSku=function(){
   for (var i = 0; i < skuList.length; i++) {
    if (matchObj($scope.specificationItems,skuList[i].spec)) {
      $scope.sku=skuList[i];
      return;
    }else{
      $scope.sku={"id":0,"title":"---","price":"0"};
    }
   }
  }

  $scope.load=function(id){
   $http.get("http://localhost:9087/cart/addCart.do?itemId="+id+"&&num="+$scope.num,{'withCredentials':true}).success(function(response){
	   if(response.success){
		   location.href="http://localhost:9087/cart.html";
	   }else{
		   alert(response.message)
	   }
   })
  }

});