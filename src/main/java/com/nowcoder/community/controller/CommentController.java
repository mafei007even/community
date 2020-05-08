package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.model.event.Event;
import com.nowcoder.community.model.params.CommentParam;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * @author mafei007
 * @date 2020/4/11 20:40
 */

@Controller
@RequestMapping("comment")
public class CommentController {

	private final CommentService commentService;
	private final EventProducer eventProducer;
	private final DiscussPostService discussPostService;

	public CommentController(CommentService commentService, EventProducer eventProducer, DiscussPostService discussPostService) {
		this.commentService = commentService;
		this.eventProducer = eventProducer;
		this.discussPostService = discussPostService;
	}

	@LoginRequired
	@PostMapping("add/{discussPostId}")
	@ApiOperation("添加评论")
	public String addComment(@PathVariable Integer discussPostId, @Valid CommentParam commentParam) {
		Comment comment = commentParam.convertTo();
		commentService.addComment(comment);

		/**
		 触发事件对应的用户，就是要通知的用户
		 如果这个评论是直接给帖子进行评论，那要通知的就是发帖者
		 如果这个评论是给评论进行直接评论，非在评论下进行回复，那通知的就是原评论者
		 如果这个评论是给评论进行评论，且评论为回复，那通知的就是评论下的被回复者
		 */
		Integer entityUserId = null;
		switch (comment.getEntityType()) {
			case POST:
				DiscussPost targetPost = discussPostService.findDiscussPostById(comment.getEntityId());
				entityUserId = targetPost.getUserId();
				break;
			case COMMENT:
				// targetId 就是 userId，不为0说明是回复操作
				if (comment.getTargetId() != 0) {
					entityUserId = comment.getTargetId();
					break;
				}
				Comment targetComment = commentService.findCommentById(comment.getEntityId());
				entityUserId = targetComment.getUserId();
				break;
			default:
		}

		// 触发评论事件
		Event event = Event.builder()
				.topic(Topic.Comment)
				.userId(UserHolder.get().getId())
				.entityType(comment.getEntityType())
				.entityId(comment.getEntityId())
				.entityUserId(entityUserId)
				.build()
				// postId可以让被评论者点开通知时可以链接到帖子详情页面
				.setData("postId", discussPostId);

		// 发送消息
		eventProducer.fireEvent(event);

		return "redirect:/discuss/" + discussPostId;
	}

}
