package com.nowcoder.community.model.vo;

import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.DiscussPost;
import com.nowcoder.community.model.entity.User;
import lombok.Data;

/**
 * 个人主页中我的回复页面用到的
 *
 * @author mafei007
 * @date 2020/5/25 23:18
 */

@Data
public class ReplyListVo {

	/**
	 * 这条评论/回复是在哪个帖子下的
	 */
	private DiscussPost post;

	/**
	 * 评论/回复的实体
	 */
	private Comment reply;

	/**
	 * 这条评论/回复的点赞数
	 */
	private long likeCount;

	/**
	 * 如果这条评论是给评论进行评论 或 给评论进行回复的话，此字段有值
	 * 为回复的那条评论
	 */
	private Comment target;

	/**
	 * 如果这条评论是给评论进行评论 或 给评论进行回复的话，此字段有值
	 * 为回复的那条评论的用户
	 */
	private User targetUser;

}
