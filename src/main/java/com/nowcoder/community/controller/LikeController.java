package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.dto.LikeDTO;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.LikeStatus;
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

	public LikeController(LikeService likeService) {
		this.likeService = likeService;
	}

	@LoginRequired
	@PostMapping("like")
	@ResponseBody
	public BaseResponse<LikeDTO> like(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();

		// 点赞
		likeService.like(userInfo.getId(), entityType, entityId);
		// 数量
		long likeCount = likeService.findEntityLikeCount(entityType, entityId);
		// 当前用户点赞状态
		LikeStatus likeStatus = likeService.findEntityLikeStatus(userInfo.getId(), entityType, entityId);
		// 返回的数据
		LikeDTO likeDTO = new LikeDTO();
		likeDTO.setLikeCount(likeCount);
		likeDTO.setLikeStatus(likeStatus);
		return BaseResponse.ok(likeDTO);
	}

}
