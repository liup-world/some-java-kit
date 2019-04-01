package com.bobsystem.exercise.commons;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/*
 * 转换汉语到拼音首字母
 * 注：只支持 GB2312 字符集中的汉字
 */
public class Pinyin {

    //region CONSTANT
    private static final Logger LOGGER = LoggerFactory.getLogger(Pinyin.class);
    private static final Pattern PATTERN_CHINESE = Pattern.compile("[\\u4E00-\\u9FA5]+");
    private static final String FULL_WIDTH_SIGN = "、，,：；。？·～！＠＃￥％…＆（）【】《》‘’“”＋—×÷｜";
    //private static final String HALF_WIDTH_SIGN = "\\,:;.?`~!@#$%^＆()[]<>''\"\"+-*/|";
    private static final String HALF_WIDTH_SIGN = "...:;.?`~!@#$%^＆()[]<>....+-*/|";
    //endregion

    /*
     * 得到 全拼
     */
    public static String getPinYin(String chinese) {
        HanyuPinyinOutputFormat pinyinOutFormat = new HanyuPinyinOutputFormat();
        pinyinOutFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        pinyinOutFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        pinyinOutFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder result = new StringBuilder();
        char[] chars = chinese.toCharArray();
        try {
            for (char ch : chars) {
                // 判断是否为汉字字符
                if (PATTERN_CHINESE.matcher(String.valueOf(ch)).find()) {
                    String[] arr = PinyinHelper.toHanyuPinyinStringArray(ch, pinyinOutFormat);
                    result.append(arr[0]);
                }
                else {
                    result.append(ch);
                }
            }
        }
        catch (BadHanyuPinyinOutputFormatCombination ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return result.toString();
    }

    /*
     * 得到中文首字母
     */
    public static String getHeadChar(String chinese) {
        StringBuilder result = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char ch : chars) {
            String[] arr = PinyinHelper.toHanyuPinyinStringArray(ch);
            if (arr != null) {
                result.append(arr[0].charAt(0));
            }
            else {
                int index = FULL_WIDTH_SIGN.indexOf(ch);
                if (index == -1) {
                    result.append(ch);
                }
                else {
                    result.append(HALF_WIDTH_SIGN.charAt(index));
                }
            }
        }
        return result.toString();
    }

    /*
     * 将字符串转移为 ASCII 码
     */
    public static String toASCIICode(String chinese) {
        StringBuilder result = new StringBuilder();
        byte[] bytes = chinese.getBytes();
        for (byte by : bytes) {
            result.append(Integer.toHexString(by & 0xff));
        }
        return result.toString();
    }
}
