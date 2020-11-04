package win.lioil.bluetooth.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.config.Config;

/**
 * Created by Bakura .
 * 2017/7/24
 * 临时测试用权限工具类
 * 此工具类方法需在SDK23及以上版本调用，23以下无需动态权限申请
 */

public class PermissionHelper {


  private int mRequestCode;

  public PermissionHelper() {}

  //  调用前需要先进行SDK版本判断，需要在SDK23以上才适用动态权限获取
  @RequiresApi(api = Build.VERSION_CODES.M)
  public void requestPermission(Activity activity, PermissionInterface aferRequestPermissions, String[] requiredPermissions, int requestCode) {
    this.mRequestCode = requestCode;
    List<String> permissonsNeeded = new ArrayList<String>();
    for (String permission : requiredPermissions) {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, permission)) {
        permissonsNeeded.add(permission);
      }
    }
    if(!permissonsNeeded.isEmpty()) {
      String[] permissions = permissonsNeeded.toArray(new String[permissonsNeeded.size()]);
      ActivityCompat.requestPermissions(activity, permissions, requestCode);
    } else {
      aferRequestPermissions.onAfterApplyAllPermission(mRequestCode);
    }

  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, PermissionInterface aferRequestPermissions, final Activity activity) {
    if (requestCode != 0) {
      if (isAllRequestedPermissionGranted(activity, permissions)) {//如果权限全部被允许，继续执行下步操作
        aferRequestPermissions.onAfterApplyAllPermission(mRequestCode);
      } else {//如果没有全部允许
        List<String> permissionDenied = new ArrayList<String>();
        for (int i = 0; i < grantResults.length; i++) {
          if (grantResults[i] != 0) {
            if (!activity.shouldShowRequestPermissionRationale(permissions[i])) {//有权限被拒绝并勾选了“不再提示”
                //弹出dialog，须进入设置中心设置权限方可继续。
                openApplicationSettings(activity, permissions[i]);
                return;
            } else {
              permissionDenied.add(permissions[i]);
            }
          }
        }
        if (permissionDenied.size() != 0) {//对拒绝的权限再次申请
          requestPermission(activity, aferRequestPermissions, permissionDenied.toArray(new String[permissionDenied.size()]), Config.PERMISSIONS_REQUEST_COMMON_CODE);
        } else {
          aferRequestPermissions.onAfterApplyAllPermission(mRequestCode);
        }

      }
    }
  }

  /**
   * 打开设置中心
   */
  private void openApplicationSettings(final Activity activity, String permission) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
      .setTitle("权限申请")
      .setMessage("请在打开的窗口的权限中开启" + permission + "权限，以正常使用本应用")
      .setPositiveButton("设置", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
          intent.addCategory(Intent.CATEGORY_DEFAULT);
          activity.startActivityForResult(intent, Config.SETTINGS_REQUEST_CODE);
        }
      })
      .setNegativeButton("取消", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          activity.finish();
        }
      });
    builder.setCancelable(false);
    builder.show();

  }

  //  用于判断权限是否已经全部允许
  private boolean isAllRequestedPermissionGranted(Activity activity, String[] requestPermissions) {
    for (String requestPermission : requestPermissions) {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, requestPermission)) {
        return false;
      }
    }
    return true;
  }

  private String permissionNotGranted(Activity activity, String[] requestPermissions)
  {
    for (String requestPermission : requestPermissions) {
      if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, requestPermission)) {
        return requestPermission;
      }
    }
    return "";
  }

  /**
   * 设置中心回来的方法
   */
  public void onActivityResult(int requestCode, Activity mActivity, PermissionInterface aferRequestPermissions, String[] mPermissions) {
    if (requestCode == Config.SETTINGS_REQUEST_CODE)
    {
      if(isAllRequestedPermissionGranted(mActivity, mPermissions)) {
        aferRequestPermissions.onAfterApplyAllPermission(mRequestCode);
      } else {
        //Toast.makeText(mActivity, "部分必须权限未被允许，无法开启应用", Toast.LENGTH_SHORT).show();
        if (!TextUtils.isEmpty(permissionNotGranted(mActivity,mPermissions)))
        {
          openApplicationSettings(mActivity,permissionNotGranted(mActivity,mPermissions));
        }
      }
    }




  }

}
