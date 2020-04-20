package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/4 20:59
 */

@Controller
@RequestMapping("user")
@Slf4j
public class UserController {

    private UserService userService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif");

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @LoginRequired
    @GetMapping("setting")
    public String getSettingPage() {
        return "site/setting";
    }


    @LoginRequired
    @PostMapping("upload")
    public String uploadHeader(MultipartFile file, Model model, @CookieValue("ticket") String ticket) {
        if (file == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "site/setting";
        }

        String filename = file.getOriginalFilename();
        // 校验文件类型
        String contentType = file.getContentType();
        if (!CONTENT_TYPES.contains(contentType)) {
            log.info("文件类型不合法，不是图片：{}", filename);
            model.addAttribute("error", "文件类型不合法，不是图片");
            return "site/setting";
        }

        try {
            // 校验文件内容是不是图片
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                log.info("文件内容不合法，不是图片！：{}", filename);
                model.addAttribute("error", "文件内容不合法，不是图片！");
                return "site/setting";
            }

            // 保存到文件服务器
            // 取扩展名
            String ext = StringUtils.substringAfterLast(filename, ".");
            filename = CodecUtils.generateUUID() + "." + ext;

            File dest = new File(uploadPath + "/" + filename);
            // 存储文件
            file.transferTo(dest);

        } catch (IOException e) {
            log.error("上传文件失败：" + filename, e);
            throw new RuntimeException("上传文件失败，请稍后再试！");
        }

        // 更新用户的头像路径
        // http://localhost:8080/community/user/header/xxx.png
        UserInfo userInfo = UserHolder.get();
        String headerUrl = domain + contextPath + "/user/header/" + filename;

        userService.updateHeader(userInfo.getId(), headerUrl);

        // 更新 redis 缓存中的用户信息
        userInfo.setHeaderUrl(headerUrl);
        userService.saveUserInfo(userInfo, ticket, null);


        return "redirect:/index";
    }

    @GetMapping("header/{filename}")
    public void getHeaderUrl(@PathVariable String filename, HttpServletResponse response) {

        try {

            Path path = Paths.get(uploadPath, filename);

            if (Files.exists(path)) {
                response.setContentType("image/jpeg");
                ServletOutputStream outputStream = response.getOutputStream();

                Files.copy(path, outputStream);
            }
        } catch (IOException e) {
            log.error("读取本地头像失败：" + e.getMessage(), e);
        }

    }


}
