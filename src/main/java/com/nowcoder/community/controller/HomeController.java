package com.nowcoder.community.controller;

import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.MailClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mafei007
 * @date 2020/3/30 19:17
 */

@Controller
public class HomeController {

    private DiscussPostService discussPostService;
    private UserService userService;
    private MailClient mailClient;
    private TemplateEngine templateEngine;


    public HomeController(DiscussPostService discussPostService, UserService userService, MailClient mailClient, TemplateEngine templateEngine) {
        this.discussPostService = discussPostService;
        this.userService = userService;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
    }


    @GetMapping("index")
    public String getIndexPage(Model model, Page page) {

        // 设置Page回显的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        model.addAttribute("page", page);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();

                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);

        return "index";
    }


    @GetMapping("error")
    public String getErrorPage(){
        return "error/500";
    }


}
