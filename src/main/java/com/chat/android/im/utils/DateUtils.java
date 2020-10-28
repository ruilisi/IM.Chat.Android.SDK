package com.chat.android.im.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static String getDateTimeStr(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return String.format("%d/%02d/%02d %02d:%02d:%02d", year, month, day,
                hour, min, sec);
    }

    public static String getDateTimeStr1(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return String.format("%02d月%02d日 %02d:%02d", month, day,
                hour, min);
    }

    public static String getDateTimeStr2(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return String.format("%d年%02d月%02d日 %02d:%02d", year, month, day,
                hour, min);
    }


    public static int getHourOfDate(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return hour;
    }

    public static String getTimeStr(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    public static String getHourMinuteOfDate(long tick) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        return String.format("%02d:%02d:%02d", hour, min,sec);
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    // public static String getNormalDateString(Context context, long dateMi)
    // {
    // Calendar calendar = Calendar.getInstance();
    // calendar.clear();
    // calendar.setTimeInMillis(dateMi);
    // calendar.setTimeZone(TimeZone.getDefault());
    //
    // return String.format(context.getString(R.string.misc_daystr) +
    // " %3$d:%4$d:%5$d", calendar.get(Calendar.MONTH) + 1,
    // calendar.get(Calendar.DAY_OF_MONTH),
    // calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
    // calendar.get(Calendar.SECOND));
    //
    // }

//    public static String getTextFlagBeforeDate(Context context, long dateMi) {
//        int mins = (int) ((System.currentTimeMillis() - dateMi) / (1000 * 60));
//        if (mins < 0)
//            return "";
//        else if (mins == 0)
//            return String.format(
//                    context.getString(R.string.smsdetail_before_min), 1);
//        else if (mins < 60)
//            return String.format(
//                    context.getString(R.string.smsdetail_before_min), mins);
//        else if (mins < 60 * 24)
//            return String.format(
//                    context.getString(R.string.smsdetail_before_hour),
//                    mins / 60);
//        else
//            return String.format(
//                    context.getString(R.string.smsdetail_before_day), mins
//                            / (60 * 24));
//
//    }

//    public static String getDaysNum(Context context, String data) {
//        if (StringUtils.isEmpty(data)) {
//            return context.getString(R.string.is_end_time);
//        }
//        double dateMi = 0;
//        try {
//            SimpleDateFormat simpleDateFormat = null;
//            if (data.contains("-")) {
//                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            } else {
//                simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
//            }
//            Date date = simpleDateFormat.parse(data);
//            dateMi = date.getTime();
//        } catch (ParseException e) {
//            dateMi = 0;
//            e.printStackTrace();
//        }
//        double times = dateMi - System.currentTimeMillis();
//        if (times <= 0) {
//            return context.getString(R.string.is_end_time);
//        } else {
////            int day = (int) (times / 1000 / 60 / 60 / 24);
//            int day = (int) Math.ceil(times / 86400000);
//            return day + context.getString(R.string.day);
//        }
//
//    }

//    public static String getTextFlagAfterDate(Context context, long dateMi) {
//        String word = "";
//        int mins = (int) ((dateMi - System.currentTimeMillis()) / (1000 * 60));
//        if (mins < 0) {
//            word = "前";
//        } else {
//            word = "后";
//        }
//        mins = Math.abs(mins);
//        // return StringUtils.getAnotherDate(dateMi);
//        if (mins == 0)
//            return context.getString(R.string.unknow);
////			return String.format(
////					context.getString(R.string.smsdetail_after_min), 1)
////					+ word;
//        else if (mins < 60)
//            return String.format(
//                    context.getString(R.string.smsdetail_after_min), mins)
//                    + word;
//        else if (mins < 60 * 24)
//            return String
//                    .format(context.getString(R.string.smsdetail_after_hour),
//                            mins / 60)
//                    + word;
//        else
//            return String.format(
//                    context.getString(R.string.smsdetail_after_day), mins
//                            / (60 * 24))
//                    + word;
//
//    }

    public static boolean isToday(long dateMi) {
        Calendar old = Calendar.getInstance();
        old.setTimeInMillis(dateMi);
        Calendar now = Calendar.getInstance();

        Calendar old1 = Calendar.getInstance();
        old1.set(old.get(Calendar.YEAR), old.get(Calendar.MONTH),
                old.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        Calendar now1 = Calendar.getInstance();
        now1.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        int intervalDay = (int) ((now1.getTimeInMillis() - old1
                .getTimeInMillis()) / (1000 * 60 * 60 * 24));
        return intervalDay == 0;
    }

    public static boolean isMinute(String time1, String time2) {
        try {
            if (time1 == null || time2 == null) {
                return false;
            }
            int time1Length = time1.length();
            int time2Length = time2.length();
            if (time1Length != time2Length || time1Length < 13) {
                return false;
            }
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTimeInMillis(Long.parseLong(time1));
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTimeInMillis(Long.parseLong(time2));

            Calendar calendar11 = Calendar.getInstance();
            calendar11.set(calendar1.get(Calendar.YEAR), calendar1.get(Calendar.MONTH),
                    calendar1.get(Calendar.DAY_OF_MONTH), calendar1.get(Calendar.HOUR_OF_DAY), calendar1.get(Calendar.MINUTE), 0);

            Calendar calendar22 = Calendar.getInstance();
            calendar22.set(calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH),
                    calendar2.get(Calendar.DAY_OF_MONTH), calendar2.get(Calendar.HOUR_OF_DAY), calendar2.get(Calendar.MINUTE), 0);

            return calendar11.getTimeInMillis() == calendar22.getTimeInMillis();
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isYear(long dateMi) {
        Calendar old = Calendar.getInstance();
        old.setTimeInMillis(dateMi);
        Calendar now = Calendar.getInstance();
        return old.get(Calendar.YEAR) == now.get(Calendar.YEAR);
    }

//    public static String getTomorrowWeek(Context context) {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        String[] strs = context.getResources().getStringArray(
//                R.array.array_week_days);
//        return strs[cal.get(Calendar.DAY_OF_WEEK) - 1];
//    }


    public static boolean isBusinessTime() {
        //工作时间 8.30 ~  18:00
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if ((hour > 8 || (hour == 8 && minute > 29)) && hour < 18) {
            return true;
        }
        return false;
    }

    /*
     * 将时间转换为时间戳
     */
    public static long dateToStamp(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = simpleDateFormat.parse(s);
        return date.getTime();
    }
}
