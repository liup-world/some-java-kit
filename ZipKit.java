package com.bobsystem.exercise.commons;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipKit {

    //region zip
    public static boolean zip(String sourceFile, String destFile)
        throws IOException {
        return zip(new File(sourceFile), new File(destFile));
    }

    public static boolean zip(File sourceFile, File destFile)
        throws IOException {
        return zip(sourceFile, true, destFile);
    }

    public static boolean zip(File sourceFile, boolean KeepDirStructure, File destFile)
        throws IOException {
        //
        ZipOutputStream zipStream =
            new ZipOutputStream(new FileOutputStream(destFile));
        zip(sourceFile, "", KeepDirStructure, zipStream);
        zipStream.close();
        return destFile.exists();
    }

    /*########################################################################*/

    public static void zip(String sourceFile, ZipOutputStream zipStream)
        throws IOException {
        zip(new File(sourceFile), zipStream);
    }

    public static void zip(File sourceFile, ZipOutputStream zipStream)
        throws IOException {
        zip(sourceFile, true, zipStream);
    }

    public static void zip(File sourceFile, boolean KeepDirStructure, ZipOutputStream zipStream)
        throws IOException {
        zip(sourceFile, "", KeepDirStructure, zipStream);
    }

    /*
     * 递归读取文件夹中的文件 打成 .zip 包
     * @param sourceFile 目标文件夹
     * @param fullName 解压缩后文件夹的名字
     * @KeepDirStructure 是否保留原目录结构, true 保留
     */
    public static void zip(File sourceFile, String fullName,
                           boolean KeepDirStructure, ZipOutputStream zipStream)
        throws IOException {
        //
        if (sourceFile.isFile()) {
            //region 向 zip 输出流中添加一个 zip 实体，构造器中 fullName 为 zip 实体的文件的名字
            if (StringUtils.isBlank(fullName)) fullName = sourceFile.getName();
            ZipEntry entry = new ZipEntry(fullName);
            zipStream.putNextEntry(entry);
            entry.setComment(fullName);
            //endregion
            //region copy 文件到 zip 输出流中
            byte[] buffer = new byte[4096];
            int length;
            FileInputStream inStream = new FileInputStream(sourceFile);
            while ((length = inStream.read(buffer)) != -1){
                zipStream.write(buffer, 0, length);
            }
            inStream.close();
            //endregion
            zipStream.closeEntry();
            return;
        }
        File[] children = sourceFile.listFiles();
        if(children == null || children.length == 0) {
            // 需要保留原来的文件结构时，添加空文件夹 entry
            if (KeepDirStructure) {
                String path = fullName + "/";
                ZipEntry entry = new ZipEntry(path);
                entry.setComment(path);
                zipStream.putNextEntry(entry);
                zipStream.closeEntry();
            }
            return;
        }
        for (File file : children) {
            if (KeepDirStructure) { // 判断是否需要保留原来的文件结构
                // 注意：file.getName() 前面需要带上父文件夹的名字加一斜杠，
                // 不然最后压缩包中就不能保留原来的文件结构，即：所有文件都跑到压缩包根目录下了
                zip(file, fullName + "/" + file.getName(), true, zipStream);
            }
            else {
                zip(file, file.getName(), false, zipStream);
            }
        }
    }
    //endregion

    //region constructor
    private ZipKit() { }
    //endregion
}
