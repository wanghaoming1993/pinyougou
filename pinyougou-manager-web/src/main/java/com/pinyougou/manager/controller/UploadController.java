package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

@RestController
public class UploadController {
	
	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	
	@RequestMapping("upload")
	public Result upload(MultipartFile file)  {
		//获取文件的全名
		String filename = file.getOriginalFilename();
		//获取文件的扩展名
		String name = filename.substring(filename.lastIndexOf(".")+1);
		
		//创建一个FastDFSClient客户端
		try {
			FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
			String uploadFile = fastDFSClient.uploadFile(file.getBytes(), name);
			String url=FILE_SERVER_URL+uploadFile;
			return new Result(true,url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false,"上传失败");
		}
			
	}
}
