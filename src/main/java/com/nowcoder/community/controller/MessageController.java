package com.nowcoder.community.controller;

import com.github.pagehelper.PageInfo;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.exception.NotFoundException;
import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.Message;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.MessageStatus;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.model.enums.ValueEnum;
import com.nowcoder.community.model.params.MessageParam;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.model.vo.ConversationVo;
import com.nowcoder.community.model.vo.LetterVo;
import com.nowcoder.community.model.vo.MessageNoticeVO;
import com.nowcoder.community.model.vo.NoticeForListVO;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.JsonUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mafei007
 * @date 2020/4/18 17:05
 */

@Controller
public class MessageController {

	private final MessageService messageService;
	private final UserService userService;

	public MessageController(MessageService messageService, UserService userService) {
		this.messageService = messageService;
		this.userService = userService;
	}

	/**
	 * 私信列表
	 *
	 * @return
	 */
	@LoginRequired
	@GetMapping("letter/list")
	@ApiOperation("私信列表")
	public String getLetterList(Model model, Page page) {
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		//分页信息
		page.setLimit(5);
		page.setPath("/letter/list");

		// 会话列表
		List<Message> conversationList = messageService.findConversations(userId, page);

		// page.setRows(messageService.findConversationCount(userInfo.getId()));
		// 使用 PageHelper 来获取分页信息
		PageInfo<Message> pageInfo = new PageInfo<>(conversationList);
		page.setRows((int) pageInfo.getTotal());

		// 每个会话需要的都放在 ConversationVo
		List<ConversationVo> conversationVos = new ArrayList<>();
		if (conversationList != null) {
			for (Message message : conversationList) {
				ConversationVo conversationVo = new ConversationVo();
				String conversationId = message.getConversationId();
				conversationVo.setConversation(message);
				conversationVo.setLetterCount(messageService.findLetterCount(conversationId));
				conversationVo.setUnreadCount(messageService.findLetterUnreadCount(userId, conversationId));

				// 私信用户id，需要查出用户名和头像
				// 有可能此 Message 是当前用户发给别人，那目标id就是 to_id
				// 有可能此 Message 是别人发给当前用户，那目标id就是 from_id
				int targetId = userId.equals(message.getFromId()) ? message.getToId() : message.getFromId();
				conversationVo.setTarget(userService.findUserById(targetId));

				conversationVos.add(conversationVo);
			}
		}

		// 总未读数量
		int allLetterUnreadCount = messageService.findLetterUnreadCount(userId, null);

		// 查询系统通知未读消息数量
		int allNoticeUnreadCount = messageService.findAllNoticeUnreadCount(userId);
		model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

		model.addAttribute("page", page);
		model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);
		model.addAttribute("conversations", conversationVos);
		return "site/letter";
	}

	@LoginRequired
	@GetMapping("letter/{conversationId}")
	@ApiOperation("查看私信详情，并且将未读的消息设为已读")
	public String getLetterDetail(@PathVariable String conversationId, Page page, Model model) {
		isOwner(conversationId);
		//分页信息
		page.setLimit(5);
		page.setPath("/letter/" + conversationId);

		// 私信列表
		List<Message> letterList = messageService.findLetters(conversationId, page);

		// page.setRows(messageService.findLetterCount(conversationId));
		// 使用 PageHelper 来获取分页信息
		PageInfo<Message> pageInfo = new PageInfo<>(letterList);
		page.setRows((int) pageInfo.getTotal());

		List<LetterVo> letterVoList = new ArrayList<>();
		// 需要 from_user
		if (letterList != null) {
			for (Message message : letterList) {
				LetterVo letterVo = new LetterVo();
				letterVo.setLetter(message);
				letterVo.setFromUser(userService.findUserById(message.getFromId()));
				letterVoList.add(letterVo);
			}
		}

		// 将未读的消息设为已读
		List<Integer> ids = getUnreadLetterIds(letterList);
		messageService.readMessage(ids);

		// 私信目标
		model.addAttribute("target", getLetterTarget(conversationId));
		model.addAttribute("letterVoList", letterVoList);

		return "site/letter-detail";
	}


	@LoginRequired
	@PostMapping("letter/send")
	@ResponseBody
	public BaseResponse sendLetter(@Valid MessageParam messageParam) {
		if (UserHolder.get().getUsername().equals(messageParam.getToName())) {
			return new BaseResponse(400, "不能给自己私信", null);
		}
		User target = userService.findUserByUsername(messageParam.getToName());
		if (target == null) {
			throw new NotFoundException("要发送私信的用户不存在");
		}

		Message message = messageParam.convertTo(target);
		messageService.addMessage(message);

		return BaseResponse.ok("发送私信成功！");
	}

	private List<Integer> getUnreadLetterIds(List<Message> letterList) {
		// 当前用户是消息的接收者才是进行读的操作
		// 如果是发送者，那不算已读，只有接收者才算已读
		if (letterList != null) {
			return letterList.stream()
					.filter(message -> UserHolder.get().getId().equals(message.getToId()) && message.getStatus() == MessageStatus.UNREAD)
					.map(Message::getId)
					.collect(Collectors.toList());
		}

		return null;
	}

	private User getLetterTarget(String conversationId) {
		String[] ids = conversationId.split("_");
		int id0 = Integer.parseInt(ids[0]);
		int id1 = Integer.parseInt(ids[1]);
		if (UserHolder.get().getId().equals(id0)) {
			return userService.findUserById(id1);
		}
		return userService.findUserById(id0);
	}

	private void isOwner(String conversationId) {
		String[] ids = conversationId.split("_");
		Arrays.stream(ids)
				.filter(id -> UserHolder.get().getId().toString().equals(id))
				.findAny()
				.orElseThrow(() -> new RuntimeException("没有权限访问非自己的消息！"));
	}

	@LoginRequired
	@GetMapping("notice/list")
	@ApiOperation("查询用户最新一条的（点赞、评论、关注）通知")
	public String getNoticeList(Model model) {
		UserInfo userInfo = UserHolder.get();
		Integer userId = userInfo.getId();

		// 查询评论类通知
		MessageNoticeVO commentNoticeVO = getLatestMessageVo(userId, Topic.Comment);
		model.addAttribute("commentNotice", commentNoticeVO);

		// 查询点赞类通知
		MessageNoticeVO likeNoticeVO = getLatestMessageVo(userId, Topic.Like);
		model.addAttribute("likeNotice", likeNoticeVO);

		// 查询关注类通知
		MessageNoticeVO followNoticeVO = getLatestMessageVo(userId, Topic.Follow);
		model.addAttribute("followNotice", followNoticeVO);

		// 查询私信未读消息数量
		int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
		model.addAttribute("letterUnreadCount", letterUnreadCount);

		// 查询系统通知未读消息数量
		int allNoticeUnreadCount = messageService.findAllNoticeUnreadCount(userId);
		model.addAttribute("allNoticeUnreadCount", allNoticeUnreadCount);

		// 用户私信总未读数量
		int allLetterUnreadCount = messageService.findLetterUnreadCount(userId, null);
		model.addAttribute("allLetterUnreadCount", allLetterUnreadCount);

		return "site/notice";
	}

	/**
	 * 查询用户不同类型的最新一条通知
	 * @param userId
	 * @param topic 点赞、评论、关注
	 * @return
	 */
	private MessageNoticeVO getLatestMessageVo(Integer userId, Topic topic) {
		Message latestTopicNotice = messageService.findLatestNotice(userId, topic);
		// message vo
		MessageNoticeVO noticeVO = new MessageNoticeVO();
		if (latestTopicNotice != null) {
			noticeVO.setMessage(latestTopicNotice);

			String jsonContent = HtmlUtils.htmlUnescape(latestTopicNotice.getContent());
			Map<String, Object> content = JsonUtils.jsonToMap(jsonContent, String.class, Object.class);

			noticeVO.setUser(userService.findUserById((Integer) content.get("userId")));

			CommentEntityType entityType = ValueEnum.valueToEnum(CommentEntityType.class, (Integer) content.get("entityType"));
			noticeVO.setEntityType(entityType);
			noticeVO.setEntityId((Integer) content.get("entityId"));


			int count = messageService.findNoticeCount(userId, topic);
			noticeVO.setCount(count);

			int unread = messageService.findNoticeUnreadCount(userId, topic);
			noticeVO.setUnread(unread);

			// 关注类通知不需要 postId 参数
			if (topic != Topic.Follow) {
				noticeVO.setPostId((Integer) content.get("postId"));
			}
		}
		return noticeVO;
	}

	@LoginRequired
	@GetMapping("notice/detail/{topic}")
	public String getNoticeDetail(@PathVariable Topic topic, Page page, Model model) {
		UserInfo userInfo = UserHolder.get();

		page.setLimit(5);
		page.setPath("/notice/detail/" + topic.getValue());

		List<Message> notices = messageService.findNotices(userInfo.getId(), topic, page);
		// 使用 PageHelper 来获取分页信息，总行数
		PageInfo<Message> pageInfo = new PageInfo<>(notices);
		page.setRows((int) pageInfo.getTotal());

		List<NoticeForListVO> noticeVoList = notices.stream()
				.map(notice -> {
					NoticeForListVO noticeVo = new NoticeForListVO();
					noticeVo.setNotice(notice);

					String jsonContent = HtmlUtils.htmlUnescape(notice.getContent());
					Map<String, Object> content = JsonUtils.jsonToMap(jsonContent, String.class, Object.class);

					noticeVo.setUser(userService.findUserById((Integer) content.get("userId")));

					CommentEntityType entityType = ValueEnum.valueToEnum(CommentEntityType.class, (Integer) content.get("entityType"));
					noticeVo.setEntityType(entityType);
					noticeVo.setEntityId((Integer) content.get("entityId"));

					// 关注类通知不需要 postId 参数
					if (topic != Topic.Follow) {
						noticeVo.setPostId((Integer) content.get("postId"));
					}
					// 通知者，就是 id 为 1 的系统管理员
					noticeVo.setFromUser(userService.findUserById(notice.getFromId()));

					return noticeVo;
				}).collect(Collectors.toList());

		model.addAttribute("notices", noticeVoList);
		model.addAttribute("page", page);

		// 设置已读
		List<Integer> unreadIds = getUnreadLetterIds(notices);
		if (!CollectionUtils.isEmpty(unreadIds)) {
			messageService.readMessage(unreadIds);
		}

		return "site/notice-detail";
	}

}
