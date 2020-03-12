package com.imooc.miaosha.config;

import com.imooc.miaosha.domain.User;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.StandardServletEnvironment;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Properties;


@PropertySource(value = {"file:/data/app.properties"}, encoding = "utf-8")
@Import({SwaggerConfiguration.class})
@ConfigurationProperties(prefix = "redis")
@Data
@Configuration
@Log4j
public class ImportOutConfig {

    @Value("${config.icon}")
    private String mode;
    @Autowired
    Environment environment;
    @Autowired
    Docket docket;

    @Value("#{1}")
    private int intTest;

    @Value("#{1.0005}")
    private double doubleTest;

    @Value("#{'shiny'}")
    private String string;

    @Value("#{systemProperties['PID']}")
    private String PID;

    @Value("#{user}")
    private User user;

    @Value("#{user.name}")
    private String userName;

    @Value("#{user.name + user.id}")
    private String userStr;

    @Value("#{T(java.lang.Math).random() * 2 / T(java.lang.Math).PI }")
    private double random;

    @Value("#{T(System).currentTimeMillis()}")
    private long time;

    @Value("#{user.id eq 12 }")
    private boolean ifTrue;

    @Value("#{user.id == 12 }")
    private boolean ifTrue2;

    private String host;
    private int port;
    private int timeout;//秒
    private String password;
    private int poolMaxTotal;
    private int poolMaxIdle;
    private int poolMaxWait;//秒
    private int test;

    @Bean
    public String string(User user) {
        ((StandardServletEnvironment) environment).getPropertySources().get("URL [file:/data/app.properties]").getProperty("redis.test");
        log.info("config" + mode);
        Properties properties = System.getProperties();
        return mode;
    }

}
