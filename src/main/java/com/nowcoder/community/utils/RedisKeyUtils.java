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

}
