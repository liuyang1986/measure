package win.lioil.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.BtReceiver;
import win.lioil.bluetooth.util.PermissionHelper;
import win.lioil.bluetooth.util.PermissionInterface;
import win.lioil.bluetooth.util.Permissions;

import static android.os.Build.VERSION_CODES.M;

public class BtClientActivity extends Activity implements BtBase.Listener, BtReceiver.Listener, BtDevAdapter.Listener, PermissionInterface {
    private static final String TAG = "Test BtClientActivity";
    private LinearLayout llBack;
    private TextView txtTitle;
    private TextView mTips;
//    private EditText mInputMsg;
//    private EditText mInputFile;
    private TextView mLogs;
    private BtReceiver mBtReceiver;
    private final BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);
    private final BtClient mClient = new BtClient(this);
    private PermissionHelper permissionHelper;
    private BtClientActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);

        // 检查蓝牙开关
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            APP.toast("本机没有找到蓝牙硬件或驱动！", 0);
            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
                //跳转到设置界面
                //startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
            }
        }

        // 检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            APP.toast("本机不支持低功耗蓝牙！", 0);
            finish();
            return;
        }

        mActivity = this;

        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mBtDevAdapter);
        txtTitle = findViewById(R.id.txt_title);

        llBack = findViewById(R.id.ll_back);
        mTips = findViewById(R.id.tv_tips);
        mLogs = findViewById(R.id.tv_log);
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= M) {
            permissionHelper = new PermissionHelper();
            permissionHelper.requestPermission(mActivity, BtClientActivity.this,
                    Permissions.PERMISSIONS_LOGIN, Config.PERMISSIONS_REQUEST_COMMON_CODE);
        }
        else
        {
            startScan();
        }
    }


    private void startScan()
    {
        mBtReceiver = new BtReceiver(this, this);//注册蓝牙广播
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mClient.unListener();
        mClient.close();
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev)) {
            APP.toast("已经连接了", 0);
            return;
        }
        mClient.connect(dev);
        APP.toast("正在连接...", 0);
        mTips.setText("正在连接...");
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        mBtDevAdapter.add(dev);
    }

    // 重新扫描
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }


    @Override
    public void socketNotify(int state, final Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                String message = String.format("\n%s", obj);
                mLogs.append(message);
                break;
            case BtBase.Listener.REC500:
               parseREC500Format((String)obj);
                break;
            case BtBase.Listener.M5:
                String m5Message = String.format("M5协议格式：%s\n", obj);
                mLogs.append(m5Message);
                break;
        }

        if (!TextUtils.isEmpty(msg))
        {
            APP.toast(msg, 0);
        }

    }

    private void parseREC500Format(String message)
    {
        //3个空格
        String prefix = message.substring(0,2);
        //4位地址 存储器地址 数字
        String WI = message.substring(3,6);
        //27位点识别 点识别14位 附加信息13位 数字/字母
        String PI_Number = message.substring(8,21);
        String PI_Extra = message.substring(22,34);
        //T1 2位类型识别 数字/字母
        String T1_Type = message.substring(36,37);
        //T1 12位数字
        String T1_Data = message.substring(38,49);
        //T2 2位类型识别 数字/字母
        String T2_Type = message.substring(51,52);
        //T2 13位数字
        String T2_Data = message.substring(53,65);
        //T3 2位类型识别 数字/字母
        String T3_Type = message.substring(67,68);
        //T3 9位数字
        String T3_Data = message.substring(69,77);

        Log.d(TAG,"T1数据块的类型：" + T1_Type + " , T1数据块的数据：" + T1_Data
            + " , T2数据块的类型：" + T2_Type + " , T2数据块的数据：" + T2_Data +
                " , T3数据块的类型：" + T3_Type + " , T3数据块的数据：" + T3_Data);
    }

    @Override
    public void onAfterApplyAllPermission(int requestCode) {
        if (requestCode == Config.PERMISSIONS_REQUEST_COMMON_CODE)
        {
            startScan();
        }
    }
}