package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.model.vo.Followee;
import com.nowcoder.community.model.vo.Follower;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mafei007
 * @date 2020/5/2 22:39
 */

@Controller
public class FollowController {

	private final FollowService followService;
	private final UserService userService;

	public FollowController(FollowService followService, UserService userService) {
		this.followService = followService;
		this.userService = userService;
	}

	@LoginRequired
	@PostMapping("follow")
	@ResponseBody
	public BaseResponse<Object> follow(CommentEntityType entityType, Integer entityId) {
		UserInfo userInfo = UserHolder.get();
		if (userInfo.getId().equals(entityId)) {
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

	@GetMapping("followees/{userId}")
	@ApiOperation("根据userId查询该用户关注的人")
	public String getFollowees(@PathVariable Integer userId, Page page, Model model) {
		User user = userService.findUserById(userId);
		// 用户不存在
		if (user == null) {
			return "error/404";
		}

		// 设置分页数据
		page.setLimit(5);
		page.setPath("followees/" + userId);
		page.setRows((int) followService.findFolloweeCount(userId, CommentEntityType.USER));

		List<Followee> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
		// 判断当前用户是否关注了查询用户的某些关注者
		if (!CollectionUtils.isEmpty(followees)) {
			followees = followees.stream()
					.peek(followee -> followee.setHasFollowed(hasFollowed(followee.getUser().getId())))
					.collect(Collectors.toList());
		}

		model.addAttribute("user", user);
		model.addAttribute("followees", followees);
		return "site/followee";
	}

	@GetMapping("followers/{userId}")
	@ApiOperation("根据userId查询该用户的粉丝")
	public String getFollowers(@PathVariable Integer userId, Page page, Model model) {
		User user = userService.findUserById(userId);
		// 用户不存在
		if (user == null) {
			return "error/404";
		}

		// 设置分页数据
		page.setLimit(5);
		page.setPath("followers/" + userId);
		page.setRows((int) followService.findFollowerCount(CommentEntityType.USER, userId));

		List<Follower> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
		// 判断当前用户是否关注了查询用户的某些粉丝
		if (!CollectionUtils.isEmpty(followers)) {
			followers = followers.stream()
					.peek(follower -> follower.setHasFollowed(hasFollowed(follower.getUser().getId())))
					.collect(Collectors.toList());
		}

		model.addAttribute("user", user);
		model.addAttribute("followers", followers);
		return "site/follower";
	}

	/**
	 * 判断当前用户有没有关注指定的user
	 *
	 * @param userId
	 * @return
	 */
	private boolean hasFollowed(Integer userId) {
		if (UserHolder.get() == null) {
			return false;
		}
		return followService.hasFollowed(UserHolder.get().getId(), CommentEntityType.USER, userId);
	}

}
