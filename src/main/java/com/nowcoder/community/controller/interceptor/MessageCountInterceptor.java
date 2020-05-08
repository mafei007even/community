package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 显示顶部标题栏中的消息未读数量
 * @author mafei007
 * @date 2020/5/8 20:44
 */

@Component
public class MessageCountInterceptor implements HandlerInterceptor {

	private final MessageService messageService;

	public MessageCountInterceptor(MessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		UserInfo userInfo = UserHolder.get();
		if (userInfo != null && modelAndView != null) {
			int letterUnreadCount = messageService.findLetterUnreadCount(userInfo.getId(), null);
			int allNoticeUnreadCount = messageService.findAllNoticeUnreadCount(userInfo.getId());
			modelAndView.addObject("allUnreadCount", letterUnreadCount + allNoticeUnreadCount);
		}

	}
}
