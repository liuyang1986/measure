package win.lioil.bluetooth.config;

public class Config {

//    public static final String SERVER_PREFIXX = "http://106.14.142.42:8080/";

    public static final String SERVER_PREFIXX = "http://47.103.60.41:23375/";

    /**
     * 开打手机设置的请求码
     */
    public static final int SETTINGS_REQUEST_CODE = 200;

    /**
     * 小tips:这里的int数值不能太大，否则不会弹出请求权限提示，测试的时候,改到1000就不会弹出请求了
     * 权限申请通用requestcode
     */
    public static final int PERMISSIONS_REQUEST_COMMON_CODE = 201;

    //前后视距差
    public static final String FB_HD_DELTA = "1.5";
    //累积视距差
    public static final String FB_HD_SUM = "6.0";
    //前后两次读数差
    public static final String NEAR_VD_DELTA = "0.4";
    //高差之差
    public static final String NEAR_HD_DELTA = "0.6";
    //最小视线高
    public static final String MIN_SIGHT_HEIGHT = "0.5";
    //最大视线高
    public static final String MAX_SIGHT_HEIGHT = "1.8";

    //工作基点类型
    public static final String BASE_POINT_TYPE = "1";

    //测点类型
    public static final String MEASURE_POINT_TYPE = "2";

    //拐点类型或者辅助点类型
    public static final String ASSIST_POINT_TYPE = "3";

    //中间点
    public static final String MIDWAY_POINT_TYPE = "4";

    //奇数点模式
    public static final int ODD_MEASURE_POINT_MODE = 1;

    //偶数点模式
    public static final int EVEN_MEASURE_POINT_MODE = 0;

    //项目类型
    public static final int XM_TYPE = 0;

    //标段类型
    public static final int BD_TYPE = 1;

    //工点类型
    public static final int GD_TYPE = 2;

    //路基断面类型
    public static final int SUBGRADE_TYPE = 3;

    //工点类型
    public static final int ALL_GD_TYPE = 4;

    public static String AUTH_TOKEN = "";

    public static String AUTH_XM = "";

    //前后两次视距差
    public static String THRESHOLD_SETTING_FB_HD_DELTA = "";

    //累积视距差
    public static String THRESHOLD_SETTING_FB_HD_SUM = "";

    //前后两次读数差
    public static String THRESHOLD_SETTING_NEAR_R_DELTA = "";

    //高差之差
    public static String THRESHOLD_SETTING_NEAR_H_DELTA = "";

    //最小视线高
    public static String THRESHOLD_SETTING_MIN_SIGHT_HEIGHT = "";

    //最大视线高
    public static String THRESHOLD_SETTING_MAX_SIGHT_HEIGHT = "";
}
