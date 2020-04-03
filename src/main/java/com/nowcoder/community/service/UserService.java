package com.nowcoder.community.service;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.constants.ActivationStatus;
import com.nowcoder.community.constants.ExpiredTime;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.entity.pojo.UserInfo;
import com.nowcoder.community.utils.CodecUtils;
import com.nowcoder.community.utils.JsonUtils;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tk.mybatis.mapper.entity.Example;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author mafei007
 * @date 2020/3/30 19:11
 */

@Service
public class UserService {

    private UserMapper userMapper;
    private MailClient mailClient;
    private TemplateEngine templateEngine;
    private StringRedisTemplate redisTemplate;
    private Producer kapchaProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public UserService(UserMapper userMapper, MailClient mailClient, TemplateEngine templateEngine, StringRedisTemplate redisTemplate, Producer kapchaProducer) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
        this.redisTemplate = redisTemplate;
        this.kapchaProducer = kapchaProducer;
    }


    public User findUserById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }


    public Map<String, Object> register(User user) {

        HashMap<String, Object> map = new HashMap<>();


        // 验证账号是否存在
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", user.getUsername());

        int count = userMapper.selectCountByExample(example);
        if (count > 0) {
            map.put("usernameMsg", "该账号已经存在!");
            return map;
        }

        // 验证邮箱
        Example example2 = new Example(User.class);
        Example.Criteria criteria2 = example2.createCriteria();
        criteria2.andEqualTo("email", user.getEmail());

        int count2 = userMapper.selectCountByExample(example2);
        if (count2 > 0) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册
        String salt = CodecUtils.generateUUID();
        String encryptedPw = CodecUtils.md5Hex(user.getPassword(), salt);

        user.setSalt(salt);
        user.setPassword(encryptedPw);
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(CodecUtils.generateUUID());

        String urlTemplate = "/head/%dt.png";
        String headerUrl = String.format(urlTemplate, new Random().nextInt(1000));
        user.setHeaderUrl(headerUrl);
        user.setCreateTime(new Date());

        // 插入后自增长 id 会回显
        userMapper.insertSelective(user);

        // 发送激活邮箱
        sendActivationEmail(user);

        return map;
    }

    private void sendActivationEmail(User user) {

        Context context = new Context();
        context.setVariable("username", user.getUsername());

        // http://localhost:8080/community/activation/{uid}/{code}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String content = templateEngine.process("/mail/activation", context);

        mailClient.sendMail(user.getEmail(), "激活账号", content);

    }


    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    public ActivationStatus activation(int userId, String code) {
        User user = userMapper.selectByPrimaryKey(userId);

        if (user == null) {
            return ActivationStatus.FAILURE;
        }

        // status 为 1 代表已经激活
        if (user.getStatus() == 1) {
            return ActivationStatus.REPEAT;
        }

        if (user.getActivationCode().equals(code)) {
            // 更新激活状态
            user.setStatus(1);
            userMapper.updateByPrimaryKeySelective(user);
            return ActivationStatus.SUCCESS;
        }

        return ActivationStatus.FAILURE;
    }


    public BufferedImage genCaptcha(String captchaId) {

        String code = kapchaProducer.createText();
        // 存入 redis
        redisTemplate.opsForValue().set("captcha_code_" + captchaId, code, 60, TimeUnit.SECONDS);

        BufferedImage image = kapchaProducer.createImage(code);

        return image;
    }


    public Map<String, Object> login(String username, String password, ExpiredTime expiredTime) {
        HashMap<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证
        User t = new User();
        t.setUsername(username);
        User user = userMapper.selectOne(t);

        if (user == null) {
            map.put("usernameMsg", "账号或密码错误！");
            map.put("passwordMsg", "账号或密码错误！");
            return map;
        }

        // 验证密码
        String encryptedPw = CodecUtils.md5Hex(password, user.getSalt());
        if (!StringUtils.equals(encryptedPw, user.getPassword())) {
            map.put("usernameMsg", "账号或密码错误！");
            map.put("passwordMsg", "账号或密码错误！");
            return map;
        }

        // 验证账号是否激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }

        // 生成登陆凭证，存到 redis，并要返回给客户端 cookie 的
        String uuid = CodecUtils.generateUUID();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setHeaderUrl(user.getHeaderUrl());

        String json = JsonUtils.objectToJson(userInfo);
        redisTemplate.opsForValue().set("user_" + uuid, json, expiredTime.getTimeout(), expiredTime.getTimeUnit());

        // 返回凭证
        map.put("ticket", uuid);

        return map;
    }

    public void logout(String ticket) {
        redisTemplate.delete("user_" + ticket);
    }

    public UserInfo findUserInfo(String ticket) {
        if (ticket == null) {
            return null;
        }
        String json = redisTemplate.opsForValue().get("user_" + ticket);
        return json == null ? null : JsonUtils.jsonToPojo(json, UserInfo.class);
    }

}
