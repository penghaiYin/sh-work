package com.yph.shwork.utils;

import com.oigbuy.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 获取开始日期和结束日期
 * @Author: sheng.wang
 * @Date: 2021/07/31  16:28
 * @Description:
 */
public class DateTool {
    private static final Logger logger = LoggerFactory.getLogger(DateTool.class);
    private static final String datePattern="yyyy-MM-dd HH:mm:ss";

    private static final String datePattern2="yyyyMMdd HH:mm:ss";

    private static final String datePattern3="yyyy-MM-dd";

    private static final String datePattern4="yyyyMMdd";


    public static Date getStartDateTime(String startDate) throws ParseException {
        if(StringUtils.isBlank(startDate)){
            return null;
        }
        if(startDate.contains("-")){
            return DateUtils.parseDate(String.format("%s 00:00:00",startDate), datePattern);
        }
        return DateUtils.parseDate(String.format("%s 00:00:00",startDate), datePattern2);
    }

    public static Date getEndDateTime(String endDate) throws ParseException {
        if(StringUtils.isBlank(endDate)){
            return null;
        }
        if(endDate.contains("-")){
            return DateUtils.parseDate(String.format("%s 23:59:59",endDate), datePattern);
        }
        return DateUtils.parseDate(String.format("%s 23:59:59",endDate), datePattern2);
    }

    /**
     * 不抛出异常
     * @param startDate
     * @return
     */
    public static Date getStartDateTimeNoEx(String startDate){
        if(StringUtils.isBlank(startDate)){
            return null;
        }
        if(startDate.contains("-")){
            try {
                return DateUtils.parseDate(String.format("%s 00:00:00",startDate), datePattern);
            } catch (ParseException e) {
                logger.error("时间转换异常",e);
                throw new RuntimeException("时间转换异常",e);
            }
        }
        try {
            return DateUtils.parseDate(String.format("%s 00:00:00",startDate), datePattern2);
        } catch (ParseException e) {
            logger.error("时间转换异常",e);
            throw new RuntimeException("时间转换异常",e);
        }
    }

    /**
     * 不抛出异常
     * @param endDate
     * @return
     */
    public static Date getEndDateTimeNoEx(String endDate) {
        if(StringUtils.isBlank(endDate)){
            return null;
        }
        if(endDate.contains("-")){
            try {
                return DateUtils.parseDate(String.format("%s 23:59:59",endDate), datePattern);
            } catch (ParseException e) {
                logger.error("时间转换异常",e);
                throw new RuntimeException("时间转换异常",e);
            }
        }
        try {
            return DateUtils.parseDate(String.format("%s 23:59:59",endDate), datePattern2);
        } catch (ParseException e) {
            logger.error("时间转换异常",e);
            throw new RuntimeException("时间转换异常",e);
        }
    }

    public static Date getDate(String date) throws ParseException {
        if(StringUtils.isBlank(date)){
            return null;
        }
        if(date.contains("-")){
            return DateUtils.parseDate(date, datePattern3);
        }
        return DateUtils.parseDate(date, datePattern4);
    }

    public static Date getDateTime(String date) throws ParseException {
        if(StringUtils.isBlank(date)){
            return null;
        }
        if(date.contains("-")){
            return DateUtils.parseDate(date, datePattern);
        }
        return DateUtils.parseDate(date, datePattern2);
    }

    public static Date getPreviousMonthFirstDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        return calendar.getTime();
    }

    public static Date getPreviousMonthLastDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return calendar.getTime();
    }

    public static Date getCurrentMonthLastDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getCurrentMonthFirstDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        return calendar.getTime();
    }

    /**
     * 转换时区时间
     * @param srcDate
     * @param fromTime  原始时区
     * @param toTime    需要转换到的时区
     * @return
     */
    public static Date transformTimeZoneTime(Date srcDate, String fromTime, String toTime){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(toTime));
        cal.setTime(srcDate);
        Calendar date = Calendar.getInstance();
        date.setTimeZone(TimeZone.getTimeZone(fromTime));
        date.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
        date.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
        return date.getTime();
    }

    /**
     * 获取北京时区时间
     * @param srcDate
     * @param fromTime  原始时区
     * @return
     */
    public static Date getChinaTimeZoneTime(Date srcDate, String fromTime){
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        cal.setTime(srcDate);
        Calendar date = Calendar.getInstance();
        date.setTimeZone(TimeZone.getTimeZone(fromTime));
        date.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND));
        date.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),cal.get(Calendar.SECOND));
        return date.getTime();
    }

    /**
     *
     * @param srcDate   国际时间字符串
     * @param srcTimeZone  原始时区
     * @return
     */
    public static Date transformTimeZoneTime2(String srcDate, String srcTimeZone){
        SimpleDateFormat sdf;
        // DCP 返回时间存在两种情况
        if (srcDate.contains(".")) {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }
        sdf.setTimeZone(TimeZone.getTimeZone(srcTimeZone));
        Date date = null;
        try {
            date = sdf.parse(srcDate);
        } catch (ParseException e) {
            logger.error("转换时间异常");
        }
        return date;
    }

    /**
     * 转换日期为不带年月日的日期
     * @param date
     * @return
     */
    public static Date getDateNoHms(Date date){
        if(Objects.isNull(date)){
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 将时分秒,毫秒域清零
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
