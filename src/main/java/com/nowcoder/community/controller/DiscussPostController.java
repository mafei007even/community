package com.nowcoder.community.controller;

import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.params.PostParam;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.model.vo.CommentVO;
import com.nowcoder.community.model.vo.ReplyVO;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private CommentService commentService;

    public DiscussPostController(DiscussPostService discussPostService, UserService userService, CommentService commentService) {
        this.discussPostService = discussPostService;
        this.userService = userService;
        this.commentService = commentService;
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

    /**
     * 帖子的直接评论是分页查出来
     * 评论的评论是直接全部查出来
     * @param postId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("{postId}")
    @ApiOperation(value = "根据帖子id查询帖子信息与发帖人信息，以及分页查询评论")
    public String findDiscussPostById(@PathVariable Integer postId, Model model, Page page){

        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null){
            return "site/error/404";
        }

        // 发帖者
        User postUser = userService.findUserById(post.getUserId());

        model.addAttribute("post", post);
        model.addAttribute("user", postUser);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/" + postId);
        page.setRows(post.getCommentCount());

        // 评论：帖子下的直接评论
        // 回复：评论的评论
        List<Comment> commentList = commentService.findCommentsByEntity(
                CommentEntityType.POST, post.getId(), page);

        //  评论VO列表
        List<CommentVO> commentVoList = new ArrayList<>();

        for (Comment comment : commentList) {
            // 评论Vo
            CommentVO commentVO = new CommentVO();
            commentVO.setComment(comment);
            // 这条评论的用户
            commentVO.setUser(userService.findUserById(comment.getUserId()));

            // 回复，就是评论的评论
            List<Comment> replyList = commentService.findCommentsByEntity(
                    CommentEntityType.COMMENT, comment.getId(), null);
            // 回复VO列表
            List<ReplyVO> replyVOList = new ArrayList<>();
            // 查询评论的评论 对应的用户，回复目标
            for (Comment reply : replyList) {
                ReplyVO replyVO = new ReplyVO();
                replyVO.setReply(reply);
                replyVO.setUser(userService.findUserById(reply.getUserId()));
                // 回复目标
                User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                replyVO.setTarget(target);

                replyVOList.add(replyVO);
            }

            commentVO.setReplys(replyVOList);
            commentVO.setReplyCount(replyList.size());

            commentVoList.add(commentVO);
        }

        model.addAttribute("comments", commentVoList);
        model.addAttribute("page", page);

        return "site/discuss-detail";
    }

}
