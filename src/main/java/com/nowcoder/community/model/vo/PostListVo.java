package com.nowcoder.community.model.vo;

import com.nowcoder.community.model.entity.DiscussPost;
import lombok.Data;

/**
 * 个人主页中我的帖子页面用到的
 *
 * @author mafei007
 * @date 2020/5/25 21:30
 */

@Data
public class PostListVo {

	private DiscussPost post;
	private long likeCount;

}
