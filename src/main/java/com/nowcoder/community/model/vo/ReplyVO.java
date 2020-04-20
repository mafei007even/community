package com.nowcoder.community.model.vo;

import com.nowcoder.community.model.entity.Comment;
import com.nowcoder.community.model.entity.User;
import com.nowcoder.community.model.enums.LikeStatus;
import lombok.Data;

/**
 * 评论的评论vo
 *
 * @author mafei007
 * @date 2020/4/9 17:04
 */

@Data
public class ReplyVO {

    private Comment reply;
    private User user;
    private User target;
    private long likeCount;
    private LikeStatus likeStatus;

}
