package com.nowcoder.community.model.vo;

import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.User;
import lombok.Data;

import java.util.List;

/**
 *
 * 评论的vo
 *
 * @author mafei007
 * @date 2020/4/9 17:00
 */

@Data
public class CommentVO {

    private Comment comment;
    private User user;
    private List<ReplyVO> replys;
    private Integer replyCount;

}
