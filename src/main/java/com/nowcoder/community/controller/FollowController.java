package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.FollowService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author mafei007
 * @date 2020/5/2 22:39
 */

@Controller
public class FollowController {

	private final FollowService followService;

	public FollowController(FollowService followService) {
		this.followService = followService;
	}

	@LoginRequired
	@PostMapping("follow")
	@ResponseBody
	public BaseResponse<Object> follow(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();
		if (userInfo.getId().equals(entityId)){
			return new BaseResponse<>(400, "不能自己关注自己！", null);
		}
		followService.follow(userInfo.getId(), entityType, entityId);
		return BaseResponse.ok("已关注！");
	}

	@LoginRequired
	@PostMapping("unfollow")
	@ResponseBody
	public BaseResponse<Void> unfollow(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();
		followService.unfollow(userInfo.getId(), entityType, entityId);
		return BaseResponse.ok("已取消关注！");
	}

}
