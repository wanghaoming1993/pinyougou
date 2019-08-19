app.service("brandService", function ($http) {

    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    }

    this.findPage = function (page, pageSize) {
        return $http.get(
            "../brand/findPage.do?page=" + page + "&&pageSize="
            + pageSize);
    }

    this.save = function (entity) {
        return $http.post("../brand/save.do", entity)
    }

    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id=" + id);
    }

    this.dele = function (selectIds) {
        return $http.get("../brand/dele.do?ids=" + selectIds);
    }
    this.search = function (page, pageSize, tbBrand) {
        return $http.post("../brand/findPage.do?page=" + page + "&&pageSize=" + pageSize, tbBrand);
    }

    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do")
    }

})