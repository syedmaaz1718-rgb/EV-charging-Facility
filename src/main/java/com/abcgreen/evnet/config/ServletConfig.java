package com.abcgreen.evnet.config;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ServletComponentScan("com.abcgreen.evnet.servlet")
public class ServletConfig {}
