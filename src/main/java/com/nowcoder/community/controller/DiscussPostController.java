package com.nowcoder.community.controller;

import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.params.PostParam;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.DiscussPostService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * @author mafei007
 * @date 2020/4/5 22:59
 */

@Controller
@RequestMapping("discuss")
public class DiscussPostController {

    private DiscussPostService discussPostService;

    public DiscussPostController(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @PostMapping
    @ResponseBody
    public BaseResponse<String> addDiscussPost(@Valid PostParam postParam) {

        UserInfo userInfo = UserHolder.get();
        if (userInfo == null){
            return new BaseResponse<>(HttpStatus.FORBIDDEN.value(), "您还没有登陆！", null);
        }

        DiscussPost post = postParam.convertTo();
        discussPostService.addDiscussPost(post);

        return BaseResponse.ok("发布成功");
    }


}
