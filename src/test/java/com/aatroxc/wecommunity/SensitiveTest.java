package com.aatroxc.wecommunity;

import com.aatroxc.wecommunity.utils.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author mafei007
 * @date 2020/4/5 19:43
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;


    @Test
    public void test12(){
        String text = "这里可以#@赌#@博#@，可以!嫖#@娼~，可以吸~毒，哈~@哈哈";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);
    }


    @Test
    public void test1(){

    }

}
