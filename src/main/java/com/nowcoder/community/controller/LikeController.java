package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.dto.LikeDTO;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.LikeStatus;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.model.event.Event;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.LikeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author mafei007
 * @date 2020/4/20 21:06
 */

@Controller
public class LikeController {

	private final LikeService likeService;
	private final EventProducer eventProducer;

	public LikeController(LikeService likeService, EventProducer eventProducer) {
		this.likeService = likeService;
		this.eventProducer = eventProducer;
	}

	/**
	 * 点赞，点赞后还要发送消息，异步通知被赞者
	 *
	 * @param entityType   点赞类型：帖子、评论
	 * @param entityId     帖子或评论的 id
	 * @param entityUserId 被点赞的用户
	 * @param postId       给哪条帖子点的赞，或是在哪条帖子下给评论点的赞
	 * @return
	 */
	@LoginRequired
	@PostMapping("like")
	@ResponseBody
	public BaseResponse<LikeDTO> like(CommentEntityType entityType, Integer entityId, Integer entityUserId, Integer postId) {
		UserInfo userInfo = UserHolder.get();

		// 点赞
		likeService.like(userInfo.getId(), entityType, entityId, entityUserId);
		// 数量
		long likeCount = likeService.findEntityLikeCount(entityType, entityId);
		// 当前用户点赞状态
		LikeStatus likeStatus = likeService.findEntityLikeStatus(userInfo.getId(), entityType, entityId);
		// 返回的数据
		LikeDTO likeDTO = new LikeDTO();
		likeDTO.setLikeCount(likeCount);
		likeDTO.setLikeStatus(likeStatus);

		// 触发点赞事件，点赞发送通知，取消赞不发送通知
		if (likeStatus == LikeStatus.LIKE) {
			Event event = Event.builder()
					.topic(Topic.Like)
					.userId(UserHolder.get().getId())
					.entityType(entityType)
					.entityId(entityId)
					.entityUserId(entityUserId)
					.build()
					// 不管是给帖子点赞还是帖子下的评论点赞，被赞者查看通知时都是跳到帖子页面
					.setData("postId", postId);
			//发送消息
			eventProducer.fireEvent(event);
		}

		return BaseResponse.ok(likeDTO);
	}

}
