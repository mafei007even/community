package com.nowcoder.community.service;

import com.github.pagehelper.PageHelper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.model.dto.Page;
import com.nowcoder.community.model.entity.Message;
import com.nowcoder.community.model.enums.MessageStatus;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/18 16:58
 */

@Service
public class MessageService {

	private final MessageMapper messageMapper;
	private final SensitiveFilter sensitiveFilter;

	public MessageService(MessageMapper messageMapper, SensitiveFilter sensitiveFilter) {
		this.messageMapper = messageMapper;
		this.sensitiveFilter = sensitiveFilter;
	}


	public List<Message> findConversations(Integer userId, Page page) {
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectConversations(userId);
	}

	/**
	 * 可以从 PageHelper 中取分页信息
	 * @param userId
	 * @return
	 */
	public int findConversationCount(Integer userId) {
		return messageMapper.selectConversationCount(userId);
	}

	public List<Message> findLetters(String conversationId, Page page) {
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectLetters(conversationId);
	}

	/**
	 * 可以从 PageHelper 中取分页信息
	 * @param conversationId
	 * @return
	 */
	public int findLetterCount(String conversationId) {
		return messageMapper.selectLetterCount(conversationId);
	}

	public int findLetterUnreadCount(Integer userId, String conversationId) {
		return messageMapper.selectLetterUnreadCount(userId, conversationId);
	}

	public int addMessage(Message message){
		Assert.notNull(message, "消息不能为空！");

		message.setContent(HtmlUtils.htmlEscape(message.getContent()));
		message.setContent(sensitiveFilter.filter(message.getContent()));
		return messageMapper.insertSelective(message);
	}

	/**
	 * 更改消息状态为已读
	 * @param ids 要更改的消息id
	 * @return 更改的个数
	 */
	public int readMessage(List<Integer> ids){
		if (CollectionUtils.isEmpty(ids)){
			return 0;
		}
		return messageMapper.updateStatus(ids, MessageStatus.READED);
	}

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下最新的一条通知
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public Message findLatestNotice(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectLatestNotice(userId, topic);
	}

	/**
	 * 查询用户某个系统通知（点赞、评论、关注）下的总通知数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public int findNoticeCount(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectNoticeCount(userId, topic);
	}


	/**
	 * 查询用户某个系统通知（点赞、评论、关注）未读通知的数量
	 *
	 * @param userId
	 * @param topic
	 * @return
	 */
	public int findNoticeUnreadCount(Integer userId, Topic topic) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		return messageMapper.selectNoticeUnreadCount(userId, topic);
	}

	/**
	 * 查询用户所有系统通知（点赞、评论、关注）未读通知的总数量
	 *
	 * @param userId
	 * @return
	 */
	public int findAllNoticeUnreadCount(Integer userId) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		return messageMapper.selectAllNoticeUnreadCount(userId);
	}

	/**
	 * 分页查询用户某个系统通知（点赞、评论、关注）的列表
	 * @param userId
	 * @param topic
	 * @param page
	 * @return
	 */
	public List<Message> findNotices(Integer userId, Topic topic, Page page) {
		Assert.notNull(userId, "要查询通知的 userId 不能为空");
		Assert.notNull(topic, "要查询通知的 topic 不能为空");
		Assert.notNull(page, "分页参数不能为空");
		PageHelper.startPage(page.getCurrent(), page.getLimit());
		return messageMapper.selectNotices(userId, topic);
	}

}
