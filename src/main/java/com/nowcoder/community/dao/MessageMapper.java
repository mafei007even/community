package com.nowcoder.community.dao;

import com.nowcoder.community.model.entity.Message;
import com.nowcoder.community.model.enums.MessageStatus;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 *
 * count 方法用来计算分页用
 *
 * @author mafei007
 * @date 2020/4/18 15:16
 */


public interface MessageMapper extends Mapper<Message> {


	/**
	 * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
	 * 且要分页查,limit 使用 PageHelper
	 *
	 * 每个会话会有多条message，最大的messageId 的那条就是最新的消息
	 *
		 select * from message
		 where id in
		 (
			 select max(id) from message
			 where from_id != 1
			 and status != 2
			 and (from_id = 111 or to_id= 111)
			 GROUP BY conversation_id
		 )
		 ORDER BY create_time desc
		 limit x,x
	 *
	 * 这里 order by 排序使用 id 和 create_time都可以.
	 * 因为 id 是自增长的，后面发的消息 id 肯定就大，后面发的消息create_time肯定也大
	 * 所以用哪个来排序都行
	 *
	 * @param userId
	 * @return
	 */
	@Select("select * from message " +
			"where id in " +
			"(" +
				"select max(id) from message " +
				"where from_id != 1 " +
				"and status != 2 " +
				"and (from_id = #{userId} or to_id = #{userId}) " +
				"GROUP BY conversation_id " +
			") " +
			"ORDER BY create_time desc")
	List<Message> selectConversations(Integer userId);


	/**
	 * 查询当前用户的会话数量
	 * 跟 selectConversations() 方法差不多
	 *
	 * select count(m.maxid) from
	 * (
	 *    select max(id) as maxid from message
	 *    where from_id != 1
	 *    and status != 2
	 *    and (from_id = 111 or to_id= 111)
	 *    GROUP BY conversation_id
	 * ) as m
	 *
	 * @param userId
	 * @return
	 */
	@Select("select count(m.maxid) from " +
			"(" +
				"select max(id) as maxid from message " +
				"where from_id != 1 " +
				"and status != 2 " +
				"and (from_id = #{userId} or to_id = #{userId}) " +
				"GROUP BY conversation_id" +
			") as m")
	int selectConversationCount(Integer userId);


	/**
	 * 查询某个会话所包含的私信列表，需要分页
	 * @param conversationId
	 * @return
	 */
	@Select("select * from message " +
			"where status != 2 " +
			"and from_id != 1 " +
			"and conversation_id = #{conversationId} " +
			"order by id desc")
	List<Message> selectLetters(String conversationId);


	/**
	 * 查询某个会话所包含的私信数量，跟 selectLetters() 差不多
	 * @param conversationId
	 * @return
	 */
	@Select("select count(id) from message " +
			"where status != 2 " +
			"and from_id != 1 " +
			"and conversation_id = #{conversationId}")
	int selectLetterCount(String conversationId);

	/**
	 * 查询未读私信的数量
	 * 有两处要用到：
	 * 		1. 需要所有的未读会话数量
	 * 		2. 需要某一个会话的未读数量
	 * 	如果	conversationId 传了就拼上条件
	 *
	 *   select count(id)
	 *   from message
	 *   where status = 0
	 *   and from_id != 1
	 *   and to_id = #{userId}
	 *   <if test="conversationId != null">
	 *       and conversation_id = #{conversationId}
	 *   </if>
	 *
	 * @param userId
	 * @param conversationId
	 * @return
	 */
	int selectLetterUnreadCount(Integer userId, String conversationId);

	/**
	 * 更新消息状态
	 *
	 *    update message
	 *    set status = #{status}
	 *    where id in
	 *    <foreach collection="ids" item="id" open="(" close=")" separator=",">
	 *        #{id}
	 *    </foreach>
	 *
	 * @param ids
	 * @param status
	 * @return
	 */
	int updateStatus(List<Integer> ids, MessageStatus status);

}
