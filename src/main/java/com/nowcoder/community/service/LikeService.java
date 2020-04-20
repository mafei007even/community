package com.nowcoder.community.service;

import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.model.enums.LikeStatus;
import com.nowcoder.community.utils.RedisKeyUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author mafei007
 * @date 2020/4/19 22:28
 */

@Service
public class LikeService {

	private final RedisTemplate<String, Object> redisTemplate;

	public LikeService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}


	/**
	 * 点赞
	 * redis 中的结构：
	 * 		k  -> v
	 * 		like:entity:entityType:entityId -> set(userId)
	 *
	 * 当前用户没点过赞，那就执行点赞
	 * 如果点过赞，那就是取消点赞
	 *
	 * @param userId 谁点的赞
	 * @param entityType 点赞的实体类型
	 * @param entityId 具体的实体
	 */
	public void like(Integer userId, CommentEntityType entityType, Integer entityId){
		Assert.notNull(userId, "点赞的用户id不能为空");
		Assert.notNull(entityType, "点赞的实体类型不能为空");
		Assert.notNull(entityId, "点赞的实体id不能为空");

		String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);

		// 判断有没有点过赞，如果点过那就是取消点赞
		Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
		if (isMember) {
			redisTemplate.opsForSet().remove(entityLikeKey, userId);
		} else{
			redisTemplate.opsForSet().add(entityLikeKey, userId);
		}
	}

	/**
	 * 查询实体点赞的数量
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public long findEntityLikeCount(CommentEntityType entityType, Integer entityId){
		String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
		Long size = redisTemplate.opsForSet().size(entityLikeKey);
		return size == null ? 0 : size;
	}

	/**
	 * 查询某人对某实体的点赞状态，就是有没有点过赞
	 * 返回 boolean 就行，但这里为了扩展，有可能点的是踩，返回一个枚举，来表示3中状态
	 * @param userId
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public LikeStatus findEntityLikeStatus(Integer userId, CommentEntityType entityType, Integer entityId){
		Assert.notNull(userId, "点赞的用户id不能为空");
		Assert.notNull(entityType, "点赞的实体类型不能为空");
		Assert.notNull(entityId, "点赞的实体id不能为空");

		String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType, entityId);
		Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
		return isMember ? LikeStatus.LIKE : LikeStatus.NONE;
	}

}
