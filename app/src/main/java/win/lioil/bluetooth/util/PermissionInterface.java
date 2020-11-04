package win.lioil.bluetooth.util;

/**
 * 作者：wwl on 2017/8/2 16：37.
 * 邮箱：wwl198800@163.com
 * 电话：18600868377
 */

public interface PermissionInterface {

  /**
   * 申请所有权限之后的逻辑
   */
  void onAfterApplyAllPermission(int requestCode);

}
