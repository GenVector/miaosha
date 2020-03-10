package com.imooc.miaosha;

import com.imooc.miaosha.config.SwaggerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SwaggerConfiguration.class})
public class MainApplication extends SpringBootServletInitializer{

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);
    }
////以war包形式
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//		return builder.sources(MainApplication.class);
//	}
//    
 //redis ip地址
    //数据库建表
    
}
