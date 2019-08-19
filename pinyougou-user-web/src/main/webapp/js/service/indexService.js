app.service("indexService", function ($http) {


  this.login = function () {
    return $http.get("../user/login.do");
  }

})