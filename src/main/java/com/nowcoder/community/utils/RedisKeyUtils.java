package com.nowcoder.community.utils;

import com.nowcoder.community.model.enums.CommentEntityType;
import org.springframework.util.Assert;

/**
 * @author mafei007
 * @date 2020/4/19 22:12
 */


public class RedisKeyUtils {

	/**
	 * redis 中使用 : 作为单词间的分隔符
	 */
	private static final String SPLIT = ":";
	private static final String PREFIX_ENTITY_LIKE = "like:entity";
	private static final String PREFIX_USER_LIKE = "like:user";
	private static final String PREFIX_FOLLOWEE = "followee";
	private static final String PREFIX_FOLLOWER = "follower";
	private static final String PREFIX_CAPTCHA = "captcha";
	private static final String PREFIX_TICKET = "ticket";
	private static final String PREFIX_USER = "user";

	/**
	 *
	 * 某个实体的赞
	 * redis 中的结构：
	 * 		k  -> v
	 * 		like:entity:entityType:entityId -> set(userId)
	 *
	 * value 如果使用数值来存，每次点赞都 increment，但是这样就不知道是谁点的赞.
	 * 所以这里使用 set 集合，每次点赞都存放点赞用户的 userId，
	 * 这样既能统计点赞个数，也可以根据 userId 找到点赞的用户
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public static String getEntityLikeKey(CommentEntityType entityType, Integer entityId){
		Assert.notNull(entityType, "点赞的实体类型不能为空");
		Assert.notNull(entityId, "点赞的实体id不能为空");
		return PREFIX_ENTITY_LIKE + SPLIT + entityType.getValue() + SPLIT + entityId;
	}

	/**
	 * 某个用户的赞，是被赞的那个 userId
	 *
	 * like:user:userId -> int
	 * 存的就是一个数
	 *
	 * @param userId
	 * @return
	 */
	public static String getUserLikeKey(Integer userId){
		return PREFIX_USER_LIKE + SPLIT + userId;
	}

	/**
	 * 某个用户关注的实体
	 * 关注的目标可以是用户、帖子、题目等
	 *
	 * followee:userId:entityType -> zset(entityId, now)
	 *
	 * 排序set 存入 now，就是关注的时间
	 * @param userId
	 * @param entityType
	 * @return
	 */
	public static String getFolloweeKey(Integer userId, CommentEntityType entityType){
		return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType.getValue();
	}

	/**
	 * 某个实体拥有的粉丝
	 *
	 * follower:entityType:entityId -> zset(userId, now)
	 *
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public static String getFollowerKey(CommentEntityType entityType, Integer entityId){
		return PREFIX_FOLLOWER + SPLIT + entityType.getValue() + SPLIT + entityId;
	}


	public static String getCaptchaKey(String captchaId) {
		return PREFIX_CAPTCHA + SPLIT + captchaId;
	}

	public static String getTicketKey(String ticket) {
		return PREFIX_TICKET + SPLIT + ticket;
	}

	/**
	 * User 对象的缓存
	 * @param userId
	 * @return
	 */
	public static String getUserKey(Integer userId) {
		return PREFIX_USER + SPLIT + userId;
	}

}
