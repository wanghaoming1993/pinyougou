//控制层 
app.controller('goodsController', function ($scope, $location, $controller, goodsService, uploadService, itemCatService, typeTemplateService) {

	$controller('baseController', { $scope: $scope });//继承

	//读取列表数据绑定到表单中  
	$scope.findAll = function () {
		goodsService.findAll().success(
			function (response) {
				$scope.list = response;
			}
		);
	}

	$scope.status = ['未审核', '已审核', '审核通过', '关闭'];
	$scope.itemCatList = [];
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(function (response) {
			for (var i = 0; i < response.length; i++) {
				$scope.itemCatList[response[i].id] = response[i].name;
			}
		})
	}
	//分页
	$scope.findPage = function (page, rows) {
		goodsService.findPage(page, rows).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}

	//查询实体 
	$scope.findOne = function () {

		var id = $location.search()['id'];
		if (id == null) {
			return;
		}
		goodsService.findOne(id).success(
			function (response) {
				$scope.entity = response;
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages)
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				for (var i = 0; $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);
	}

	//保存 
	$scope.save = function () {
		var serviceObject;//服务层对象  				
		if ($scope.entity.goods.id != null) {//如果有ID
			serviceObject = goodsService.update($scope.entity); //修改  
		} else {
			serviceObject = goodsService.add($scope.entity);//增加 
		}
		serviceObject.success(
			function (response) {
				if (response.success) {
					//重新查询 
					alert(response.message);
					location.href = 'goods.html';
				} else {
					alert(response.message);
				}
			}
		);
	}


	//批量删除 
	$scope.dele = function () {
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
			function (response) {
				if (response.success) {
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}
			}
		);
	}

	$scope.searchEntity = {};//定义搜索对象 

	//搜索
	$scope.search = function (page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	}


	//保存 
	$scope.add = function () {
		$scope.entity.goodsDesc.introduction = editor.html()
		goodsService.add($scope.entity).success(

			function (response) {
				if (response.success) {
					//重新查询 
					alert(response.message);
					$scope.entity = {};
					editor.html('')
				} else {
					alert(response.message);
				}
			}
		);
	}
	//上传图片
	$scope.uploadFile = function () {

		uploadService.uploadFile().success(function (response) {
			if (response.success) {
				$scope.image_entity.url = response.message;
			} else {
				alert(response.message);
			}
		})
	}

	$scope.entity = { goodsDesc: { itemImages: [], specificationItems: [] } };
	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	$scope.remove_image_entity = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}

	$scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List = response;
		})
	}

	$scope.$watch('entity.goods.category1Id', function (newValue, OldValue) {
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat2List = response;
		})

	})


	$scope.$watch('entity.goods.category2Id', function (newValue, OldValue) {
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat3List = response;
		})

	})

	$scope.$watch('entity.goods.category3Id', function (newValue, OldValue) {
		itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId = response.typeId;

		})

	})

	$scope.$watch('entity.goods.typeTemplateId', function (newValue, OldValue) {
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate = response;
			$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
			if ($location.search()['id'] == null) {
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
			}
		})
		typeTemplateService.findSpecList(newValue).success(function (response) {
			$scope.specList = response;
		})

	})

	// specificationItems增改 
	$scope.updateSpecAttribute = function ($event, key, keyValue) {
		var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", key);
		if (obj != null) {
			if ($event.target.checked) {
				obj.attributeValue.push(keyValue);
			} else {
				obj.attributeValue.splice(obj.attributeValue.indexOf(keyValue), 1);

				if (obj.attributeValue.length == 0) {

					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj), 1)
				}
			}
		} else {
			$scope.entity.goodsDesc.specificationItems.push({ "attributeName": key, "attributeValue": [keyValue] })
		}

	}

	//itemList的完成 根据上面得到的goodsDesc.specificationItems完成
	$scope.createItemList = function () {
		$scope.entity.itemList = [{ "spec": {}, "price": 0, "num": 9999, "status": 0, "isDefault": 0 }];
		var items = $scope.entity.goodsDesc.specificationItems;
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
		}
	}

	addColumn = function (list, name, value) {
		var newList = [];
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			for (var j = 0; j < value.length; j++) {
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[name] = value[j]//.和【】的区别，.是直接内部属性，【】是引用
				newList.push(newRow);
			}
		}
		return newList;
	}



	$scope.checkAttributeValue = function (name, value) {
		var items = $scope.entity.goodsDesc.specificationItems;
		var obj = $scope.searchObjectByKey(items, "attributeName", name);
		if (obj != null) {
			if (obj.attributeValue.indexOf(value) >= 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	$scope.updateStatus = function (status) {
		goodsService.updateStatus($scope.selectIds, status).success(function (response) {
			if (response.success) {
				$scope.reloadList();
				$scope.selectIds = [];
			} else {
				alert(response.message);
			}
		})
	}

});	
