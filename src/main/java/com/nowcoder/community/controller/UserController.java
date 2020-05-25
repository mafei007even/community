package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CodecUtils;
import com.nowcoder.community.utils.JsonUtils;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
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
@Validated
public class UserController {

    private final UserService userService;
    private final LikeService likeService;
    private final FollowService followService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif");

    public UserController(UserService userService, LikeService likeService, FollowService followService) {
        this.userService = userService;
        this.likeService = likeService;
        this.followService = followService;
    }

    /**
     * 使用七牛云，设置头像页面里利用这些信息构造表单
     * 将表单信息提交给七牛云
     * @param model
     * @return
     */
    @LoginRequired
    @GetMapping("setting")
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CodecUtils.generateUUID();
        // 设置响应信息，就是上传成功了给我们响应的 json 数据是什么
        StringMap policy = new StringMap();
        policy.put("returnBody", JsonUtils.objectToJson(BaseResponse.ok("success")));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        // expires 是生成的这个 token 的有效时间
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "site/setting";
    }

    /**
     * 更新保存在七牛云的用户头像 url
     * @param fileName
     * @return
     */
    @PostMapping("header/url")
    @ResponseBody
    public BaseResponse updateHeaderUrl(@NotBlank String fileName, @CookieValue("ticket") String ticket) {
        String headerUrl = headerBucketUrl + "/" + fileName;

        // 更新用户的头像路径
        UserInfo userInfo = UserHolder.get();
        userService.updateHeader(userInfo.getId(), headerUrl);

        // 更新 redis 缓存中的用户信息
        userInfo.setHeaderUrl(headerUrl);
        userService.saveUserInfo(userInfo, ticket);
        log.info("用户userId: " + userInfo.getId() + " 更新了头像: " + headerUrl);
        return BaseResponse.ok("success");
    }


    /**
     * 2020年5月19日 Deprecated
     * 改用七牛云存储图片
     * @param file
     * @param model
     * @param ticket
     * @return
     */
    @Deprecated
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
        userService.saveUserInfo(userInfo, ticket);

        return "redirect:/index";
    }

    /**
     * 2020年5月19日 Deprecated
     * 改用七牛云存储图片
     * @param filename
     * @param response
     */
    @Deprecated
    @GetMapping("header/{filename}")
    public void getHeaderUrl(@PathVariable String filename, HttpServletResponse response) {
        try {
            Path path = Paths.get(uploadPath, filename);
            if (Files.exists(path)) {
                response.setContentType("image/jpeg");
                ServletOutputStream outputStream = response.getOutputStream();
                Files.copy(path, outputStream);
            } else {
                response.setStatus(404);
            }
        } catch (IOException e) {
            log.error("读取本地头像失败：" + e.getMessage(), e);
        }
    }

    /**
     * 查看用户的信息
     * 所有人无论有没有登陆都可以访问
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("profile/{userId}")
    public String getProfilePage(@PathVariable Integer userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            // throw new NotFoundException("用户不存在");
            return "error/404";
        }
        // 被赞数量
        int likeCount = likeService.findUserLikeCount(userId);

        // 这个用户关注别人的数量
        long followeeCount = followService.findFolloweeCount(userId, CommentEntityType.USER);

        // 粉丝数量
        long followerCount = followService.findFollowerCount(CommentEntityType.USER, userId);

        // 当前用户对这个用户有没有关注，没有登陆的话就是没关注
        boolean hasFollowed = false;
        UserInfo userInfo = UserHolder.get();
        if (userInfo != null){
            hasFollowed = followService.hasFollowed(userInfo.getId(), CommentEntityType.USER, userId);
        }

        // 用户
        model.addAttribute("user", user);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("followeeCount", followeeCount);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }


}
