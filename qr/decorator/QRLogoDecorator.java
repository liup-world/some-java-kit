package com.yy.yycloud.commons.qr.decorator;

import com.google.zxing.BarcodeFormat;
import com.yy.yycloud.commons.SystemParameter;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

// 具体装饰角色（为二维码添加logo）
public class QRLogoDecorator
    extends QRMakerDecorator {

    private final File logoFile;

    public QRLogoDecorator(QRMaker generator, File logoFile) {
        super(generator);
        this.logoFile = logoFile;
    }

    @Override
    public BufferedImage make(BarcodeFormat type, int width, int height,
                              String content, String comment)
        throws Exception {

        BufferedImage image = super.make(type, width, height, content, comment);
        if (image == null) return null;
        if (this.logoFile == null) return image;
        // 读取Logo图片
        BufferedImage logoImage = ImageIO.read(this.logoFile);
        int logoWidth = logoImage.getWidth(); // logo 宽度
        int logoHeight = logoImage.getHeight(); // logo 高度
        //region 计算 logo 尺寸，原尺寸宽和高中较大的一边，最大不超过 二维码的 1/5
        float rate;
        if (logoWidth > logoHeight) {
            float size = (logoWidth > width / 5 ? (width / 5) : logoWidth);
            rate = size / logoWidth;
        }
        else {
            float size = (logoHeight > height / 5 ? (height / 5) : logoHeight);
            rate = size / logoHeight;
        }
        logoWidth *= rate;
        logoHeight *= rate;
        //endregion
        // 计算中心坐标
        int centerX = (width - logoWidth) / 2;
        int centerY = (height - logoHeight) / 2;
        // 开始绘制 logo，此时生成的二维码 是有logo 的
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(
            logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH),
            centerX, centerY, logoWidth, logoHeight, null);
        graphics.dispose();
        logoImage.flush();
        image.flush();
        return image;
    }
}
