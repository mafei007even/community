package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.model.support.UserHolder;
import com.nowcoder.community.model.support.UserInfo;
import com.nowcoder.community.service.DataService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mafei007
 * @date 2020/5/17 23:09
 */

@Component
public class DataInterceptor implements HandlerInterceptor {

	private final DataService dataService;

	public DataInterceptor(DataService dataService) {
		this.dataService = dataService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 统计 UV
		String ip = request.getRemoteHost();
		dataService.recordUV(ip);

		// 统计 DAU
		UserInfo userInfo = UserHolder.get();
		if (userInfo != null) {
			dataService.recordDAU(userInfo.getId());
		}

		return true;
	}
}
