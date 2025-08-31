package com.winter.winter_back.common.utils;


import org.apache.commons.lang3.time.DateUtils;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil extends DateUtils {

	// 날짜 포맷(yyyy-MM-dd HH:mm:ss)
	public static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	// 시간 포맷(HH:mm)
	public static final String DB_CURR_TIME_FORMAT = "HH:mm";

	/**
	 * 폴더이름 날짜추출
	 */
	public static String getFolderDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(new Date());
	}

	/**
	 * 현재시간 추출
	 */
	public static String getNowDate() {
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat sdfCurrent = new SimpleDateFormat(DB_DATE_FORMAT);

		return sdfCurrent.format(today);
	}

	/**
	 * 현재시각 추출
	 */
	public static String getNowTime() {
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat sdfCurrent = new SimpleDateFormat(DB_CURR_TIME_FORMAT);

		return sdfCurrent.format(today);
	}

	/**
	 * 현재시간 추출
	 */
	public static String getNowDate(String format) {
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat sdfCurrent = new SimpleDateFormat(format);

		return sdfCurrent.format(today);
	}

	/**
	 * long형 데이터를 날짜로
	 */
	public static String convertLongToDateString(Long dateLong) {

		if (dateLong == null || dateLong == 0) {
			return null;
		}

		Date date = new Date(dateLong);

		SimpleDateFormat sdfCurrent = new SimpleDateFormat(DB_DATE_FORMAT);
		return sdfCurrent.format(date);
	}

	/**
	 * 지정된 포멧으로 시간 추출
	 */
	public static Date getDateByFormat(String date, String format) throws Exception {

		SimpleDateFormat sdfCurrent = new SimpleDateFormat(format);

		return sdfCurrent.parse(date);
	}

	public static String getDateByFormatToString(String date, String originFormat, String toFormat) throws ParseException {
		SimpleDateFormat sdfCurrent = new SimpleDateFormat(originFormat);
		Date tempDate = sdfCurrent.parse(date);
		sdfCurrent = new SimpleDateFormat(toFormat);
		return sdfCurrent.format(tempDate);
	}

	/**
	 * <p>
	 * yyyyMMdd 혹은 yyyy-MM-dd 형식의 날짜 문자열을 입력 받아 년, 월, 일을 증감한다. 년, 월, 일은 가감할 수를
	 * 의미하며, 음수를 입력할 경우 감한다.
	 * </p>
	 *
	 * <pre>
	 * DateUtil.addYearMonthDay("19810828", 0, 0, 19)  = "19810916"
	 * DateUtil.addYearMonthDay("20060228", 0, 0, -10) = "20060218"
	 * DateUtil.addYearMonthDay("20060228", 0, 0, 10)  = "20060310"
	 * DateUtil.addYearMonthDay("20060228", 0, 0, 32)  = "20060401"
	 * DateUtil.addYearMonthDay("20050331", 0, -1, 0)  = "20050228"
	 * DateUtil.addYearMonthDay("20050301", 0, 2, 30)  = "20050531"
	 * DateUtil.addYearMonthDay("20050301", 1, 2, 30)  = "20060531"
	 * DateUtil.addYearMonthDay("20040301", 2, 0, 0)   = "20060301"
	 * DateUtil.addYearMonthDay("20040229", 2, 0, 0)   = "20060228"
	 * DateUtil.addYearMonthDay("20040229", 2, 0, 1)   = "20060301"
	 * </pre>
	 *
	 * @param sDate 날짜 문자열(yyyyMMdd, yyyy-MM-dd의 형식)
	 * @param year 가감할 년. 0이 입력될 경우 가감이 없다
	 * @param month 가감할 월. 0이 입력될 경우 가감이 없다
	 * @param day 가감할 일. 0이 입력될 경우 가감이 없다
	 * @return yyyyMMdd 형식의 날짜 문자열
	 * @throws IllegalArgumentException 날짜 포맷이 정해진 바와 다를 경우.
	 *             입력 값이 <code>null</code>인 경우.
	 */
	public static String addYearMonthDay(String sDate, int year, int month, int day) {

		String dateStr = validChkDate(sDate);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
		try {
			cal.setTime(sdf.parse(dateStr));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid date format: " + dateStr);
		}

		if (year != 0)
			cal.add(Calendar.YEAR, year);
		if (month != 0)
			cal.add(Calendar.MONTH, month);
		if (day != 0)
			cal.add(Calendar.DATE, day);
		return sdf.format(cal.getTime());
	}

	/**
	 * 입력된 일자 문자열을 확인하고 8자리로 리턴
	 *
	 * @param dateStr
	 * @return
	 */
	public static String validChkDate(String dateStr) {
		String _dateStr = dateStr;

		if (dateStr == null || !(dateStr.trim().length() == 8 || dateStr.trim().length() == 10)) {
			throw new IllegalArgumentException("Invalid date format: " + dateStr);
		}
		if (dateStr.length() == 10) {
			_dateStr = replace(dateStr, "-", "");
		}
		return _dateStr;
	}

	/**
	 * 문자열 치환
	 * <p>
	 * 문자열에서 oldStr를 newStr로 변환한다.
	 *
	 * @param str
	 *            원본 문자열
	 * @param oldStr
	 *            수정될 문자
	 * @param newStr
	 *            수정된 문자
	 * @return String
	 */
	public static String replace(String str, String oldStr, String newStr) {
		StringBuffer sb = new StringBuffer();
		int savedPos = 0;
		while (true) {
			int pos = str.indexOf(oldStr, savedPos);
			if (pos != -1) {
				sb.append(str.substring(savedPos, pos));
				sb.append(newStr);
				savedPos = pos + oldStr.length();
				if (savedPos >= str.length())
					break;
			} else
				break;
		}
		sb.append(str.substring(savedPos, str.length()));
		return sb.toString();
	}

	public static boolean isThisDateValid(String dateToValidate, String dateFromat) {

		if (dateToValidate == null) {
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);

		try {
			sdf.parse(dateToValidate);
		} catch (ParseException e) {
			return false;
		}

		return true;
	}

	public static String timeStamp(int size) {
		return timeStamp().substring(0, size);
	}

	public static String getLogTimeStamp() {
		return timeStamp().substring(0, 14);
	}

	public static String timeStamp() {
		return timeStamp("yyyyMMddHHmmssSSS");
	}

	public static String timeStamp(String format) {
		return new SimpleDateFormat(format).format(new GregorianCalendar().getTime());
	}

	public static String timeStamp(String format, long date) {
		return new SimpleDateFormat(format).format(new Date(date));
	}

	public static String getSessionId() {
		SecureRandom secureRandom = new SecureRandom();
		long temp = Math.round(secureRandom.nextDouble() * 1000.0D);
		return timeStamp() + String.format("%04d", temp);
	}

	public static String getSeqId() {
		return timeStamp() + getRandomStr(4);
	}

	public static String getRandomStr(int nSize) {
		StringBuffer temp = new StringBuffer();
		SecureRandom secureRandom = new SecureRandom();
		for (int i = 0; i < nSize; i++) {
			int rIndex = secureRandom.nextInt(3);
			switch (rIndex) {
				case 0:
					// a-z
					temp.append((char) ((secureRandom.nextInt(26)) + 97));
					break;
				case 1:
					// A-Z
					temp.append((char) ((secureRandom.nextInt(26)) + 65));
					break;
				case 2:
					// 0-9
					temp.append((secureRandom.nextInt(10)));
					break;
			}
		}
		return temp.toString();
	}
}
