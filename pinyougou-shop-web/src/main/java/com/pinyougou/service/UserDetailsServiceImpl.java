package com.pinyougou.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;


public class UserDetailsServiceImpl implements UserDetailsService {

	private SellerService sellerService;

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		TbSeller seller = sellerService.findByUsername(username);
		System.out.println("方法执行了");	
		if (seller != null) {
			if (seller.getStatus().equals("1")) {
				String password = seller.getPassword();
				List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				return new User(username, password, authorities);
			} else {

				return null;
			}
		} else {
			return null;
		}
	}
}
