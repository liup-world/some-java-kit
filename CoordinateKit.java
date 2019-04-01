package com.bobsystem.exercise.commons;

public class CoordinateKit {

    //region CONSTANT
    private final static double PI = 3.14159265358979324;
    private final static double A = 6378245.0;
    private final static double EE = 0.00669342162296594323;
    //endregion

    //region member methods
    //region 纠偏
    public static double[] correct(double latitude, double longitude) {
        double[] result = new double[2];
        if (overseas(latitude, longitude)) {
            result[0] = latitude;
            result[1] = longitude;
            return result;
        }
        double correctLat = correctLat_(longitude - 105.0, latitude - 35.0);
        double correctLon = correctLon_(longitude - 105.0, latitude - 35.0);

        double radiusLat = latitude / 180.0 * PI;
        double magic = Math.sin(radiusLat);

        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        correctLat = (correctLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        correctLon = (correctLon * 180.0) / (A / sqrtMagic * Math.cos(radiusLat) * PI);

        result[0] = latitude + correctLat;
        result[1] = longitude + correctLon;
        return result;
    }
    //endregion

    /*
     * 度分秒格式 转换为 小数经纬度
     *  @param degree 3158.4608,11848.3737
     *  @return 小数经纬度
     */
    public static double convert(double degree) {
        int x = (int)(degree / 100);
        double y = (degree - x * 100) / 60d;
        return Commons.reserveDecimal(x + y, 8);
    }

    /*
     * 经纬度 转 度分秒格式
     */
    public static double toDegree(double coord) {
        int x = (int)coord;
        double y = (coord - x) * 60;
        return x * 100 + y;
    }

    //region private methods
    private static double correctLat_(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double correctLon_(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /*
     * 境外
     */
    private static boolean overseas(double latitude, double longitude) {
        if (longitude < 72.004 || longitude > 137.8347)
            return true;
        return (latitude < 0.8293 || latitude > 55.8271);
    }
    //endregion
    //endregion

    //region constructor
    private CoordinateKit() { }
    //endregion
}
