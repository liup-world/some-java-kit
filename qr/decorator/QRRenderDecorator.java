package com.yy.yycloud.commons.qr.decorator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.yy.yycloud.commons.qr.QRKit;

import java.awt.image.BufferedImage;

/*
 * 具体构建角色 (制作二维码图片)
 */
public class QRRenderDecorator
    extends QRMakerDecorator {

    //region CONSTANT
    private static final int COLOR_BLACK = 0xFF000000; // 黑色（二维码背景色）
    private static final int COLOR_WHITE = 0xFFFFFFFF; // 白色（二维码背景色）
    //endregion

    public QRRenderDecorator() {
        super(null);
    }

    @Override
    public BufferedImage make(BarcodeFormat type, int width, int height,
                              String content, String comment)
        throws Exception {

        BitMatrix matrix = QRKit.encodeMatrix(type, width, height, content);
        //region 创建图像，并使用矩阵数据渲染 Bitmap 图片，分别设为黑（0xFFFFFFFF）白（0xFF000000）两色
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                image.setRGB(x, y, matrix.get(x, y) ? COLOR_BLACK : COLOR_WHITE);
            }
        }
        //endregion
        return image; // 此时生成的二维码 只有黑白两色，没有logo 没有文字
    }
}
