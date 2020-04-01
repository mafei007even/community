package com.nowcoder.community;

import cn.hutool.extra.mail.MailUtil;
import com.nowcoder.community.utils.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author mafei007
 * @date 2020/3/31 23:03
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;
    @Autowired
    private MailProperties mailProperties;


    @Autowired
    private TemplateEngine templateEngine;


    @Test
    public void test(){
        mailClient.sendMail(mailProperties.getUsername(), "TEST", "Welcome");

    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "zhangsan");

        // 模板引擎渲染后的 html 文本
        String content = templateEngine.process("/mail/demo", context);

        mailClient.sendMail(mailProperties.getUsername(), "测试html邮件", content);
    }


    @Test
    public void testHtmlMailByHutool(){
        Context context = new Context();
        context.setVariable("username", "zhangsan");

        // 模板引擎渲染后的 html 文本
        String content = templateEngine.process("/mail/demo", context);

        MailUtil.send(mailProperties.getUsername(), "htmlByHutool", content, true);
    }



}
