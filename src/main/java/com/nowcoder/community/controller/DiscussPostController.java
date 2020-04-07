package com.nowcoder.community.controller;

import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.params.PostParam;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author mafei007
 * @date 2020/4/5 22:59
 */

@Controller
@RequestMapping("discuss")
@Api("帖子相关接口")
public class DiscussPostController {

    private DiscussPostService discussPostService;
    private UserService userService;

    public DiscussPostController(DiscussPostService discussPostService, UserService userService) {
        this.discussPostService = discussPostService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseBody
    @ApiOperation("新增帖子")
    public BaseResponse<String> addDiscussPost(@Valid PostParam postParam) {

        UserInfo userInfo = UserHolder.get();
        if (userInfo == null){
            return new BaseResponse<>(HttpStatus.FORBIDDEN.value(), "您还没有登陆！", null);
        }

        DiscussPost post = postParam.convertTo();
        discussPostService.addDiscussPost(post);

        return BaseResponse.ok("发布成功");
    }

    @GetMapping("{postId}")
    @ApiOperation(value = "根据帖子id查询帖子信息与发帖人信息")
    public String findDiscussPostById(@PathVariable Integer postId, Model model){

        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null){
            return "site/error/404";
        }

        User user = userService.findUserById(post.getUserId());

        model.addAttribute("post", post);
        model.addAttribute("user", user);
        return "site/discuss-detail";

    }

}
