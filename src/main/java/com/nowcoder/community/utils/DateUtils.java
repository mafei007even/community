package com.nowcoder.community.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author mafei007
 * @date 2020/5/17 22:21
 */


public final class DateUtils {

	public static String now(DateTimeFormatter formatter) {
		return LocalDateTime.now().format(formatter);
	}

}
