package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.LikeStatus;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.model.event.Event;
import com.nowcoder.community.model.params.PostParam;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.model.vo.CommentVO;
import com.nowcoder.community.model.vo.ReplyVO;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/5 22:59
 */

@Controller
@RequestMapping("discuss")
@Api("帖子相关接口")
public class DiscussPostController {

    private final DiscussPostService discussPostService;
    private final UserService userService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final EventProducer eventProducer;

    public DiscussPostController(DiscussPostService discussPostService, UserService userService, CommentService commentService, LikeService likeService, EventProducer eventProducer) {
        this.discussPostService = discussPostService;
        this.userService = userService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.eventProducer = eventProducer;
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

        // 触发发帖事件，发帖事件没有 entityUserId 要通知的用户
        Event event = Event.builder()
                .topic(Topic.Publish)
                .userId(userInfo.getId())
                .entityType(CommentEntityType.POST)
                .entityId(post.getId())
                .build();
        eventProducer.fireEvent(event);

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
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null){
            return "error/404";
        }
        UserInfo userInfo = UserHolder.get();

        // 帖子点赞数
        long likeCount = likeService.findEntityLikeCount(CommentEntityType.POST, postId);
        // 点赞状态，就是当前用户是否对这个帖子点过赞，要考虑用户是否登陆
        LikeStatus likeStatus = userInfo == null ?
                LikeStatus.NONE : likeService.findEntityLikeStatus(userInfo.getId(),
                CommentEntityType.POST, postId);

        // 发帖者
        User postUser = userService.findUserById(post.getUserId());

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
            // 这条评论的点赞数
            long commentLikeCount = likeService.findEntityLikeCount(CommentEntityType.COMMENT, comment.getId());
            commentVO.setLikeCount(commentLikeCount);
            // 这条评论的点赞状态，没有登陆那就是没点赞
            LikeStatus commentLikeStatus = userInfo == null ?
                    LikeStatus.NONE : likeService.findEntityLikeStatus(userInfo.getId(),
                    CommentEntityType.COMMENT, comment.getId());
            commentVO.setLikeStatus(commentLikeStatus);


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

                // 这条回复评论的点赞数
                long replyLikeCount = likeService.findEntityLikeCount(CommentEntityType.COMMENT, reply.getId());
                replyVO.setLikeCount(replyLikeCount);
                // 这条回复评论的点赞状态，没有登陆那就是没点赞
                LikeStatus replyLikeStatus = userInfo == null ?
                        LikeStatus.NONE : likeService.findEntityLikeStatus(userInfo.getId(),
                        CommentEntityType.COMMENT, reply.getId());
                replyVO.setLikeStatus(replyLikeStatus);


                replyVOList.add(replyVO);
            }

            commentVO.setReplys(replyVOList);
            commentVO.setReplyCount(replyList.size());

            commentVoList.add(commentVO);
        }


        model.addAttribute("post", post);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("user", postUser);
        model.addAttribute("comments", commentVoList);
        model.addAttribute("page", page);

        return "site/discuss-detail";
    }

}
