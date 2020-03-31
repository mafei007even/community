package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author mafei007
 * @date 2020/3/30 18:23
 */


public interface DiscussPostMapper extends Mapper<DiscussPost> {


    List<DiscussPost> selectDiscussPosts(Integer userId, Integer offset, Integer limit);

    /**
     * 视频说如果只有一个参数，使用动态sql在 <if>中使用，必需使用别名@Param
     * @param userId
     * @return
     */
    Integer selectDiscussPostRows(@Param("userId") Integer userId);


}
