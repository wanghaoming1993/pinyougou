app.service('payService', function ($http) {


  this.createNative = function () {
    return $http.get('./pay/createNative.do');
  }

  this.quertStatu = function (out_trade_no) {
    return $http.get('./pay/quertForPayStatu.do?out_trade_no=' + out_trade_no);
  }

})