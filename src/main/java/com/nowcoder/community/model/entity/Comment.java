package com.nowcoder.community.model.entity;

import com.nowcoder.community.model.enums.CommentEntityType;
import lombok.Data;

import java.util.Date;

@Data
public class Comment {

    private Integer id;
    private Integer userId;

    /**
     * 帖子的评论
     * 评论的评论
     * 课程的评论
     * ....
     */
    private CommentEntityType entityType;

    /**
     * 评论对应帖子或课程的 id
     * 或者这条评论是评论的评论，那么entityId 就对应评论的id
     */
    private Integer entityId;
    private Integer targetId;
    private String content;
    private Integer status;
    private Date createTime;
    
}