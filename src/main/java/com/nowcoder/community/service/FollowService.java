package com.nowcoder.community.service;

import com.nowcoder.community.model.enums.CommentEntityType;
import com.nowcoder.community.utils.RedisKeyUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @author mafei007
 * @date 2020/5/2 22:14
 */

@Service
public class FollowService {

	private final RedisTemplate<String, Object> redisTemplate;

	public FollowService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 关注
	 * <p>
	 * 某个用户关注的实体:
	 * 		followee:userId:entityType -> zset(entityId, now)
	 * <p>
	 * 某个实体拥有的粉丝:
	 * 		follower:entityType:entityId -> zset(userId, now)
	 *
	 * @param userId     执行关注操作的用户
	 * @param entityType 关注的实体类型，可以是用户、帖子、题目等
	 * @param entityId   关注实体类型对应的 id
	 */
	public void follow(Integer userId, CommentEntityType entityType, Integer entityId) {
		// 一个业务有两次或以上写操作就要用事务
		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);

				// 开启事务
				operations.multi();

				operations.opsForZSet().add((K) followeeKey, (V) entityId, System.currentTimeMillis());
				operations.opsForZSet().add((K) followerKey, (V) userId, System.currentTimeMillis());

				// 提交事务
				return operations.exec();
			}
		});
	}

	/**
	 * 取消关注，详情注释见
	 * @see #follow
	 *
	 * @param userId
	 * @param entityType
	 * @param entityId
	 */
	public void unfollow(Integer userId, CommentEntityType entityType, Integer entityId) {
		// 一个业务有两次或以上写操作就要用事务
		redisTemplate.execute(new SessionCallback<Object>() {
			@Override
			public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
				String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);

				// 开启事务
				operations.multi();

				operations.opsForZSet().remove((K) followeeKey, (V) entityId);
				operations.opsForZSet().remove((K) followerKey, (V) userId);

				// 提交事务
				return operations.exec();
			}
		});
	}

	/**
	 * 查询某个用户关注的实体的数量
	 * @param userId
	 * @param entityType
	 * @return
	 */
	public long findFolloweeCount(Integer userId, CommentEntityType entityType){
		String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().zCard(followeeKey);
	}

	/**
	 * 查询某个实体的粉丝数量
	 * @param entityType
	 * @param entityId
	 * @return
	 */
	public long findFollowerCount(CommentEntityType entityType, Integer entityId){
		String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);
		return redisTemplate.opsForZSet().zCard(followerKey);
	}

	/**
	 * 查询当前用户是否已关注该实体
	 * @param userId 要查询的当前用户
	 * @param entityType User
	 * @param entityId 被关注的实体 id，这里是 userId
	 * @return
	 */
	public boolean hasFollowed(Integer userId, CommentEntityType entityType, Integer entityId) {
		String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
	}

}
