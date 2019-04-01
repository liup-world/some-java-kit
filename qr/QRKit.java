package com.bobsystem.exercise.commons.qr;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public final class QRKit {

    private static final int COLOR_FRONT = 0xFF000000;
    private static final int COLOR_BACKGROUND = 0xFFFFFFFF;

    //region write
    /*
     * 输出到文件
     */
    public static void write(BitMatrix matrix, String format, File file)
        throws IOException {
        BufferedImage image = render_(matrix);
        if (!ImageIO.write(image, format, file)) {
            throw new IOException(String.format(
                "Could not write an image of format %s to %s.", format, file));
        }
    }

    /*
     * 输出到流(文件、页面)
     */
    public static void write(BitMatrix matrix, String format, OutputStream stream)
        throws IOException {
        BufferedImage image = render_(matrix);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

    //region private methods
    /*
     * 将矩阵数据转换为 Image 对象
     */
    private static BufferedImage render_(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? COLOR_FRONT : COLOR_BACKGROUND);
            }
        }
        return image;
    }
    //endregion
    //endregion

    //region make
    //region make to file
    public static void make(String content, int width, int height, File file)
        throws Exception {
        make(BarcodeFormat.QR_CODE, width, height, content, file);
    }

    public static void make(BarcodeFormat type, int width, int height, String content,
                            File file)
        throws Exception {
        BitMatrix matrix = encodeMatrix(type, width, height, content);
        write(matrix, "png", file); // 写到指定文件
     }
    //endregion

    //region make to stream
    public static void make(int width, int height, String content, OutputStream stream)
        throws Exception {
        make(BarcodeFormat.QR_CODE, width, height, content, stream);
    }

    public static void make(BarcodeFormat type, int width, int height, String content,
                            OutputStream stream)
        throws Exception {
        BitMatrix matrix = encodeMatrix(type, width, height, content);
        write(matrix, "png", stream); // 写到指定流
    }
    //endregion
    //endregion

    //region encode matrix
    public static BitMatrix encodeMatrix(BarcodeFormat type, int width, int height,
                                         String content)
        throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        MultiFormatWriter writer = new MultiFormatWriter();
        return writer.encode(content, type, width, height, hints);
    }
    //endregion

    //region constructors
    private QRKit() { }
    //endregion
}
