package com.nowcoder.community.model.entity;

import com.nowcoder.community.model.enums.MessageStatus;
import lombok.Data;

import javax.persistence.Table;
import java.util.Date;

/**
 * @author mafei007
 * @date 2020/4/18 15:08
 */

@Data
@Table(name = "message")
public class Message {

	private Integer id;
	private Integer fromId;
	private Integer toId;
	private String conversationId;
	private String content;
	private MessageStatus status;
	private Date createTime;

}
