package com.yy.yycloud.commons.qr.decorator;

import com.google.zxing.BarcodeFormat;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// 具体装饰角色(添加注释)
public class QRCommentDecorator
    extends QRMakerDecorator {

    //region CONSTANT
    private static final int SPACING = 5;
    private static final Font FONT_COMMENT = new Font("verdana", Font.PLAIN, 24);
    //endregion

    public QRCommentDecorator(QRMaker maker) {
        super(maker);
    }

    /*
     * @param comment 要添加的注释内容
     * @return 添加了注释的 BufferedImage 对象
     */
    @Override
    public BufferedImage make(BarcodeFormat type, int width, int height,
                              String content, String comment)
        throws Exception {

        BufferedImage image = super.make(type, width, height, content, comment);
        if (image == null) return null;
        int newHeight = height + 10 + SPACING;
        // param3：表示具有 32 位 RGBA 颜色组件的图像
        BufferedImage newImage = new BufferedImage(width, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = newImage.createGraphics();
        // 设置消除锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setBackground(Color.WHITE); // 设置背景色
        graphics.clearRect(0, 0, width, newHeight);
        // 将原二维码绘制到新的画板
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.setColor(Color.BLACK); // 画文字到新的面板
        graphics.setFont(FONT_COMMENT);
        // 计算文本在当前字体下的宽度
        int textWidth = graphics.getFontMetrics().stringWidth(comment);
        graphics.drawString(comment, (width - textWidth) / 2, height + SPACING); // 画文字
        graphics.dispose();
        newImage.flush();
        return newImage;
    }
}
