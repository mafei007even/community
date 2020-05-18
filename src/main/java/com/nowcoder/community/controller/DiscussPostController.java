package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.*;
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
import com.nowcoder.community.utils.RedisKeyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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

@Slf4j
@Controller
@RequestMapping("discuss")
@Api("帖子相关接口")
public class DiscussPostController {

    private final DiscussPostService discussPostService;
    private final UserService userService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final EventProducer eventProducer;
    private final RedisTemplate redisTemplate;

    public DiscussPostController(DiscussPostService discussPostService, UserService userService, CommentService commentService, LikeService likeService, EventProducer eventProducer, RedisTemplate redisTemplate) {
        this.discussPostService = discussPostService;
        this.userService = userService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
    }

    @LoginRequired
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

        // 新增的帖子给个基础权重，加入缓存中等定时任务来计算权重
        String redisKey = RedisKeyUtils.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        return BaseResponse.ok("发布成功");
    }

    /**
     * 帖子的直接评论是分页查出来
     * 评论的评论是直接全部查出来
     *
     * 2020年5月16日
     * 新增允许管理员访问被删除的帖子，并可以进行恢复
     *
     * @param postId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("{postId}")
    @ApiOperation(value = "根据帖子id查询帖子信息与发帖人信息，以及分页查询评论")
    public String findDiscussPostById(@PathVariable Integer postId, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostByIdAllowBlock(postId);
        if (post == null){
            return "error/404";
        }

        UserInfo userInfo = UserHolder.get();
        if (post.getStatus() == DiscussPostStatus.BLOCK) {
            // 帖子被删除，且当前用户没有登陆
            if (userInfo == null) {
                return "error/404";
            }
            // 帖子被删除，且当前用户不是管理员
            if (userInfo.getType() != UserType.ADMIN) {
                return "error/404";
            }
        }

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

    /**
     * 置顶/取消置顶，只允许系统版主操作
     *
     * @param postId
     * @return
     */
    @LoginRequired
    @PostMapping("top")
    @ResponseBody
    public BaseResponse setTop(@RequestParam Integer postId, @RequestParam DiscussPostType type){
        discussPostService.updateType(postId, type);

        // 更改后触发发帖事件，发帖事件没有 entityUserId 要通知的用户
        Event event = Event.builder()
                .topic(Topic.Publish)
                .userId(UserHolder.get().getId())
                .entityType(CommentEntityType.POST)
                .entityId(postId)
                .build();
        eventProducer.fireEvent(event);

        return BaseResponse.ok(null);
    }

    /**
     * 加精/取消加精，只允许系统版主操作
     *
     * @param postId
     * @return
     */
    @LoginRequired
    @PostMapping("wonderful")
    @ResponseBody
    public BaseResponse setWonderful(@RequestParam Integer postId, @RequestParam DiscussPostStatus status){
        // 不允许删除
        if (status == DiscussPostStatus.BLOCK) {
            return new BaseResponse(400, "error", null);
        }
        discussPostService.updateStatus(postId, status);

        // 更改后触发发帖事件，发帖事件没有 entityUserId 要通知的用户
        Event event = Event.builder()
                .topic(Topic.Publish)
                .userId(UserHolder.get().getId())
                .entityType(CommentEntityType.POST)
                .entityId(postId)
                .build();
        eventProducer.fireEvent(event);

        // 加精后重新计算权重
        String redisKey = RedisKeyUtils.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, postId);

        return BaseResponse.ok(null);
    }

    /**
     * 删除，只允许系统管理员操作
     * 	如果帖子是当前用户发的，那么允许自己删除自己发的帖子
     *
     * @param postId
     * @return
     */
    @LoginRequired
    @PostMapping("delete")
    @ResponseBody
    public BaseResponse setDelete(@RequestParam Integer postId){
        UserInfo userInfo = UserHolder.get();
        Integer userId = userInfo.getId();

        User user = userService.findUserById(userId);
        // 当前用户是管理员直接操作
        if (user.getType() == UserType.ADMIN) {
            discussPostService.updateStatus(postId, DiscussPostStatus.BLOCK);
        } else {
            DiscussPost post = discussPostService.findDiscussPostById(postId);
            // 帖子不存在 或 帖子已经删除 或 不是当前用户发的帖子，不允许删除
            if (post == null || post.getStatus() == DiscussPostStatus.BLOCK || !post.getUserId().equals(userId)) {
                log.warn(String.format("用户尝试删除不存在 或 已经删除 或 非自己的帖子！ userId: %s, postId: %s", userId, postId));
                return new BaseResponse(400, "error", null);
            }
            discussPostService.updateStatus(postId, DiscussPostStatus.BLOCK);
        }

        // 触发删帖事件
        Event event = Event.builder()
                .topic(Topic.Delete)
                .userId(userId)
                .entityType(CommentEntityType.POST)
                .entityId(postId)
                .build();
        eventProducer.fireEvent(event);

        return BaseResponse.ok(null);
    }

    @PostMapping("restore")
    @ResponseBody
    public BaseResponse restore(@RequestParam Integer postId) {
        discussPostService.updateStatus(postId, DiscussPostStatus.NORMAL);

        // 更改后触发发帖事件，发帖事件没有 entityUserId 要通知的用户
        Event event = Event.builder()
                .topic(Topic.Publish)
                .userId(UserHolder.get().getId())
                .entityType(CommentEntityType.POST)
                .entityId(postId)
                .build();
        eventProducer.fireEvent(event);

        return BaseResponse.ok(null);
    }

}
