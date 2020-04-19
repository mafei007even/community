package com.nowcoder.community.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author mafei007
 * @date 2020/4/19 16:44
 */

@Component
@Aspect
@Slf4j
public class ServiceLogAop {

	@Pointcut("execution(*  com.nowcoder.community.service.*.*(..))")
	public void service() {
	}

	@Before("service()")
	public void before(JoinPoint joinPoint){
		// 用户[47.103.83.12],在[xxx],访问了[com.nowcoder.community.service.xxx()].
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest();
		String ip = request.getRemoteHost();
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String target = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();

		log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
	}

}
