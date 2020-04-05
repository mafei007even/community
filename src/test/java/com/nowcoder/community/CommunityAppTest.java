package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author mafei007
 * @date 2020/3/28 22:55
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommunityAppTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Test
    public void testApplicationContext(){
        System.out.println(applicationContext);
    }




    @Test
    public void test222(){


    }

}
