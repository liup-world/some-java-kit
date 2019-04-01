package com.yy.yycloud.commons.qr.decorator;

import com.google.zxing.BarcodeFormat;

import java.awt.image.BufferedImage;

// 抽象构建角色
public interface QRMaker {

    /*
     * 制作二维码
     * @param content 二维码的地址 ：http://www .....
     * @param width 二维码宽度
     * @param height 二维码高度
     * @param comment 注释内容
     */
    BufferedImage make(BarcodeFormat type, int width, int height,
                       String content, String comment)
        throws Exception;
}