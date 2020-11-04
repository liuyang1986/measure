package win.lioil.bluetooth.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.BDBean;
import win.lioil.bluetooth.bean.GDBean;
import win.lioil.bluetooth.bean.TabItem;
import win.lioil.bluetooth.bean.WorkPointBean;
import win.lioil.bluetooth.bean.XMBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.fragment.BaseFragment;
import win.lioil.bluetooth.fragment.HomeFragment;
import win.lioil.bluetooth.fragment.MeasureFragment;
import win.lioil.bluetooth.fragment.MineFragment;
import win.lioil.bluetooth.util.PermissionHelper;
import win.lioil.bluetooth.util.PermissionInterface;
import win.lioil.bluetooth.util.Permissions;
import win.lioil.bluetooth.util.ShareRefrenceUtil;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.TabLayout;

import static android.os.Build.VERSION_CODES.M;


public class HomeActivity extends BaseActivity implements PermissionInterface,TabLayout.OnTabClickListener{

    private static final String TAG = "Test HomeActivity";

    private PermissionHelper permissionHelper;

    private HomeActivity mActivity;

    private ProgressDialog mProgressDialog;

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private List<Fragment> fragmentList = new ArrayList<>();

    private static final int PAGE_SIZE = 400;

    private int size = 0;

    private ArrayList<TabItem> tabs;
    BaseFragment fragment;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Config.XM_TYPE:
                    Log.d(TAG, "request project finished");
                    size++;
                    break;
                case Config.BD_TYPE:
                    Log.d(TAG, "request section finished");
                    size++;
                    break;
                case Config.GD_TYPE:
                    Log.d(TAG, "request workpoint finished");
                    size++;
                    break;
                case Config.ALL_GD_TYPE:
                    Log.d(TAG, "request all workpoint finished");
                    size++;
                    break;
                default:
                    break;
            }

            if (size == 4)
            {
                dismissWaitingDialog();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        mActivity = this;
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

        if (android.os.Build.VERSION.SDK_INT >= M) {
            permissionHelper = new PermissionHelper();
            permissionHelper.requestPermission(mActivity, HomeActivity.this,
                    Permissions.PERMISSIONS_LOGIN, Config.PERMISSIONS_REQUEST_COMMON_CODE);
        } else {
            requestAllData();
        }

        readThresholdSettings();
    }

    public void readThresholdSettings()
    {
        Config.THRESHOLD_SETTING_FB_HD_DELTA = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"fb_hd_delta"))?
                Config.FB_HD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"fb_hd_delta"));

        Config.THRESHOLD_SETTING_FB_HD_SUM = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"fb_hd_sum"))?
                Config.FB_HD_SUM:((String)ShareRefrenceUtil.get(mActivity,"fb_hd_sum"));

        Config.THRESHOLD_SETTING_NEAR_R_DELTA = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"near_vd_delta"))?
                Config.NEAR_VD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"near_vd_delta"));

        Config.THRESHOLD_SETTING_NEAR_H_DELTA = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"near_hd_delta"))?
                Config.NEAR_HD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"near_hd_delta"));

        Config.THRESHOLD_SETTING_MIN_SIGHT_HEIGHT = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"min_hd"))?
                Config.MIN_SIGHT_HEIGHT:((String)ShareRefrenceUtil.get(mActivity,"min_hd"));

        Config.THRESHOLD_SETTING_MAX_SIGHT_HEIGHT = TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"max_hd"))?
                Config.MAX_SIGHT_HEIGHT:((String)ShareRefrenceUtil.get(mActivity,"max_hd"));
    }

    private void initView() {
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);

        tabs=new ArrayList<>();
        tabs.add(new TabItem(R.drawable.tablayout_home_icon,R.string.bottom_nav_home, HomeFragment.class));
        tabs.add(new TabItem(R.drawable.tablayout_measure_icon,R.string.bottom_nav_measure, MeasureFragment.class));
        tabs.add(new TabItem(R.drawable.tablayout_mine_icon,R.string.bottom_nav_mine, MineFragment.class));
        mTabLayout.initData(tabs, this);
        mTabLayout.setCurrentTab(0);
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onAfterApplyAllPermission(int requestCode) {
        requestAllData();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null ) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults,
                    this, this);
        }
    }

    private void showWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(HomeActivity.this);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage("查询中，请稍等...");
                }
                mProgressDialog.show();
            }
        });
    }

    private void dismissWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    //请求登录用户所在的所有项目
    private void requestXMData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                List<XMBean> projectBeanList = new ArrayList<>();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "project/selectProjectByUserId";
                    String requestJSon = jsonObject.toString();
                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "requestXMData responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                XMBean bean = JSON.parseObject(list.getJSONObject(i).toString(), XMBean.class);
                                projectBeanList.add(bean);
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "requestXMData failed , response code is " + response.code());
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "requestXMData + "+e.getMessage());
                } finally {
                    Util.setAppCacheProjectListData(projectBeanList);
                    Message msg = new Message();
                    msg.what = Config.XM_TYPE;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    //请求登录用户所在的所有标段
    private void requestBDData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                List<BDBean> sectionBeanList = new ArrayList<>();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", 0);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "bidSection/selectByUserAcc";
                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "requestBDData responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = dataJSON.getJSONArray("list");

                            for (int i = 0; i < list.size(); i++) {
                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
                                sectionBeanList.add(bean);
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "requestBDData failed , response code is " + response.code());
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "requestBDData + "+e.getMessage());

                } finally {
                    Util.setAppCacheSectionListData(sectionBeanList);
                    Message msg = new Message();
                    msg.what = Config.BD_TYPE;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    //请求登录用户所在的所有工点
    private void requestGDData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                List<GDBean> workPointList = new ArrayList<>();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "workPoints/selectWorkByUser";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "requestGDData responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");

                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
                                workPointList.add(bean);
                            }

                        }
                    }
                    else
                    {
                        Log.e(TAG, "requestGDData failed , response code is " + response.code());
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "requestGDData + "+e.getMessage());
                } finally {
                    Util.setAppCacheWorkPointListData(workPointList);
                    Message msg = new Message();
                    msg.what = Config.GD_TYPE;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    //请求登录用户所在的所有工点
    private void requestAllGDData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                List<WorkPointBean> workPointList = new ArrayList<>();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    String localURL = Config.SERVER_PREFIXX + "workPoints/showWorkPointsByUser";
                    String requestJSon = jsonObject.toString();
                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "requestAllGDData responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONArray list = responseJson.getJSONArray("data");
                            for (int i = 0; i < list.size(); i++) {
                                WorkPointBean bean = JSON.parseObject(list.getJSONObject(i).toString(), WorkPointBean.class);
                                workPointList.add(bean);
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "requestAllGDData failed , response code is " + response.code());
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "requestAllGDData "+e.getMessage());
                } finally {
                    Util.setAllWorkPointListData(workPointList);
                    Message msg = new Message();
                    msg.what = Config.ALL_GD_TYPE;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void requestAllData() {
        showWaitingDialog();
        requestXMData();
        requestBDData();
        requestGDData();
        requestAllGDData();
    }

    @Override
    public void onTabClick(TabItem tabItem) {
        mViewPager.setCurrentItem(tabs.indexOf(tabItem));
    }



    public class FragAdapter extends FragmentPagerAdapter {


        public FragAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            try {
                return tabs.get(arg0).tagFragmentClz.newInstance();
            } catch (InstantiationException e) {
                Log.e(TAG,e.getMessage());
            } catch (IllegalAccessException e) {
                Log.e(TAG,e.getMessage());
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

    }
}
