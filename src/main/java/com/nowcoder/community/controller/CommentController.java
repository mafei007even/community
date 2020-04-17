package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.params.CommentParam;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/4/11 20:40
 */

@Controller
@RequestMapping("comment")
public class CommentController {

    private CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    @LoginRequired
    @PostMapping("add/{discussPostId}")
    @ApiOperation("添加评论")
    public String addComment(@PathVariable Integer discussPostId, @Valid CommentParam commentParam){
        Comment comment = commentParam.convertTo();
        commentService.addComment(comment);
        return "redirect:/discuss/" + discussPostId;
    }

}
