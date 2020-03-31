package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


/**
 * @author mafei007
 * @date 2020/3/28 22:52
 */

@SpringBootApplication
@MapperScan("com.nowcoder.community.dao")
public class CommunityApplication {


    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
