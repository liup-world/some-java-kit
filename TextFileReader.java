package com.bobsystem.exercise.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.LinkedList;
import java.util.List;

/**
 * 批量导入客户 解析txt文件
 */
public class TextFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextFileReader.class);

    public static List<String> read(InputStream inStream) {
        List<String> result = new LinkedList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
        return result;
    }
}
//public static List<String> read(CommonsMultipartFile mfile, HttpServletRequest request) throws IOException {
//    String temporary = request.getSession().getServletContext().getRealPath("/temporary");
//    File file = new File(temporary);
//    if (!file.exists()) file.mkdirs();
//    File newfile = new File(temporary + ".txt");
//
//    //将上传的文件写入新建的文件中
//    try {
//        mfile.getFileItem().write(newfile);
//    } catch (Exception ex) {
//        ex.printStackTrace();
//    }
//
//    List<String> codes = new ArrayList<>();
//
//    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newfile)));
//    String str = null;
//    while((str = reader.readLine()) != null) {
//        if(str.length() < 15 || str.length() > 20) {
//            return null;
//        }
//        codes.add(str);
//    }
//    reader.close();
//    if(codes.size() > 0) {
//        return codes;
//    }
//    return null;
//}
