package com.bobsystem.exercise.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Field;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemParameter {

    //region CONSTANT
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemParameter.class);

    public static final String SESSION_LOGIN_INFO_KEY = "LoginInfo";
    public static final String SESSION_LOGIN_CUSTOMTER_KEY = "CustomerInfo";

    public static final String SERIALIZATION_PARENT = "/upload-files/serialization";

    private static final String PROPERTIES_NAME = "/system.properties";
    private static final Pattern PATTERN_PROP_VAR = Pattern.compile("\\$\\{([^\\}\\s]+)\\}");
    private static final Map<String, String> MAP_FIELDS = new HashMap<>();
    //endregion

    // 每次发布前更新
    public static final String APP_VERSION = "19.03.09";

    //region enviroment paprameter
    public static String rootPath;
    public static long startupTime = Long.MAX_VALUE;
    public static boolean devMode;
    //endregion

    //region 系统参数
    public static String appName;
    public static String appDescription;
    public static String appKeywords;
    public static String appCopyright;
    public static String appAuthor;
    public static String appRecordCode;
    public static String appRecordYear;
    //endregion


    //region 高德地图
    public static String amapKey;
    public static String mapCoord;
    //endregion

    //region Redis
    public static String redisHost;
    public static int redisPort;
    public static String redisPassword;
    public static int redisMaxTotal;
    public static int redisMaxIdle;
    public static int redisMinIdle;
    //region business
    public static long redisMaxExpire;
    public static long redisEquipOnlineStatusExpire;
    //endregion
    //endregion

    //region HTTP
    public static int httpMaxTotal;
    //endregion

    //region 通过反射自动从配置中加载值到对应的参数字段上
    static {
        //region 读取 system.properties 文件到 输入流
        Properties properties = new Properties();
        InputStream inStream = SystemParameter.class.getResourceAsStream(PROPERTIES_NAME);
        if (inStream == null) {
            Error error = new ExceptionInInitializerError(PROPERTIES_NAME);
            LOGGER.error(error.getMessage(), error);
            throw error;
        }
        //endregion
        //region 从流加载到 Properties 对象
        try {
            properties.load(inStream);
        }
        catch (Exception ex) {
            Error error = new ExceptionInInitializerError(ex);
            LOGGER.error(ex.getMessage(), ex);
            throw error;
        }
        finally {
            //region inStream.close()
            try {
                inStream.close();
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
            //endregion
        }
        //endregion
        //region 遍历每一个属性，从 properties 对象到 Map<String, String>
        Enumeration enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            try {
                String key = enumeration.nextElement().toString();
                //region 构造每个属性的名字到变量名
                StringBuilder name = new StringBuilder();
                String[] segments = key.split("\\.");
                for (String str : segments) {
                    if (name.length() != 0) {
                        String first = String.valueOf(str.charAt(0));
                        name.append(str.replaceFirst(first, first.toUpperCase()));
                    }
                    else { // 第一段首字母不需要大写
                        name.append(str);
                    }
                }
                //endregion
                //region 解析属性值中的变量名
                String value = properties.getProperty(key);
                Matcher matcher = PATTERN_PROP_VAR.matcher(value);
                if (matcher.find()) {
                    value = resolveVariable_(matcher, properties);
                }
                MAP_FIELDS.put(name.toString(), value);
                //endregion
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        //endregion
        //region 将读取到的值设置到对应的参数中
        Class clazz = SystemParameter.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                //field.setAccessible(true);
                Class fieldType = field.getType();
                Object value = MAP_FIELDS.get(fieldName);
                if (value != null) {
                    if (fieldType == String.class) {
                        field.set(fieldName, value);
                        continue;
                    }
                    if (fieldType == boolean.class || fieldType == Boolean.class) {
                        field.set(fieldName, "true".equals(value));
                        continue;
                    }
                    if (fieldType == int.class || fieldType == Integer.class) {
                        String str = value.toString();
                        if (str.length() != 0) {
                            field.set(fieldName, Integer.valueOf(str));
                        }
                        continue;
                    }
                    if (fieldType == long.class || fieldType == Long.class) {
                        String str = value.toString();
                        if (str.length() != 0) {
                            field.set(fieldName, Long.valueOf(str));
                        }
                        //continue;
                    }
                }
            }
            catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        //endregion
    }

    // 解析属性值中的变量名，支持变量值再引用变量
    private static String resolveVariable_(Matcher matcher, Properties properties) {
        StringBuffer strBuilder = new StringBuffer();
        matcher.reset();
        while (matcher.find()) {
            String var = matcher.group(1);
            String val = properties.getProperty(var);
            Matcher matcher2 = PATTERN_PROP_VAR.matcher(val);
            if (matcher2.find()) {
                val = resolveVariable_(matcher2, properties);
            }
            matcher.appendReplacement(strBuilder, Matcher.quoteReplacement(val));
        }
        matcher.appendTail(strBuilder);
        return strBuilder.toString();
    }

    public static String get(String variable) {
        return MAP_FIELDS.get(variable);
    }
    //endregion
}

