package com.ylm.lmpuffpicturebankend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.ylm.lmpuffpicturebankend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class LmpuffPictureBankendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmpuffPictureBankendApplication.class, args);
    }

}
