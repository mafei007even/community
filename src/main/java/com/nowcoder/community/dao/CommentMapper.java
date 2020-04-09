package com.nowcoder.community.dao;

import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.enums.CommentEntityType;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/4/6 19:01
 */


public interface CommentMapper extends Mapper<Comment> {


    /**
     *
     *  select *
     *  from comment
     *  where status = 0
     *  and entity_type = #{entityType}
     *  and entity_id = #{entityId}
     *  order by create_time asc
     *
     *  使用PageHealer
     *  limit x, x
     *
     * @param entityType
     * @param entityId
     * @return
     */
    List<Comment> findCommentsByEntity(CommentEntityType entityType, Integer entityId);


    /**
     *         select count(id)
     *         from comment
     *         where status = 0
     *         and entity_type = #{entityType}
     *         and entity_id = #{entityId}
     *
     * @param entityType
     * @param entityId
     * @return
     */
    // int findCountByEntity(Integer entityType, Integer entityId);

}
