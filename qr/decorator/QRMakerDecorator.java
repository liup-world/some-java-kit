package com.yy.yycloud.commons.qr.decorator;

import com.google.zxing.BarcodeFormat;

import java.awt.image.BufferedImage;

// 装饰角色
public abstract class QRMakerDecorator
    implements QRMaker {

    private QRMaker maker;

    public QRMakerDecorator(QRMaker maker) {
        this.maker = maker;
    }

    @Override
    public BufferedImage make(BarcodeFormat type, int width, int height,
                              String content, String comment)
        throws Exception {
        if (this.maker != null) {
            return maker.make(type, width, height, content, comment);
        }
        return null;
    }
}
