package com.nowcoder.community.model.vo;

import com.nowcoder.community.model.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 某个用户关注的人
 * @author mafei007
 * @date 2020/5/4 14:47
 */

@Data
public class Followee {

	private User user;
	private Date followTime;
	/**
	 * 对于查看的用户来说，他有没有关注
	 */
	private boolean hasFollowed;

}
