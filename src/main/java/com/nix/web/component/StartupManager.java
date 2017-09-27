package com.nix.web.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartupManager implements InitializingBean {
	@Autowired
	private ServletContext application;

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
