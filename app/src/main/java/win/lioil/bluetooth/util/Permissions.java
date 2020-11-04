package win.lioil.bluetooth.util;

import android.Manifest;


/**
 * 作者：wwl on 2017/7/19 16：55.
 * 邮箱：wwl198800@163.com
 * 电话：18600868377
 */

public class Permissions {

    /**
     * 首次进入应用页面，LoginActivity需要申请的权限
     */
    public static String[] PERMISSIONS_LOGIN = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /**
     * 调用相机或相册
     */
    public static String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    /**
     * 调用相机录像
     */
    public static String[] PERMISSIONS_RECORD = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };


    /**
     * 拨打电话
     */
    public static String[] PERMISSIONS_CALL = new String[]{
            Manifest.permission.CALL_PHONE
    };
    /**
     * 获取地理位置
     */
    public static String[] PERMISSIONS_LOCATION = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    /**
     * 调用二维码(同时需要定位和相机)
     */
    public static String[] PERMISSIONS_QRCODE = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };


}
