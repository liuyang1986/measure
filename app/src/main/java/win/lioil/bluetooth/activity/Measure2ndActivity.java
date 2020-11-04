package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Jama.Matrix;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.In1FileBean;
import win.lioil.bluetooth.bean.MeasureInfoBean;
import win.lioil.bluetooth.bean.MeasurePointBean;
import win.lioil.bluetooth.bean.MeasureStationBean;
import win.lioil.bluetooth.bt.BtBase;
import win.lioil.bluetooth.bt.BtClient;
import win.lioil.bluetooth.bt.BtDevAdapter;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Arith;
import win.lioil.bluetooth.util.BtReceiver;
import win.lioil.bluetooth.util.CheckIDCardRule;
import win.lioil.bluetooth.util.MatrixOperation;
import win.lioil.bluetooth.util.Ou1FileUtil;
import win.lioil.bluetooth.util.PermissionHelper;
import win.lioil.bluetooth.util.PermissionInterface;
import win.lioil.bluetooth.util.Permissions;
import win.lioil.bluetooth.util.ShareRefrenceUtil;
import win.lioil.bluetooth.util.Util;

import static android.os.Build.VERSION_CODES.M;

public class Measure2ndActivity extends Activity implements BtBase.Listener, BtReceiver.Listener, BtDevAdapter.Listener, PermissionInterface {
    private static final String TAG = "Test Measure2ndActivity";

    private static final String SPILT_LINE = "------------------------------------------------\n";
    private static final String SWITCH_LINE = "\n";
    private LinearLayout llBack;
    private TextView txtTitle;
    private Button btnMeasureMethod, btnConnectBlueTooth, btnClearAll, btnSaveData;
    private Button btnDeviceNo, btnTemperature, btnPressure, btnOperaterName,
            btnOperaterIdNum, btnInstrumentBrand, btnInstrumentModel;
    private Spinner spinnerWeather;
    private LinearLayout llAllMeasureData;
    private Button btnResult, btnCancel;
    private TextView txtRb1, txtRbHD1, txtRb4, txtRbHD4, txtRf5, txtRfHD5, txtRf8, txtRfHD8;
    private TextView txtRb6, txtRbHD6, txtRb7, txtRbHD7, txtRf2, txtRfHD2, txtRf3, txtRfHD3;
    private TextView txtRd1, txtDeltaRd1;
    private TextView txtRd2, txtDeltaRd2;

    //中间点测量值
    private TextView txtRz;
    //中间点视距
    private TextView txtHD;
    //中间点高程
    private TextView txtRd;
    //中间点高差
    private TextView txtDaltaRd;

    private double lastRb;

    private BtReceiver mBtReceiver;
    private final BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);
    private final BtClient mClient = new BtClient(this);
    private PermissionHelper permissionHelper;
    private Measure2ndActivity mActivity;
    private AlertDialog bluetoothDialog;

    private ProgressDialog mProgressDialog;

    //水准线路主键id
    private String levelingLineId;

    //水准线路编号
    private String levelingLineNo;

    //工作基点主键id
    private String workPointId;

    //查询返回的测点列表
    private List<MeasurePointBean> measurePointBeanList;
    //查询返回的已知点列表
    private List<MeasurePointBean> baseMPBList = new ArrayList<>();

    private List<In1FileBean> in1BeanList = new ArrayList<>();
    //拐点索引
    private int gdIndex = 0;

    //测量模式 BFFB还是FBBF
    private int measureMode = Config.ODD_MEASURE_POINT_MODE;

    //是否中间点测量
    private boolean isMiddleMode = false;

    //是否单点测量方式
    private boolean isSingleMeasureMode = false;

    //是否奇数次
    private boolean isOddTimes = false;

    private int measureTime = 0;

    //当前操作（测量以及更新）的测点在测点列表的序列
    private int currentMeasurePos = 0;

    //当前需要顺序测量的测点在查询返回的测点列表序列
    //当前需要顺序初始化测点视图的徐柳
    private int measurePointIndex = 0;

    //当前选择的测量点
    private MeasurePointBean currentMPB;

    //是否往测
    private boolean isReturnMeasure = false;

    private String hd1 = "0.0", hd2 = "0.0", hd3 = "0.0", hd4 = "0.0";

    private String rb1 = "0.0", rb2 = "0.0", rf1 = "0.0", rf2 = "0.0";

    private double refRb = 0.0;

    //测段列表
    private List<MeasureStationBean> measureStationBeanList = new ArrayList<>();

    //测段信息,需要保存的，包含两个测点
    private MeasureStationBean mMeasureStationBean;

    //往测起始已知基点信息
    private MeasurePointBean startMPB;

    //往测终点已知基点信息
    private MeasurePointBean endMPB;

    //测点列表
    private List<MeasurePointBean> selectedMPBList = new ArrayList<>();

    //缓存的测点列表
    private List<MeasurePointBean> cacheMPBList = new ArrayList<>();
    //缓存的测段列表
    private List<MeasureStationBean> cacheMSBList = new ArrayList<>();

    //当前测量点高值
    private double lastHeight = 0.0;

    private boolean isMeasureFinished = false;

    private double sumSightDis = 0.0;

    private int measureNum = 0;

    private MeasureInfoBean mMeasureInfoBean = new MeasureInfoBean();

    //起始已知点高程
    private double startHeight = 0.0;

    //结束已知点高程
    private double endHeight = 0.0;

    private List<String> nameList = new ArrayList<>();

    //测量记录名称
    private String measureRecordName = "";

    //允许闭合差
    private double fh_threshold = 0.0;

    private AlertDialog resultDialg;

    //缓存的本地文件列表
    private ArrayList<String> previewFileNameList = new ArrayList<>();

    //本地DAT文件名称
    private String datFileName = "";

    //中间点起始点站点名称
    private String startStationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
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

        Intent intent = getIntent();
        if (intent != null) {
            levelingLineId = intent.getStringExtra("levelingLineId");
            workPointId = intent.getStringExtra("workPointId");
            levelingLineNo = intent.getStringExtra("levelingLineNo");
            mMeasureInfoBean = (MeasureInfoBean) intent.getSerializableExtra("measureInfoBean");
        }
//        else
//        {
//            levelingLineId = "dfy238jd9812v09j";
//            workPointId = "120cfl982jn8287s";
//            levelingLineNo = "";
//        }


        if (mMeasureInfoBean == null) {
            mMeasureInfoBean = new MeasureInfoBean();
        }

        if (TextUtils.isEmpty(levelingLineId)) {
            finish();
            return;
        }


        nameList.add("晴");
        nameList.add("阴");
        nameList.add("雨");
        nameList.add("雪");
        nameList.add("风");
        nameList.add("其它");

        initView();
        initMeasureInfoView(mMeasureInfoBean);
        initClickEvent();
        initBlueToothDialog();

        if (android.os.Build.VERSION.SDK_INT >= M) {
            permissionHelper = new PermissionHelper();
            permissionHelper.requestPermission(mActivity, Measure2ndActivity.this,
                    Permissions.PERMISSIONS_LOGIN, Config.PERMISSIONS_REQUEST_COMMON_CODE);
        } else {
            startScan();
        }

        String jsonMPDText = (String) ShareRefrenceUtil.get(mActivity.getApplicationContext(),
                String.format("%s_measure_data", levelingLineId));
        String jsonMPLText = (String) ShareRefrenceUtil.get(mActivity.getApplicationContext(),
                String.format("%s_measure_list", levelingLineId));

        if (!TextUtils.isEmpty(jsonMPLText) && !TextUtils.isEmpty(jsonMPDText)) {
            cacheMSBList = JSONObject.parseArray(jsonMPDText, MeasureStationBean.class);
            cacheMPBList = JSONObject.parseArray(jsonMPLText, MeasurePointBean.class);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        measureRecordName = String.format("%s_record", sdf.format(new Date()));

        requestMeasurePointList(levelingLineId);
    }

    private void initBlueToothDialog() {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.bluetooth_list_dialog, null);
        AlertDialog.Builder bluetoothBuilder = new AlertDialog.Builder(mActivity);
        bluetoothBuilder.setTitle("蓝牙列表");
        bluetoothBuilder.setView(view);

        RecyclerView rv = view.findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        rv.setAdapter(mBtDevAdapter);

        bluetoothBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        bluetoothDialog = bluetoothBuilder.create();
    }

    private void initView() {
        llBack = findViewById(R.id.ll_back);
        txtTitle = findViewById(R.id.txt_title);

        btnMeasureMethod = findViewById(R.id.measure_method);
        btnConnectBlueTooth = findViewById(R.id.connect_bluetooth);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnSaveData = findViewById(R.id.btn_save);

        llAllMeasureData = findViewById(R.id.ll_measure_data);
        btnResult = findViewById(R.id.measure_result);
        btnCancel = findViewById(R.id.measure_cancel);

        btnDeviceNo = findViewById(R.id.device_no);
        btnTemperature = findViewById(R.id.temperature);
        btnPressure = findViewById(R.id.pressure);
        spinnerWeather = findViewById(R.id.weather_spin);

        btnOperaterName = findViewById(R.id.operater_name);
        btnOperaterIdNum = findViewById(R.id.operater_id);

        btnInstrumentBrand = findViewById(R.id.instrument_brand);
        btnInstrumentModel = findViewById(R.id.instrument_model);
    }

    private void initMeasureInfoView(MeasureInfoBean measureInfoBean) {
        int selectedIndex = 0;
        if (measureInfoBean != null) {
            if (!TextUtils.isEmpty(measureInfoBean.getSerialNo())) {
                btnDeviceNo.setText(measureInfoBean.getSerialNo());
            }

            if (!TextUtils.isEmpty(measureInfoBean.getTemperature())) {
                btnTemperature.setText(measureInfoBean.getTemperature());
            }

            if (!TextUtils.isEmpty(measureInfoBean.getPressure())) {
                btnPressure.setText(measureInfoBean.getPressure());
            }

            if (!TextUtils.isEmpty(measureInfoBean.getOperaterName())) {
                btnOperaterName.setText(measureInfoBean.getOperaterName());
            }

            if (!TextUtils.isEmpty(measureInfoBean.getOperaterIDCard())) {
                btnOperaterIdNum.setText(measureInfoBean.getOperaterIDCard());
            }


            if (!TextUtils.isEmpty(measureInfoBean.getWeather())) {
                for (int i = 0; i < nameList.size(); i++) {
                    if (TextUtils.equals(measureInfoBean.getWeather(), nameList.get(i))) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item, nameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeather.setAdapter(adapter);
        spinnerWeather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= nameList.size()) {
                    return;
                }

                mMeasureInfoBean.setWeather(nameList.get(position));
                Log.d(TAG, "project pos is " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerWeather.setSelection(selectedIndex);
    }

    //根据水准线路id查询测点列表
    private void requestMeasurePointList(final String levelingId) {
        measurePointBeanList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog("正在查询，请稍等...");
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("szlxid", levelingId);

                    String localURL = Config.SERVER_PREFIXX + "levelingLineForm/showRouteFormByLineId";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token", Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONArray list = responseJson.getJSONArray("data");
                            for (int i = 0; i < list.size(); i++) {
                                MeasurePointBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(), MeasurePointBean.class);
                                measurePointBeanList.add(bean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为 " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!measurePointBeanList.isEmpty()) {
                                //获取当前测点的数量，包含两个已知点和其余未知点
                                measureNum = measurePointBeanList.size();
                                //添加辅助拐点
                                addFirstAssistMPB();
                                //添加已知点列表
                                for (MeasurePointBean mpb : measurePointBeanList) {
                                    if (TextUtils.equals(mpb.getCdlx(), Config.BASE_POINT_TYPE)) {
                                        baseMPBList.add(mpb);
                                    }
                                }

                                //初始化已知点视图
                                addFirstBasePointMeasureDataItemView();

                                if (!cacheMSBList.isEmpty() && !cacheMPBList.isEmpty()) {
                                    initCacheData();
                                }
                            }
                            dismissWaitingDialog();
                        }
                    });

                }
            }
        }).start();
    }


    private void initCacheData() {
        int currentPos = 0;
        for (int i = 0; i < cacheMSBList.size(); i++) {
            MeasureStationBean bean = cacheMSBList.get(i);
            if (TextUtils.equals(bean.getMeasureType(),Config.MIDWAY_POINT_TYPE))
            {
                setMidWayItemView(bean.getRz(),bean.getHD());
            }
            else
            {
                if (currentPos % 2 == 0) {
                    setOdd1stPointView(bean.getRb1(), bean.getRb1HD());
                    setOdd2ndPointView(bean.getRf1(), bean.getRf1HD());
                    setOdd3rdPointView(bean.getRf2(), bean.getRf2HD());
                    setOdd4thPointView(bean.getMeasureTime(), bean.getRb2(), bean.getRb2HD());
                } else {
                    setEven1stPointView(bean.getRb1(), bean.getRb1HD());
                    setEven2ndPointView(bean.getRf1(), bean.getRf1HD());
                    setEven3rdPointView(bean.getRf2(), bean.getRf2HD());
                    setEven4thPointView(bean.getMeasureTime(), bean.getRb2(), bean.getRb2HD());
                }
                currentPos ++;
            }
        }
    }

    private void addFirstAssistMPB() {
        gdIndex++;
        MeasurePointBean bean = new MeasurePointBean();
        bean.setId(String.format("%sGD%d", levelingLineId, gdIndex));
        bean.setCdmc(String.format("GD%d", gdIndex));
        bean.setCdlx(Config.ASSIST_POINT_TYPE);
        measurePointBeanList.add(bean);
    }

    private void addAssistMPB() {

        boolean bAddEnable = false;
        for (MeasurePointBean bean : selectedMPBList) {
            if (TextUtils.equals(bean.getCdlx(), Config.ASSIST_POINT_TYPE)
                    && TextUtils.equals(bean.getId(), String.format("%sGD%d", levelingLineId, gdIndex))) {
                bAddEnable = true;
                break;
            }
        }

        if (bAddEnable) {
            gdIndex++;
            MeasurePointBean bean = new MeasurePointBean();
            bean.setId(String.format("%sGD%d", levelingLineId, gdIndex));
            bean.setCdmc(String.format("GD%d", gdIndex));
            bean.setCdlx(Config.ASSIST_POINT_TYPE);
            measurePointBeanList.add(bean);
        }
    }

    //更新当前测点的顺序measurePointIndex，当前测点在查询返回的测点列表中的索引currentMeasurePos
    private void updateMeasurePointIndex()
    {
        if (measurePointBeanList.isEmpty()) {
            return;
        }

        //缓存的测点列表信息为空
        if (cacheMPBList.isEmpty()) {
            currentMeasurePos += measurePointIndex;
            currentMeasurePos = currentMeasurePos % measurePointBeanList.size();
        } else {
            if (measurePointIndex < cacheMPBList.size()) {
                //当前测点在查询返回的测点列表中的索引
                MeasurePointBean mpb = cacheMPBList.get(measurePointIndex);
                for (int i = 0; i < measurePointBeanList.size(); i++) {
                    MeasurePointBean bean = measurePointBeanList.get(i);
                    if (TextUtils.equals(mpb.getCdmc(), bean.getCdmc())
                            && TextUtils.equals(mpb.getId(), bean.getId())) {
                        currentMeasurePos = i;
                        break;
                    }
                }
            }
        }
        measurePointIndex++;
    }


    //添加测点视图
    private void addMeasureDataItemView() {

        //更新当前测点的顺序measurePointIndex，当前测点在查询返回的测点列表中的索引currentMeasurePos
        updateMeasurePointIndex();

        //组合下拉测点列表显示
        final List<String> pointName = new ArrayList<>();
        for (MeasurePointBean measurePointBean : measurePointBeanList) {
            if (TextUtils.equals(measurePointBean.getCdlx(), Config.BASE_POINT_TYPE)) {
                pointName.add(String.format("%s(基点)", measurePointBean.getCdmc()));
            } else if (TextUtils.equals(measurePointBean.getCdlx(), Config.MEASURE_POINT_TYPE)) {
                pointName.add(String.format("%s(测点)", measurePointBean.getCdmc()));
            } else {
                pointName.add(String.format("%s(拐点)", measurePointBean.getCdmc()));
            }
        }

        //奇数次
        if (isOddTimes) {
            addOddMeasureDataItemView(pointName, measurePointIndex, currentMeasurePos);
        } else {//偶数次
            addEvenMeasureDataItemView(pointName, measurePointIndex, currentMeasurePos);
        }

        isOddTimes = !isOddTimes;

    }

    //添加中间点测量视图
    private void addMidWayMeasureDataItemView(final List<String> pointName, final int mpbIndex, final int selectedPos)
    {
        LinearLayout llMeasureData = (LinearLayout) LayoutInflater.from(Measure2ndActivity.this).inflate(R.layout.measure_data_item_midway, null);
        Spinner spinner = llMeasureData.findViewById(R.id.spin_measure_name);
        ArrayAdapter pointNameAdapter = new ArrayAdapter<>(mActivity,
                R.layout.spinner_item_style, pointName);
        pointNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(pointNameAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (selectedPos == position) {
                    return;
                }
                Log.d(TAG, "mpbIndex is " + (mpbIndex - 1));
                Log.d(TAG, "position is " + position);
                updateMeasurePointBeanList(mpbIndex - 1, pointName.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(selectedPos%pointName.size());

        updateMeasurePointBeanList(mpbIndex - 1, pointName.get(selectedPos%pointName.size()));
//
//        updateMeasureStationPoint(mpbIndex - 1);
//
        Log.d(TAG, "currentMeasurePos is "
                + currentMeasurePos);



        txtRz = llMeasureData.findViewById(R.id.txt_rz);
        txtHD = llMeasureData.findViewById(R.id.txt_hd);
        txtRd = llMeasureData.findViewById(R.id.txt_rd);
        txtDaltaRd = llMeasureData.findViewById(R.id.txt_delta_rd);

        llMeasureData.findViewById(R.id.btn_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMSBIndex = mpbIndex - 2;
                if (currentMSBIndex < measureStationBeanList.size() && currentMSBIndex >= 0) {
                    MeasureStationBean stationBean = measureStationBeanList.get(currentMSBIndex);
                    showAlertDialog("校验结果为：", parseMeasureStationBean(stationBean));
                }
            }
        });

        llAllMeasureData.addView(llMeasureData);
    }

    //添加已知基点的选择下拉框视图
    private void addFirstBasePointMeasureDataItemView() {
        if (baseMPBList.isEmpty()) {
            return;
        }

        final List<String> pointName = new ArrayList<>();
        for (MeasurePointBean measurePointBean : baseMPBList) {
            pointName.add(String.format("%s(基点)", measurePointBean.getCdmc()));
        }

        final LinearLayout llMeasureData = (LinearLayout) LayoutInflater.from(Measure2ndActivity.this).inflate(R.layout.measure_data_item_odd, null);
        Spinner spinner = llMeasureData.findViewById(R.id.spin_measure_name_odd);
        ArrayAdapter pointNameAdapter = new ArrayAdapter<>(mActivity,
                R.layout.spinner_item_style, pointName);
        pointNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(pointNameAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {

                //往测不允许修改已知基点
                if (isReturnMeasure) {
                    Toast.makeText(mActivity, "往测过程中，不允许修改已知基点", Toast.LENGTH_LONG).show();
                    return;
                }

                startMPB = baseMPBList.get(position);

                //起始点和结束点高程变化赋值
                startHeight = Double.valueOf(startMPB.getCdcsgc());
                endMPB = baseMPBList.get(position == 0 ? 1 : 0);
                endHeight = Double.valueOf(endMPB.getCdcsgc());

                //当前已选择的测量点列表为空
                if (selectedMPBList.isEmpty()) {
                    //寻找当前选中的第1基点在查询返回的测量列表中的位置
                    for (int i = 0; i < measurePointBeanList.size(); i++) {
                        MeasurePointBean bean = measurePointBeanList.get(i);
                        if (TextUtils.equals(bean.getCdmc(), startMPB.getCdmc())
                                && TextUtils.equals(bean.getId(), startMPB.getId())) {
                            currentMeasurePos = i;
                            break;
                        }
                    }
                    //更新最后的测量高值
                    lastHeight = Double.valueOf(startMPB.getCdcsgc());
                    selectedMPBList.add(startMPB);
                } else {
                    //变更已完成测量列表中的第1测点信息
                    selectedMPBList.set(0, startMPB);
                }

                //本地缓存测量点列表信息
                String jsonFirstMPBText = JSON.toJSONString(selectedMPBList);
                ShareRefrenceUtil.save(mActivity.getApplicationContext(),
                        String.format("%s_measure_list", levelingLineId), jsonFirstMPBText);

                ((TextView) (llMeasureData.findViewById(R.id.txt1_rd1))).setText(startMPB.getCdcsgc());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(0);

        //默认当前选择的测量点为起始基点
        currentMPB = baseMPBList.get(0);
        startMPB = currentMPB;

        endMPB = baseMPBList.get(1);

        //已知高程赋值
        if (selectedMPBList.isEmpty()) {
            lastHeight = Double.valueOf(startMPB.getCdcsgc());
        }

        //将已知基点添加到测量基点列表中
        selectedMPBList.add(startMPB);

        //遍历测量基点列表，定位当前基点在测量点列表的索引位置
        for (int i = 0; i < measurePointBeanList.size(); i++) {
            MeasurePointBean bean = measurePointBeanList.get(i);
            if (TextUtils.equals(bean.getCdmc(), startMPB.getCdmc())
                    && TextUtils.equals(bean.getId(), startMPB.getId())) {
                currentMeasurePos = i;
                break;
            }
        }

        Log.d(TAG, "currentMeasurePos is " + currentMeasurePos);

        txtRb1 = llMeasureData.findViewById(R.id.txt_rb1);
        txtRbHD1 = llMeasureData.findViewById(R.id.txt_rb_hd1);

        txtRb4 = llMeasureData.findViewById(R.id.txt_rb4);
        txtRbHD4 = llMeasureData.findViewById(R.id.txt_rb_hd4);

        txtRf5 = llMeasureData.findViewById(R.id.txt_rf5);
        txtRfHD5 = llMeasureData.findViewById(R.id.txt_rf_hd5);

        txtRf8 = llMeasureData.findViewById(R.id.txt_rf8);
        txtRfHD8 = llMeasureData.findViewById(R.id.txt_rf_hd8);

        txtRd1 = llMeasureData.findViewById(R.id.txt1_rd1);
        txtDeltaRd1 = llMeasureData.findViewById(R.id.txt1_rd2);

        txtDeltaRd1.setVisibility(View.GONE);

        llMeasureData.findViewById(R.id.btn_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer message = new StringBuffer();
                message.append("测点状况:正常").append("\r\n")
                        .append("工况程序：本点为基准点");
                showAlertDialog("校验结果为：", message.toString());
            }
        });

        llAllMeasureData.addView(llMeasureData);

        //当前测量点索引加1
        measurePointIndex++;
    }


    private void showAlertDialog(final String title, final String messageContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(title);
                builder.setMessage(messageContent);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "click dialog ok");
                    }
                });
                builder.show();
            }
        });
    }

    //添加奇数点视图
    private void addOddMeasureDataItemView(final List<String> pointName, final int mpbIndex, final int selectPos) {
        LinearLayout llMeasureData = (LinearLayout) LayoutInflater.from(Measure2ndActivity.this).inflate(R.layout.measure_data_item_odd, null);
        Spinner spinner = llMeasureData.findViewById(R.id.spin_measure_name_odd);
        ArrayAdapter pointNameAdapter = new ArrayAdapter<>(mActivity,
                R.layout.spinner_item_style, pointName);
        pointNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(pointNameAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (selectPos == position) {
                    return;
                }

                Log.d(TAG, "mpbIndex is " + (mpbIndex - 1));
                Log.d(TAG, "position is " + position);
                updateMeasurePointBeanList(mpbIndex - 1, pointName.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(selectPos);
        currentMPB = measurePointBeanList.get(selectPos);
        updateMeasurePointBeanList(mpbIndex - 1, pointName.get(selectPos));
//
//        updateMeasureStationPoint(mpbIndex - 1);

        Log.d(TAG, "currentMeasurePos is " + currentMeasurePos);

        txtRb1 = llMeasureData.findViewById(R.id.txt_rb1);
        txtRbHD1 = llMeasureData.findViewById(R.id.txt_rb_hd1);

        txtRb4 = llMeasureData.findViewById(R.id.txt_rb4);
        txtRbHD4 = llMeasureData.findViewById(R.id.txt_rb_hd4);

        txtRf5 = llMeasureData.findViewById(R.id.txt_rf5);
        txtRfHD5 = llMeasureData.findViewById(R.id.txt_rf_hd5);

        txtRf8 = llMeasureData.findViewById(R.id.txt_rf8);
        txtRfHD8 = llMeasureData.findViewById(R.id.txt_rf_hd8);

        txtRd1 = llMeasureData.findViewById(R.id.txt1_rd1);
        txtDeltaRd1 = llMeasureData.findViewById(R.id.txt1_rd2);

        llMeasureData.findViewById(R.id.btn_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMSBIndex = mpbIndex - 2;
                if (currentMSBIndex < measureStationBeanList.size() && currentMSBIndex >= 0) {
                    MeasureStationBean stationBean = measureStationBeanList.get(currentMSBIndex);
                    showAlertDialog("校验结果为：", parseMeasureStationBean(stationBean));
                }
            }
        });

        llAllMeasureData.addView(llMeasureData);
    }


    //更新测点列表
    private void updateMeasurePointBeanList(int mpbIndex, String pointName) {

        MeasurePointBean bean = null;
        for (int i=0; i<measurePointBeanList.size(); i++)
        {
            if (TextUtils.equals(pointName,measurePointBeanList.get(i).getCdmc()))
            {
                bean = measurePointBeanList.get(i);
                break;
            }
        }

        if (bean == null)
        {
            return;
        }

        //当前添加索引值小于已选择列表长度，将当前测点添加到测点列表
        if (mpbIndex >= selectedMPBList.size()) {
            selectedMPBList.add(bean);
        } else {
            //更新测点列表对应的测点信息
            selectedMPBList.set(mpbIndex, bean);
        }

        //更新当前测点
        if (mpbIndex == selectedMPBList.size()) {
            currentMPB = bean;
        }

        //本地缓存测点列表
        String jsonText = JSON.toJSONString(selectedMPBList);
        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_list", levelingLineId), jsonText);
    }

    private String parseMeasureStationBean(MeasureStationBean bean) {
        StringBuffer message = new StringBuffer();

        if (TextUtils.equals(bean.getMeasureType(),Config.MEASURE_POINT_TYPE))
        {
            message.append("前后视距差1:").append(bean.getDeltaHD1()).append("\r\n")
                    .append("前后视距差2:").append(bean.getDeltaHD2()).append("\r\n")
                    .append("视距累计差:").append(bean.getSumSightDistance()).append("\r\n")
                    .append("后视读数差:").append(bean.getDeltaB()).append("\r\n")
                    .append("前视读数差:").append(bean.getDeltaF()).append("\r\n")
                    .append("高差:").append(String.format("1->%s;2->%s", bean.getDeltaR1(),
                    bean.getDeltaR2())).append("\r\n")
                    .append("测点状况:正常").append("\r\n")
                    .append("工况程序:");
        }
        else
        {
            message.append("视距累计差:").append(bean.getSumSightDistance()).append("\r\n")
                    .append("高差:").append(String.format("1->%s",bean.getH())).append("\r\n")
                    .append("测点状况:正常").append("\r\n")
                    .append("工况程序:");
        }

        return message.toString();
    }


    private void addEvenMeasureDataItemView(final List<String> pointName, final int mpbIndex, final int selectedPos) {
        LinearLayout llMeasureData = (LinearLayout) LayoutInflater.from(Measure2ndActivity.this).inflate(R.layout.measure_data_item_even, null);
        Spinner spinner = llMeasureData.findViewById(R.id.spin_measure_name_even);
        ArrayAdapter pointNameAdapter = new ArrayAdapter<>(mActivity,
                R.layout.spinner_item_style, pointName);
        pointNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(pointNameAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (selectedPos == position) {
                    return;
                }
                Log.d(TAG, "mpbIndex is " + (mpbIndex - 1));
                Log.d(TAG, "position is " + position);
                updateMeasurePointBeanList(mpbIndex - 1, pointName.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(selectedPos);

        currentMPB = measurePointBeanList.get(selectedPos);
        updateMeasurePointBeanList(mpbIndex - 1, pointName.get(selectedPos));
//
//        updateMeasureStationPoint(mpbIndex - 1);
//
        Log.d(TAG, "currentMeasurePos is "
                + currentMeasurePos);

        txtRb7 = llMeasureData.findViewById(R.id.txt_rb7);
        txtRbHD7 = llMeasureData.findViewById(R.id.txt_rb_hd7);

        txtRb6 = llMeasureData.findViewById(R.id.txt_rb6);
        txtRbHD6 = llMeasureData.findViewById(R.id.txt_rb_hd6);

        txtRf2 = llMeasureData.findViewById(R.id.txt_rf2);
        txtRfHD2 = llMeasureData.findViewById(R.id.txt_rf_hd2);

        txtRf3 = llMeasureData.findViewById(R.id.txt_rf3);
        txtRfHD3 = llMeasureData.findViewById(R.id.txt_rf_hd3);

        txtRd2 = llMeasureData.findViewById(R.id.txt2_rd1);
        txtDeltaRd2 = llMeasureData.findViewById(R.id.txt2_rd2);

        llMeasureData.findViewById(R.id.btn_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMSBIndex = mpbIndex - 2;
                if (currentMSBIndex < measureStationBeanList.size() && currentMSBIndex >= 0) {
                    MeasureStationBean stationBean = measureStationBeanList.get(currentMSBIndex);
                    showAlertDialog("校验结果为：", parseMeasureStationBean(stationBean));
                }
            }
        });

        llAllMeasureData.addView(llMeasureData);
    }

    private void initClickEvent() {
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnMeasureMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReturnMeasure) {
                    return;
                }

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
                alertBuilder.setTitle("信息提示");
                alertBuilder.setMessage("是否确认要切换到返测?");
                alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isReturnMeasure = false;
                        btnMeasureMethod.setText("返测");
                        btnMeasureMethod.setTextColor(getResources().getColor(R.color.colorPrimary));

//                        addFirstBasePointMeasureDataItemView();
                    }
                });

                alertBuilder.show();
            }
        });

        btnConnectBlueTooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothDialog.isShowing()) {
                    bluetoothDialog.show();
                }
            }
        });

        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (measureStationBeanList.isEmpty()) {
                    APP.toast("无测量数据！", 0);
                    return;
                }

                if (!validateMeasureInfo(mMeasureInfoBean)) {
                    return;
                }

                if (!isMeasureFinished) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
                    alertBuilder.setTitle("信息提示");
                    alertBuilder.setMessage("未完成测量，是否临时保存未完成的测量数据?");
                    alertBuilder.setNegativeButton("临时保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    alertBuilder.setPositiveButton("清除数据", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_data", levelingLineId), "");
                            ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_point", levelingLineId), "");
                            finish();
                        }
                    });

                    alertBuilder.show();
                    return;
                }

                if (validateData()) {
                    saveMeasureInfo(mMeasureInfoBean);
                } else {
                    APP.toast("数据不满足提交条件，请检查", 0);
                }

            }
        });


        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
                alertBuilder.setTitle("信息提示");
                alertBuilder.setMessage("是否确认要一键清空所有数据?");
                alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_data", levelingLineId), "");
                        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_point", levelingLineId), "");

                        if (cacheMPBList != null)
                        {
                            cacheMPBList.clear();
                        }

                        if (cacheMSBList != null)
                        {
                            cacheMSBList.clear();
                        }

                        clearAllData();
                    }
                });

                alertBuilder.show();
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMeasureFinished) {
                    APP.toast("测量未完成，请完成测量后再平差！", 0);
                    return;
                }

                //单程测量，不允许有任何点重复
                if (!isReturnMeasure)
                {
                    for (int i = 0; i < selectedMPBList.size() - 1; i++) {
                        MeasurePointBean bean1 = selectedMPBList.get(i);
                        for (int j = i + 1; j < selectedMPBList.size(); j++) {
                            MeasurePointBean bean2 = selectedMPBList.get(j);
                            if (TextUtils.equals(bean1.getId(), bean2.getId())
                                    && TextUtils.equals(bean1.getCdmc(), bean2.getCdmc())) {
                                APP.toast(String.format("%s点测量重复，无法平差！", bean1.getCdmc()), 0);
                                return;
                            }
                        }
                    }
                }

                nextStep();
//                showMeasureResultDialog("测量结果", parseMeasureResultMessage());

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (measureTime % 4 == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
                    builder.setTitle("请输入需重测的站数");
                    builder.setView(view);

                    final EditText editText = view.findViewById(R.id.edt_txt);
                    DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, true);
                    editText.setKeyListener(numericOnlyListener);

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "click dialog ok");

                            int num = Integer.valueOf(editText.getText().toString());
                            if (num > cacheMSBList.size()) {
                                APP.toast("输入总站数超限！", 0);
                                return;
                            }

                            if (num <= 0) {
                                return;
                            }


                            List<MeasureStationBean> msbList = new ArrayList<>();
                            for (int i = 0; i < (cacheMSBList.size() - num); i++) {
                                msbList.add(cacheMSBList.get(i));
                            }

                            List<MeasurePointBean> mpbList = new ArrayList<>();
                            for (int i = 0; i < (cacheMPBList.size() - num); i++) {
                                mpbList.add(cacheMPBList.get(i));
                            }

                            cacheMSBList.clear();
                            cacheMPBList.clear();
                            cacheMSBList.addAll(msbList);
                            cacheMPBList.addAll(mpbList);

                            clearAllData();

                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "click dialog cancel");
                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle("信息提示");
                    builder.setMessage("确认重新测量本站数据吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "click dialog ok");
                            measureTime = 0;
                            measurePointIndex = 0;
                            llAllMeasureData.removeAllViews();

                            addFirstBasePointMeasureDataItemView();
                            if (!cacheMSBList.isEmpty() && !cacheMPBList.isEmpty()) {
                                initCacheData();
                            }
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "click dialog cancel");
                        }
                    });
                    builder.show();
                }
            }
        });

        btnDeviceNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceSerieNoDialog();

//                sendCommand("FML\r\n");
            }
        });

        btnTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemperatureDialog();
            }
        });

        btnPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPressureDialog();
            }
        });

        btnOperaterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperaterNameDialog();
            }
        });

        btnOperaterIdNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOperaterIdNumDialog();
            }
        });

        btnInstrumentBrand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstrumentBrandDialog();
            }
        });

        btnInstrumentModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstrumentModelDialog();
            }
        });
    }


    private void clearAllData()
    {
        measureTime = 0;
        measurePointIndex = 0;
        gdIndex = 0;
        llAllMeasureData.removeAllViews();

        if (measurePointBeanList != null) {
            measurePointBeanList.clear();
        }

        if (baseMPBList != null) {
            baseMPBList.clear();
        }

        if (selectedMPBList!= null) {
            selectedMPBList.clear();
        }

        if (measureStationBeanList != null){
            measureStationBeanList.clear();
        }

        datFileName = "";

        requestMeasurePointList(levelingLineId);
    }

    private void addToIn1StationList(String firstCDName, String secondCDName, int stationNum,
                                     double H, double L) {
        In1FileBean in1FileBean = new In1FileBean();
        in1FileBean.setStartPtName(firstCDName);
        in1FileBean.setEndPtName(secondCDName);
        in1FileBean.setStationNum(stationNum);
        in1FileBean.setDiffH(H);
        in1FileBean.setMeasureL(L);

        in1BeanList.add(in1FileBean);
    }

    //根据测点名称查找测点类型
    private String getMeasurePointTypeByName(String mpName) {
        String mpType = Config.MEASURE_POINT_TYPE;
        for (int i = 0; i < selectedMPBList.size(); i++) {
            if (TextUtils.equals(mpName, selectedMPBList.get(i).getCdmc())) {
                mpType = selectedMPBList.get(i).getCdlx();
                break;
            }
        }
        return mpType;
    }

    //判断当前名称的测点是否可跳过
    private boolean isCurrentMPSkipAble(String mpName) {
        boolean isSkipAble = true;
        for (int i = 0; i < measureStationBeanList.size(); i++) {
            MeasureStationBean bean = measureStationBeanList.get(i);
            String startName = bean.getStartPName();
            String endName = bean.getEndPName();
            //没有中间点的转点可跳过
            if (TextUtils.equals(mpName, startName) &&
                    TextUtils.equals(getMeasurePointTypeByName(startName), Config.ASSIST_POINT_TYPE) &&
                    TextUtils.equals(getMeasurePointTypeByName(endName), Config.MIDWAY_POINT_TYPE)) {
                isSkipAble = false;
                break;
            }
        }

        return isSkipAble;
    }

    private void productIN1DataWithStationNum() {
        String fileName = String.format("%s_in1.txt", levelingLineNo);
        File file = new File(Util.getMeasureFileDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        if (in1BeanList != null) {
            in1BeanList.clear();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(SPILT_LINE);
        sb.append("\t起点名\t终点名\t测段高差\t测段距离\t测站数\t\n");
        sb.append(SPILT_LINE);
        sb.append("\t***\t***\t***\t***\t***\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(SPILT_LINE);

        sb.append(String.format("%s,%.5f", startMPB.getCdmc(), startHeight));
        sb.append(SWITCH_LINE);

        sb.append(String.format("%s,%.5f", endMPB.getCdmc(), endHeight));
        sb.append(SWITCH_LINE);

        int stationNum = 1;
        for (int i = 0; i < measureStationBeanList.size(); i++) {
            MeasureStationBean bean = measureStationBeanList.get(i);
            String startName = bean.getStartPName();
            String endName = bean.getEndPName();
            double H = bean.getH();
            double L = bean.getL();
            //如果测站起点是可跳过的转点，跳过
            if (TextUtils.equals(getMeasurePointTypeByName(startName), Config.ASSIST_POINT_TYPE)
                    && isCurrentMPSkipAble(startName)) {
                continue;
            }

            //如果测站终点是可跳过的转点，测站加1
            if (TextUtils.equals(getMeasurePointTypeByName(endName), Config.ASSIST_POINT_TYPE)
                    && isCurrentMPSkipAble(endName)) {
                stationNum++;
                if (i < measureStationBeanList.size() - 1) {
                    MeasureStationBean bean2 = measureStationBeanList.get(i + 1);
                    L += bean2.getL();
                    H += bean2.getH();
                }

                for (int j = i + 1; j < measureStationBeanList.size(); j++) {
                    MeasureStationBean bean2 = measureStationBeanList.get(j);
                    endName = bean2.getEndPName();
                    if (TextUtils.equals(getMeasurePointTypeByName(endName), Config.ASSIST_POINT_TYPE)
                            && isCurrentMPSkipAble(endName)) {
                        L += bean2.getL();
                        H += bean2.getH();
                        stationNum++;
                    }
                }
            }

            addToIn1StationList(startName, endName, stationNum, H, L);
            sb.append(String.format("%s,%s,%.5f,%.3f,%s",
                    startName, endName, H,
                    L, String.valueOf(stationNum)));
            sb.append(SWITCH_LINE);

            stationNum = 1;
        }

        previewFileNameList.add(file.getAbsolutePath());

        Util.writeMeasureDataToSDPathFile(sb.toString(), fileName);
    }

    private void productIN1DataWithMeasureLength() {
        String fileName = String.format("%s_in1.txt", levelingLineNo);
        File file = new File(Util.getMeasureFileDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(SPILT_LINE);
        sb.append("\t起点名\t终点名\t测段高差\t测段距离\t测站数\t\n");
        sb.append(SPILT_LINE);
        sb.append("\t***\t***\t***\t***\t***\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(SPILT_LINE);
        sb.append(String.format("%s,%s", baseMPBList.get(0).getCdmc(), baseMPBList.get(0).getCdcsgc()));
        sb.append(SWITCH_LINE);
        sb.append(String.format("%s,%s", baseMPBList.get(1).getCdmc(), baseMPBList.get(1).getCdcsgc()));
        sb.append(SWITCH_LINE);
        for (int i = 0; i < measureStationBeanList.size(); i++) {
            MeasureStationBean bean = measureStationBeanList.get(i);

            //跳过转点
            MeasurePointBean mpb = selectedMPBList.get(i + 1);
            if (TextUtils.equals(mpb.getCdlx(), Config.ASSIST_POINT_TYPE)) {
                continue;
            }


            String firstCDName = "", secondCDName = "";
            if (i + 1 < selectedMPBList.size()) {
                firstCDName = selectedMPBList.get(i).getCdmc();
                secondCDName = selectedMPBList.get(i + 1).getCdmc();
            }

            sb.append(String.format("%s,%s,%s,%s",
                    firstCDName, secondCDName, Arith.doubleToStr(bean.getH()),
                    Arith.doubleToStr(bean.getL() / 1000)));
            sb.append(SWITCH_LINE);
        }

        Util.writeMeasureDataToSDPathFile(sb.toString(), fileName);
    }

    private void productIN1Data() {
        productIN1DataWithStationNum();
    }


    private void productCO1Data() {
        String fileName = String.format("%s_co1.txt", levelingLineNo);
        File file = new File(Util.getMeasureFileDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(SPILT_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t往返测高差较差统计结果\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append("\t起点 \t终点 \t往测距离km \t返测距离km \t往测高差(m) \t返测高差(m) \t较差(mm) \t限差(mm)");
        sb.append(SWITCH_LINE);

        sb.append(SPILT_LINE);
        sb.append(String.format("               每公里水准测量的高差偶然中误差:%s(mm)                      ", ""));
        sb.append(SPILT_LINE);
        Util.writeMeasureDataToSDPathFile(sb.toString(), fileName);

    }

    private void productCL1Data() {
        String fileName = String.format("%s_cl1.txt", levelingLineNo);
        File file = new File(Util.getMeasureFileDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(SPILT_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t高程网闭合差计算结果\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(SWITCH_LINE);

        sb.append(String.format("符合路线号：%s", "1"));
        sb.append(SWITCH_LINE);
        sb.append("线路点号：");

        double sumHeight = 0.0;
        double sumLength = 0.0;
        for (int i = 0; i < in1BeanList.size(); i++) {
            In1FileBean bean = in1BeanList.get(i);
            String currentType = getMeasurePointTypeByName(bean.getEndPtName());
            if (TextUtils.equals(currentType, Config.MIDWAY_POINT_TYPE)) {
                continue;
            }

            if (i == in1BeanList.size() - 1) {
                sb.append(String.format("  %s  %s  ", bean.getStartPtName(),
                        in1BeanList.get(i).getEndPtName()));
            } else {
                sb.append(String.format("  %s  ", bean.getStartPtName()));
            }

            sumHeight += bean.getDiffH();
            sumLength += bean.getMeasureL();
        }


        //取km作为单位，精度要求小数点后4位
        sumLength = Arith.round(sumLength, 5);
        Log.d(TAG, "sumHeight is " + sumHeight + " , sumLength is " + sumLength);
        double fh_measure = startHeight * 1000 + sumHeight * 1000;
        fh_measure = endHeight * 1000 - fh_measure;
        double fh_threshold = Math.sqrt(sumLength) * 4.0f;

        fh_measure = Arith.round(fh_measure, 2);
        fh_threshold = Arith.round(fh_threshold, 2);
        Log.d(TAG, "高差闭合差：" + fh_measure + " , 限差：" + fh_threshold);
        sb.append(String.format("高差闭合差： %.2f(mm)  \t  限差： %.2f(mm)",
                fh_measure, fh_threshold));
        sb.append(SWITCH_LINE);
        sb.append(String.format("符合路线长度：%.4f(km)", sumLength));

        sb.append(SWITCH_LINE);
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);

        previewFileNameList.add(file.getAbsolutePath());

        Util.writeMeasureDataToSDPathFile(sb.toString(), fileName);
    }


    //计算侧段数
    private int calcStationNum() {
        return in1BeanList.size();
    }

    //计算未知点个数
    private int calcUnknownPointNum() {
        int t = 0;
        for (int i = 0; i < in1BeanList.size(); i++) {
            In1FileBean bean = in1BeanList.get(i);
            if (!TextUtils.equals(getMeasurePointTypeByName(bean.getEndPtName()),
                    Config.BASE_POINT_TYPE)) {
                t++;
            }

        }
        return t;
    }

    //计算已知点个数
    private int calcKnownPointNum() {
        return baseMPBList.size();
    }


    //初始化测段数组
    private int[] initStationNumArray(int n) {
        int stationNum = 0;
        int stationNumArray[] = new int[n];
        int index = 0;
        for (int i = 0; i < measureStationBeanList.size(); ) {
            for (int j = i + 1; j < selectedMPBList.size(); j++) {
                if (TextUtils.equals(selectedMPBList.get(j).getCdlx(),
                        Config.ASSIST_POINT_TYPE)) {
                    stationNum++;
                } else {
                    stationNum++;
                    break;
                }
            }

            i += stationNum;
            stationNumArray[index] = stationNum;
            index++;
            stationNum = 0;
        }

        return stationNumArray;
    }


    //生成高程网平差结果（常规）字符串
    private String productOU1Step1(double[][] X) {
        //概略高程
        StringBuffer sb = new StringBuffer();
        sb.append(SPILT_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t高程网平差结果(常规)\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t概略高程\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\r\n", "序号", "点号", "高程(m)"));
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%.4f\r\n", "1",
                startMPB.getCdmc(), Arith.round(startHeight, 4)));
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%.4f\r\n", "2",
                endMPB.getCdmc(), Arith.round(endHeight, 4)));

        for (int i = 0; i < in1BeanList.size(); i++) {
            if (i == in1BeanList.size() - 1) {
                continue;
            }
            In1FileBean bean = in1BeanList.get(i);
            sb.append(String.format("\t\t\t\t%d\t\t\t\t%s\t\t\t\t%.4f\r\n",
                    i + 3, bean.getEndPtName(), Arith.round(X[i][0], 4)));
        }

        return sb.toString();
    }

    //生成测段实测高差数据统计
    private String productOU1Step2() {
        StringBuffer sb = new StringBuffer();
        //测段实测高差数据统计
        sb.append("\t\t\t\t\t\t\t\t\t\t测段实测高差数据统计\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s" +
                        "\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\r\n",
                "序号", "起点", "终点", "高差(m)", "距离(km)", "权"));

        for (int i = 0; i < in1BeanList.size(); i++) {
            In1FileBean in1FileBean = in1BeanList.get(i);
            String tempString = String.format("\t\t\t\t%d\t\t\t\t%s\t\t\t\t%s" +
                            "\t\t\t\t%.5f\t\t\t\t%.4f\t\t\t\t%.3f\r\n",
                    i + 1, in1FileBean.getStartPtName(), in1FileBean.getEndPtName(),
                    Arith.round(in1FileBean.getDiffH(), 5),
                    Arith.round(in1FileBean.getMeasureL(), 4),
                    Arith.round(Arith.div(1.0, in1FileBean.getStationNum(), 4), 3));

            sb.append(tempString);
        }
        sb.append(SPILT_LINE);
        return sb.toString();
    }

    //生成高程平差值及其精度
    private String productOU1Step3(double[][] dX, double[][] X, double[] mHk) {
        StringBuffer sb = new StringBuffer();
        sb.append(SWITCH_LINE);
        //测段实测高差数据统计
        sb.append("\t\t\t\t\t\t\t\t\t\t高程平差值及其精度\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s" +
                        "\t\t\t\t%s\r\n",
                "序号", "点号", "高程(m)", "中误差"));

        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%.5f\t\t\t\t%s\r\n",
                "1", startMPB.getCdmc(), Arith.round(startHeight, 5), ""));
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%.5f\t\t\t\t%s\r\n",
                "2", endMPB.getCdmc(), Arith.round(endHeight, 5), ""));

        for (int i = 0; i < in1BeanList.size() - 1; i++) {
            In1FileBean bean = in1BeanList.get(i);
            sb.append(String.format("\t\t\t\t%d\t\t\t\t%s\t\t\t\t%.5f\t\t\t\t%.2f\r\n",
                    i + 3, bean.getEndPtName(),
                    Arith.round(Arith.add(X[i][0], dX[i][0]), 5),
                    Arith.round(mHk[i], 2)));
        }
        return sb.toString();
    }

    //生成高差平差值及其精度
    private String productOU1Step4(double[][] V, double[][] h, double[] mVk) {
        StringBuffer sb = new StringBuffer();
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t高差平差值及其精度\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(String.format("\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\t\t\t\t%s\r\n",
                "序号", "起点", "终点", "高差平差值(mm)", "改正数(mm)", "中误差(mm)"));


        for (int i = 0; i < in1BeanList.size(); i++) {
            In1FileBean bean = in1BeanList.get(i);

            sb.append(String.format("\t\t\t\t%d\t\t\t\t%s\t\t\t\t%s\t\t\t\t%.5f\t\t\t\t%.2f\t\t\t\t%.2f\r\n",
                    i + 1, bean.getStartPtName(), bean.getEndPtName(),
                    Arith.round(Arith.sub(h[i][0], V[i][0] / 1000), 5),
                    Arith.round(V[i][0], 2),
                    Arith.round(mVk[i], 2)));

        }
        return sb.toString();
    }


    private void productOU1Data() {
        String fileName = String.format("%s_ou1.txt", levelingLineNo);
        File file = new File(Util.getMeasureFileDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        //计算测端数
        int n = calcStationNum();

        //计算未知点个数
        int t = calcUnknownPointNum();

        //已知点个数
        int b = calcKnownPointNum();

        if (n <= t) {
            showAlertDialog("生成数据", "数据有误，未知点个数应该要小于测段数");
            return;
        }

        //总的点数
        int m = t + b;

        //概略高程
        StringBuffer sb = new StringBuffer();
        double[][] a = Ou1FileUtil.initAMatrix(in1BeanList, n, t);
        //A转置矩阵 t*n
        double[][] aT = MatrixOperation.MatrixTranspose(a);
        //X矩阵 n*1
        double[][] X = Ou1FileUtil.initXMatrix(in1BeanList, startHeight, endHeight);
        //H矩阵 n*1
        double[][] h = Ou1FileUtil.initHMatrix(in1BeanList);
        //l矩阵
        double[][] l = Ou1FileUtil.initLMatrix(in1BeanList, X, startHeight, endHeight);
//        Matrix L_Matrix = new Matrix(l);
        //p矩阵 n*n
        double[][] p = Ou1FileUtil.initPMatrix(in1BeanList);
//        Matrix P_Matrix = new Matrix(p);
//
//        //A矩阵 n*t
//        Matrix A_Matrix = new Matrix(a);
//        //A转置矩阵 t*n
//        Matrix AT_Matrix = A_Matrix.transpose();
        //Q矩阵(t*t)
        double[][] q = MatrixOperation.MatrixMultiply(MatrixOperation.MatrixMultiply(aT, p), a);
        //高差平差值的权逆矩阵 AT*p*A权逆矩阵,t*t
////        Matrix qMatrix = AT_Matrix.times(P_Matrix).times(A_Matrix);
        Matrix QxMatrix = new Matrix(q).inverse();
        double[][] Qx = QxMatrix.getArray();
        //高程改正数矩阵dX t*1
        double[][] dX = MatrixOperation.MatrixMultiply(
                MatrixOperation.MatrixMultiply(Qx, aT), p);
        dX = MatrixOperation.MatrixMultiply(dX, l);
        dX = MatrixOperation.MatrixMinus(
                new double[t][1], dX);
//        Matrix AtPL_Matrix = AT_Matrix.times(P_Matrix).times(L_Matrix);

        //dX矩阵，dX = - (Qx*At*P*L)
//        Matrix dX_Matrix = new Matrix(new double[t][1],t,1).minus(QxMatrix.
//                times(AtPL_Matrix));
//        //V矩阵 V = A*dX + L
//        Matrix V_Matrix = A_Matrix.times(dX_Matrix).plus(L_Matrix);
//        double [][] dX = new Matrix(new double[t][1],t,1).minus(QxMatrix.
//                times(AtPL_Matrix)).getArray();

        //A*dX n*t t*1=n*1
        double[][] adX = MatrixOperation.MatrixMultiply(a, dX);
        //n*1的矩阵
        double[][] temp = MatrixOperation.MatrixAdd(adX, l);
        double[][] V = MatrixOperation.MatrixTimes(temp, 1000.0);
        double pvv = Ou1FileUtil.calcPVV(p, V);
        pvv = Arith.roundMinus(pvv, 3);
        double u = Arith.roundMinus(Math.sqrt(pvv / (n - t)), 3);

        Log.d(TAG, "u is " + u);
        double[] mHk = Ou1FileUtil.calcHeightMidVk(u, Qx);
        double[] mVk = Ou1FileUtil.calcHeightDiffMidVk(in1BeanList, u, Qx);

        String step1 = productOU1Step1(X);
        Log.d(TAG, "step1:\n" + step1);
        String step2 = productOU1Step2();
        Log.d(TAG, "\nstep2:\n" + step2);
        String step3 = productOU1Step3(dX, X, mHk);
        Log.d(TAG, "\nstep3:\n" + step3);
        String step4 = productOU1Step4(V, h, mVk);
        Log.d(TAG, "\nstep4:\n" + step4);

        sb.append(step1).append(step2).append(step3).append(step4);
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);
        sb.append(SWITCH_LINE);
        sb.append("\t\t\t\t\t\t\t\t\t\t高程控制网总体信息\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);
        sb.append(SPILT_LINE);

        sb.append("\t\t\t\t\t\t\t\t\t\t已知高程点:2\t\t\t\t\t\t\t\t\t");
        sb.append(SWITCH_LINE);

        sb.append(String.format("\t\t\t\t\t\t\t\t\t\t未知高程点:%d\t\t\t\t\t\t\t\t\t", t));
        sb.append(SWITCH_LINE);

        sb.append(String.format("\t\t\t\t\t\t\t\t\t\t高差测端数:%d\t\t\t\t\t\t\t\t\t", n));
        sb.append(SWITCH_LINE);

        sb.append(String.format("\t\t\t\t\t\t\t\t\t\tPVV:%.3f\t\t\t\t\t\t\t\t\t", pvv));
        sb.append(SWITCH_LINE);

        sb.append(String.format("\t\t\t\t\t\t\t\t\t\t自由度:%d\t\t\t\t\t\t\t\t\t", n - t));
        sb.append(SWITCH_LINE);

        sb.append(String.format("\t\t\t\t\t\t\t\t\t\t验后单位权中误差:%.3f\t\t\t\t\t\t\t\t\t", u));
        sb.append(SWITCH_LINE);

        sb.append(SPILT_LINE);

        previewFileNameList.add(file.getAbsolutePath());
        Util.writeMeasureDataToSDPathFile(sb.toString(), fileName);
    }



    private void nextStep() {
        showWaitingDialog("生成数据中...");

//        loadData();
//        load0430Data();
        String startName = "",endName = "";
        //重新关联测点以及测站关系
        for (int i=0; i<measureStationBeanList.size(); i++)
        {
            MeasureStationBean stationBean = measureStationBeanList.get(i);
            if (!TextUtils.equals(stationBean.getMeasureType(),Config.MIDWAY_POINT_TYPE))
            {
                startName = selectedMPBList.get(i).getCdmc();
            }
            endName = selectedMPBList.get(i+1).getCdmc();
            stationBean.setStartPName(startName);
            stationBean.setEndPName(endName);
        }

        if (previewFileNameList != null) {
            previewFileNameList.clear();
        } else {
            previewFileNameList = new ArrayList<>();
        }

        //往返测，顺序生成in1、co1、cl1、ou1文件数据
        if (isReturnMeasure) {
            productIN1Data();
            productCO1Data();
            productCL1Data();
            productOU1Data();
        } else //单程测量，顺序生成in1、cl1、ou1文件数据
        {
            productIN1Data();
            productCL1Data();
            productOU1Data();
        }

        dismissWaitingDialog();
        skipToPreviewFile();
    }

//    private void loadData() {
//        int index = 0;
//
//        selectedMPBList = new ArrayList<>();
//        measureStationBeanList = new ArrayList<>();
//        baseMPBList = new ArrayList<>();
//
//        startHeight = 7.16200;
//        endHeight = 6.93070;
//        addMeasurePointBean(String.valueOf(index++), "23901", Config.BASE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "QD07", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "QD08", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "QD09", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "QD10", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "QD05", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "23900", Config.BASE_POINT_TYPE);
//
//        addMeasureStationBean(1.53788, 1.66820, 1.66817, 1.53785,
//                5.055, 5.572, 5.569, 5.053, "23901", "QD07");
//        addMeasureStationBean(1.31163, 1.35915, 1.35918, 1.31162,
//                10.392, 10.058, 10.052, 10.389, "QD07", "QD08");
//        addMeasureStationBean(1.51132, 1.07254, 1.07256, 1.51130,
//                12.785, 13.382, 13.377, 12.789, "QD08", "QD09");
//        addMeasureStationBean(1.30104, 1.69092, 1.69093, 1.30105,
//                22.716, 22.336, 22.330, 22.711, "QD09", "QD10");
//        addMeasureStationBean(1.45110, 1.44081, 1.44079, 1.45108,
//                19.506, 20.161, 20.156, 19.5, "QD10", "QD05");
//        addMeasureStationBean(1.32140, 1.43432, 1.43431, 1.32139,
//                7.964, 8.476, 8.482, 7.965, "QD05", "23900");
//
//        startMPB = baseMPBList.get(0);
//        endMPB = baseMPBList.get(1);
//    }
//
//    private void load0430Data() {
//        int index = 0;
//
//        selectedMPBList = new ArrayList<>();
//        measureStationBeanList = new ArrayList<>();
//        baseMPBList = new ArrayList<>();
//
//        int scale = 5;
//
//        startHeight = 6.98741;
//        endHeight = 7.19464;
//        addMeasurePointBean(String.valueOf(index++), "BM07", Config.BASE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB1", Config.MEASURE_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB2", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB3", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "A1", Config.ASSIST_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB4", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB5", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB6", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB7", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "A2", Config.ASSIST_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB8", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "DB9", Config.MIDWAY_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "A3", Config.ASSIST_POINT_TYPE);
//        addMeasurePointBean(String.valueOf(index++), "BM08", Config.BASE_POINT_TYPE);
//
//        addMeasureStationBean(1.35150, 1.66964, 1.66967, 1.35147,
//                22.778, 22.443, 22.442, 22.782, "BM07", "DB1");
//        double H = Arith.sub(Arith.mul(Arith.add(1.66964, 1.66967), 0.5), 1.64456);
//        addMeasureStationBean(Arith.round(H, scale), 6.649, "DB1", "DB2");
//        H = Arith.sub(Arith.mul(Arith.add(1.66964, 1.66967), 0.5), 1.48340);
//        addMeasureStationBean(Arith.round(H, scale), 17.326, "DB1", "DB3");
//        addMeasureStationBean(1.61670, 1.61669, 1.61674, 1.61669,
//                16.900, 16.235, 16.245, 16.903, "DB1", "A1");
//
//        H = Arith.sub(Arith.mul(Arith.add(1.61669, 1.61674), 0.5), 1.60304);
//        addMeasureStationBean(Arith.round(H, scale), 19.442, "A1", "DB4");
//        H = Arith.sub(Arith.mul(Arith.add(1.61669, 1.61674), 0.5), 1.55342);
//        addMeasureStationBean(Arith.round(H, scale), 26.874, "A1", "DB5");
//        H = Arith.sub(Arith.mul(Arith.add(1.61669, 1.61674), 0.5), 1.56664);
//        addMeasureStationBean(Arith.round(H, scale), 26.024, "A1", "DB6");
//        H = Arith.sub(Arith.mul(Arith.add(1.61669, 1.61674), 0.5), 1.39588);
//        addMeasureStationBean(Arith.round(H, scale), 32.265, "A1", "DB7");
//        addMeasureStationBean(1.71348, 1.66323, 1.66328, 1.71345,
//                26.427, 26.984, 26.988, 26.433, "A1", "A2");
//
//        H = Arith.sub(Arith.mul(Arith.add(1.66323, 1.66328), 0.5), 1.62695);
//        addMeasureStationBean(Arith.round(H, scale), 15.560, "A2", "DB8");
//        H = Arith.sub(Arith.mul(Arith.add(1.66323, 1.66328), 0.5), 1.64780);
//        addMeasureStationBean(Arith.round(H, scale), 25.956, "A2", "DB9");
//        addMeasureStationBean(1.51140, 1.51451, 1.51448, 1.51137,
//                24.062, 24.655, 24.665, 24.066, "A2", "A3");
//        addMeasureStationBean(1.46972, 0.99123, 0.99126, 1.46969,
//                24.575, 24.372, 24.375, 24.567, "A3", "BM08");
//
//        startMPB = baseMPBList.get(0);
//        endMPB = baseMPBList.get(1);
//    }

    private void addMeasurePointBean(String id, String name, String type) {
        MeasurePointBean bean = new MeasurePointBean();
        bean.setId(id);
        bean.setCdmc(name);
        bean.setCdlx(type);
        selectedMPBList.add(bean);

        if (TextUtils.equals(type, Config.BASE_POINT_TYPE)) {
            baseMPBList.add(bean);
        }
    }

    private void addMeasureStationBean(double H, double L, String startName, String endName) {
        MeasureStationBean bean = new MeasureStationBean();
        bean.setH(H);
        bean.setL(Arith.round(L / 1000.0f, 3));
        bean.setStartPName(startName);
        bean.setEndPName(endName);
        measureStationBeanList.add(bean);
    }

    private void addMeasureStationBean(double rb1, double rf1, double rf2, double rb2,
                                       double s1, double s2, double s3, double s4,
                                       String startName, String endName) {
        MeasureStationBean bean = new MeasureStationBean();
        bean.setRb1(Arith.doubleToStr(rb1));
        bean.setRf1(Arith.doubleToStr(rf1));
        bean.setRf2(Arith.doubleToStr(rf2));
        bean.setRb2(Arith.doubleToStr(rb2));

        double H = Arith.mul(Arith.sub(Arith.add(rb1, rb2), Arith.add(rf1, rf2)), 0.5);
        double L = Arith.mul(Arith.add(Arith.add(s1, s2), Arith.add(s3, s4)), 0.0005);
        bean.setH(Arith.round(H, 5));
        bean.setL(Arith.round(L, 3));
        bean.setStartPName(startName);
        bean.setEndPName(endName);
        measureStationBeanList.add(bean);
    }

    private void addEvenMeasureStationBean(double rf1, double rb1, double rb2, double rf2,
                                           double s1, double s2, double s3, double s4) {
        MeasureStationBean bean = new MeasureStationBean();
        bean.setRb1(Arith.doubleToStr(rb1));
        bean.setRf1(Arith.doubleToStr(rf1));
        bean.setRf2(Arith.doubleToStr(rf2));
        bean.setRb2(Arith.doubleToStr(rb2));

        bean.setH(Arith.round(Arith.div(Arith.sub(Arith.add(rb1, rb2), Arith.add(rf1, rf2)), 2.0), 5));
        bean.setL(Arith.round(Arith.div(s1 + s2 + s3 + s4, 2000.0), 3));
        measureStationBeanList.add(bean);
    }

    private void skipToPreviewFile() {
        Intent intent = new Intent(mActivity, FilePreviewActivity.class);
        intent.putExtra("step", 0);
        intent.putExtra("levelingLineNo", levelingLineNo);
        intent.putExtra("levelingLineId", levelingLineId);
        intent.putStringArrayListExtra("previewFileNameList", previewFileNameList);
        startActivity(intent);
    }

    private void sendCommand(String command) {
        mClient.sendMsg(command);
    }

    private boolean validateMeasureInfo(MeasureInfoBean measureInfoBean) {
        if (TextUtils.isEmpty(measureInfoBean.getSerialNo())) {
            APP.toast("设备序列号不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getTemperature())) {
            APP.toast("温度不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getPressure())) {
            APP.toast("气压不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getOperaterName())) {
            APP.toast("测量人员姓名不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getOperaterIDCard())) {
            APP.toast("测量人员身份证姓名不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getInstrumentBrand())) {
            APP.toast("仪器品牌不能为空", 0);
            return false;
        }

        if (TextUtils.isEmpty(measureInfoBean.getInstrumentModel())) {
            APP.toast("仪器型号不能为空", 0);
            return false;
        }


        return true;
    }

    //显示测量结果对话框
    private void showMeasureResultDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.common_dialog, null);
        builder.setView(view);

        final TextView txtTitle = view.findViewById(R.id.txt_title);
        final TextView txtContent = view.findViewById(R.id.txt_content);
        final TextView txtConfirm = view.findViewById(R.id.txt_confirm);

        txtTitle.setText(title);
        txtContent.setText(content);

        builder.setTitle("");

        resultDialg = builder.create();

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultDialg != null && resultDialg.isShowing()) {
                    resultDialg.dismiss();
                }
            }
        });

        builder.show();
    }

    private String parseMeasureResultMessage() {
        StringBuilder sb = new StringBuilder();

        double sumLength = 0.0;
        double sumHeight = 0.0;
        double pvv = 0.0;
        //高差测端数
        int n = 0;
        //未知高程点个数
        int t = 0;
        //已知高程点个数
        int baseNum = 0;
        //高程中误差不合格数
        int heightFailNum = 0;
        //高差中误差不合格数
        int hDeltaFailNum = 0;
        //累积视距差
        double sumDeltaLength = 0.0;
        for (int i = 0; i < measureStationBeanList.size(); i++) {
            //拐点以及辅助点不参与平差
            if (i + 1 < selectedMPBList.size()) {
                MeasurePointBean mpb = selectedMPBList.get(i + 1);
                if (TextUtils.equals(mpb.getCdlx(), Config.ASSIST_POINT_TYPE)) {
                    continue;
                } else if (TextUtils.equals(mpb.getCdlx(), Config.MEASURE_POINT_TYPE)) {
                    t++;
                } else {
                    baseNum++;
                }
            }

            MeasureStationBean bean = measureStationBeanList.get(i);
            double height = bean.getH();
            //高差中误差不合格数
            if (Math.abs(height)
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
                hDeltaFailNum++;
            }

            //高程中误差不合格数
            if ((Math.abs(Arith.sub(Double.valueOf(bean.getRf2()),
                    Double.valueOf(bean.getRf1())))
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA))
                    || (Math.abs(Arith.sub(Double.valueOf(bean.getRb2()),
                    Double.valueOf(bean.getRb1())))
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA))) {
                heightFailNum++;
            }

            sumHeight = Arith.add(sumHeight, height);

            double length = bean.getL();
            sumLength = Arith.add(sumLength, length);

            n++;
        }

        //取km作为单位，精度要求小数点后4位
        sumLength = Arith.div(sumLength, 1000.0, 5);
        Log.d(TAG, "sumHeight is " + sumHeight + " , sumLength is " + sumLength);
        double fh_measure = Arith.sub(Arith.add(startHeight, sumHeight), endHeight);
        Log.d(TAG, "高差闭合差的测量值为：" + fh_measure);
        fh_threshold = Arith.mul(Math.sqrt(sumLength), 4.0);
        Log.d(TAG, "高差闭合差的允许差为：" + fh_threshold);

        for (int i = 0; i < measureStationBeanList.size(); i++) {
            //拐点以及辅助点不参与平差
            if (i + 1 < selectedMPBList.size()) {
                MeasurePointBean mpb = selectedMPBList.get(i + 1);
                if (TextUtils.equals(mpb.getCdlx(), Config.ASSIST_POINT_TYPE)) {
                    continue;
                }
            }

            MeasureStationBean bean = measureStationBeanList.get(i);
            //PVV p=1/l v=-fh_measure*ln/l
            double length = bean.getL();
            //计算V值
            double v = Arith.div(length, sumLength, 5);
            v = Arith.mul(fh_measure, v);

            //计算vv值
            v = Arith.mul(v, v);
            //计算pvv值
            v = Arith.div(v, length, 5);
            pvv = Arith.add(pvv, v);
        }

        boolean bFlag = (Math.abs(fh_measure) > Math.abs(fh_threshold)) ? true : false;

        sb.append("闭合差结果:").append("\r\n")
                .append("闭合差实测值(fh)为").append(fh_measure)
                .append(",闭合差允许值(Fh)为").append(fh_threshold)
                .append(",fh").append(bFlag ? ">" : "<")
                .append("Fh").append(bFlag ? "(测量不合格)" : "(测量合格)").append("\r\n")
                .append("平差结果:").append("\r\n")
                .append("已知高程点个数:").append(baseNum).append("\r\n")
                .append("未知高程点个数t:").append(t).append("\r\n")
                .append("总点数:").append(n).append("\r\n")
                .append("高差测端数n:").append(n).append("\r\n")
                .append("自由度r:").append(n - t).append("\r\n")
                .append("[PVV]:").append(pvv).append("\r\n")
                .append("验后单位权中误差(mm):").append(Math.sqrt(pvv)).append("\r\n")
                .append("观测线路长度(km):").append(sumLength).append("\r\n")
                .append("高程中误差不合格数:").append(heightFailNum).append("\r\n")
                .append("高差中误差不合格数:").append(hDeltaFailNum);

        return sb.toString();
    }

    private boolean validateData() {
        if (startMPB == null || measureStationBeanList.isEmpty()) {
            return false;
        }

        double sumLength = 0.0;
        double sumHeight = 0.0;

        //高程中误差不合格数
        int heightFailNum = 0;
        //高差中误差不合格数
        int hDeltaFailNum = 0;
        //累积视距差
        double sumDeltaLength = 0.0;
        for (int i = 0; i < measureStationBeanList.size(); i++) {

            MeasureStationBean bean = measureStationBeanList.get(i);
            //拐点以及辅助点不参与平差
            if (i + 1 < selectedMPBList.size()) {
                MeasurePointBean mpb = selectedMPBList.get(i + 1);
                if (TextUtils.equals(mpb.getCdlx(), Config.ASSIST_POINT_TYPE)) {
                    continue;
                }
            }

            double height = bean.getH();
            //高差中误差不合格数
            if (Math.abs(height)
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
                hDeltaFailNum++;
            }

            //高程中误差不合格数
            if ((Math.abs(Arith.sub(Double.valueOf(bean.getRf2()),
                    Double.valueOf(bean.getRf1())))
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA))
                    || (Math.abs(Arith.sub(Double.valueOf(bean.getRb2()),
                    Double.valueOf(bean.getRb1())))
                    > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA))) {
                heightFailNum++;
            }

            sumHeight = Arith.add(sumHeight, height);

            double length = bean.getL();
            sumLength = Arith.add(sumLength, length);

            double diff1 = Arith.sub(Double.valueOf(bean.getRb1HD()),
                    Double.valueOf(bean.getRf1HD()));
            double diff2 = Arith.sub(Double.valueOf(bean.getRb2HD()),
                    Double.valueOf(bean.getRf2HD()));
            sumDeltaLength = Arith.add(sumDeltaLength, Arith.div(Arith.add(diff1, diff2), 2.0, 6));
        }

        //取km作为单位，精度要求小数点后4位
        sumLength = Arith.div(sumLength, 1000.0, 5);
        Log.d(TAG, "sumHeight is " + sumHeight + " , sumLength is " + sumLength);
        double fh_measure = Arith.sub(Arith.add(startHeight, sumHeight), endHeight);
        Log.d(TAG, "高差闭合差的测量值为：" + fh_measure);
        double fh_threshold = Arith.mul(Math.sqrt(sumLength), 4.0);
        Log.d(TAG, "高差闭合差的允许差为：" + fh_threshold);

        boolean bFlag = (Math.abs(fh_measure) > Math.abs(fh_threshold)) ? true : false;

        return bFlag && (heightFailNum == 0) && (hDeltaFailNum == 0)
                && (sumDeltaLength < Double.valueOf(Config.THRESHOLD_SETTING_FB_HD_SUM));
    }

    private void showDeviceSerieNoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);
//        DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, false);
//        editText.setKeyListener(numericOnlyListener);

        if (mMeasureStationBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getSerialNo())) {
            editText.setText(mMeasureInfoBean.getSerialNo());
        }

        builder.setTitle("请输入设备序列号:");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String serialNo = editText.getText().toString();
                if (TextUtils.isEmpty(serialNo)) {
                    btnDeviceNo.setText("设备序列号");
                } else {
                    checkSeriesNo(serialNo);
                }
            }
        });
        builder.show();
    }

    private void checkSeriesNo(final String seriesNo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog("正在查询，请稍等...");
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("deviceCode", seriesNo);

                    String localURL = Config.SERVER_PREFIXX + "device/checkDeviceByCode";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token", Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);
                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("data").toLowerCase(), "true")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnDeviceNo.setText(seriesNo);
                                    mMeasureInfoBean.setSerialNo(seriesNo);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "水准序列号无效 ", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为 " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    dismissWaitingDialog();
                }
            }
        }).start();
    }


    private void showTemperatureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);
        DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, true);
        editText.setKeyListener(numericOnlyListener);

        if (mMeasureStationBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getTemperature())) {
            editText.setText(mMeasureInfoBean.getTemperature());
        }

        builder.setTitle("温度:单位为摄氏度,精确至0.1℃");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String temperature = editText.getText().toString();
                if (TextUtils.isEmpty(temperature)) {
                    btnTemperature.setText("温度");
                } else {
                    btnTemperature.setText(String.format("温度:%s°C", temperature));
                }

                mMeasureInfoBean.setTemperature(temperature);
            }
        });
        builder.show();
    }

    private void showPressureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);
        DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, true);
        editText.setKeyListener(numericOnlyListener);

        if (mMeasureInfoBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getPressure())) {
            editText.setText(mMeasureInfoBean.getPressure());
        }

        builder.setTitle("气压:单位hPa,精确至0.1hPa");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pressure = editText.getText().toString();
                if (TextUtils.isEmpty(pressure)) {
                    btnPressure.setText("气压");
                } else {
                    btnPressure.setText(String.format("气压:%shPa", pressure));
                }

                mMeasureInfoBean.setPressure(pressure);
            }
        });
        builder.show();
    }

    private void showOperaterNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);

        if (mMeasureInfoBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getOperaterName())) {
            editText.setText(mMeasureInfoBean.getOperaterName());
        }

        builder.setTitle("输入司镜人员姓名");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String operaterName = editText.getText().toString();

                if (TextUtils.isEmpty(operaterName)) {
                    btnOperaterName.setText("司镜人员姓名");
                } else {
                    btnOperaterName.setText(operaterName);
                }

                mMeasureInfoBean.setOperaterName(operaterName);
            }
        });
        builder.show();
    }

    private void showOperaterIdNumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);
        DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, false);
        editText.setKeyListener(numericOnlyListener);


        if (mMeasureInfoBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getOperaterIDCard())) {
            editText.setText(mMeasureInfoBean.getOperaterIDCard());
        }

        builder.setTitle("输入司镜人员身份证号");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "click dialog ok");
                String idCardNum = editText.getText().toString();
                if (TextUtils.isEmpty(idCardNum)) {
                    APP.toast("身份证号码应该为15位或者18位", 0);
                    return;
                }

                if ((idCardNum.length() != 15) && (idCardNum.length() != 18)) {
                    APP.toast("身份证号码应该为15位或者18位", 0);
                    return;
                }


                CheckIDCardRule rule = new CheckIDCardRule(idCardNum, null);
                if (!rule.validate()) {
                    APP.toast("身份证无效，不是合法的身份证号码", 0);
                    return;
                }
                mMeasureInfoBean.setOperaterIDCard(idCardNum);

                btnOperaterIdNum.setText(idCardNum);
            }
        });
        builder.show();
    }


    private void showInstrumentBrandDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);

        if (mMeasureInfoBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getInstrumentBrand())) {
            editText.setText(mMeasureInfoBean.getInstrumentBrand());
        }

        builder.setTitle("输入仪器品牌");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "click dialog ok");
                String instrumentBrand = editText.getText().toString();
                mMeasureInfoBean.setInstrumentBrand(instrumentBrand);
                btnInstrumentBrand.setText(instrumentBrand);
            }
        });
        builder.show();
    }

    private void showInstrumentModelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.edit_input_dialog, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.edt_txt);

        if (mMeasureInfoBean != null && !TextUtils.isEmpty(mMeasureInfoBean.getInstrumentModel())) {
            editText.setText(mMeasureInfoBean.getInstrumentModel());
        }

        builder.setTitle("输入仪器型号");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "click dialog ok");
                String instrumentModel = editText.getText().toString();
                mMeasureInfoBean.setInstrumentModel(instrumentModel);
                btnInstrumentModel.setText(instrumentModel);
            }
        });
        builder.show();
    }

    @Override
    public void socketNotify(int state, Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                final BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                txtTitle.setText(msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissWaitingDialog();
                        btnConnectBlueTooth.setText("连接蓝牙");
                        btnConnectBlueTooth.setVisibility(View.GONE);
                        ShareRefrenceUtil.save(mActivity, "bluetooth_address", dev.getAddress());
                    }
                });

//                String orderCommand = "!KENC  | 1 bit";
//                mClient.sendMsg(orderCommand);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
                txtTitle.setText(msg);
                btnConnectBlueTooth.setText("连接蓝牙");
                btnConnectBlueTooth.setVisibility(View.VISIBLE);
                dismissWaitingDialog();
                break;
            case BtBase.Listener.MSG:
                msg = String.format("%s", obj);
                break;
            case BtBase.Listener.REC500:
                parseREC500Format((String) obj);
//                APP.toast("格式错误，目前只支持REC E/M5格式", 0);
                break;
            case BtBase.Listener.M5:
//                String m5Message = String.format("M5协议格式：%s\n", obj);
                parseM5Format((String) obj);
                break;
        }

        if (!TextUtils.isEmpty(msg)) {
            APP.toast(msg, 0);
        }
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev) || mClient.isConnected()) {
            APP.toast("已经连接了", 0);
            return;
        }
        mClient.connect(dev);
        bluetoothDialog.dismiss();

        showWaitingDialog("正在连接蓝牙，请稍等...");

        btnConnectBlueTooth.setText("正在连接蓝牙...");
    }

    private void showWaitingDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(mActivity);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage(message);
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

    @Override
    public void foundDev(BluetoothDevice dev) {
        mBtDevAdapter.add(dev);

        List<BluetoothDevice> deviceList = mBtDevAdapter.getDeviceList();
        for (BluetoothDevice device : deviceList) {
            if (TextUtils.equals(device.getAddress(),
                    (String) ShareRefrenceUtil.get(mActivity, "bluetooth_address"))) {
                mClient.connect(device);
                btnConnectBlueTooth.setText("正在连接蓝牙...");
                break;
            }
        }
    }

    private void parseREC500Format(String message) {
        Log.d(TAG, "recv REC500 Data Format : " + message);
        Util.writeTxtToSDPathFile(message);
        //3个空格
        String prefix = message.substring(0, 2);
        //4位地址 存储器地址 数字
        String WI = message.substring(3, 6);
        //27位点识别 点识别14位 附加信息13位 数字/字母
        String PI_Number = message.substring(8, 21);
        String PI_Extra = message.substring(22, 34);
        //T1 2位类型识别 数字/字母
        String T1_Type = message.substring(36, 37);
        //T1 12位数字
        String T1_Data = message.substring(38, 50);
        //T2 2位类型识别 数字/字母
        String T2_Type = message.substring(51, 53);
        //T2 13位数字
        String T2_Data = message.substring(54, 66);
        //T3 2位类型识别 数字/字母
        String T3_Type = message.substring(67, 68);
        //T3 9位数字
        String T3_Data = message.substring(69, 77);

        Log.d(TAG, "点识别PI_Number：" + PI_Number + " , 附加信息PI_Extra：" + PI_Extra + "T1数据块的类型：" + T1_Type + " , T1数据块的数据：" + T1_Data
                + " , T2数据块的类型：" + T2_Type + " , T2数据块的数据：" + T2_Data +
                " , T3数据块的类型：" + T3_Type + " , T3数据块的数据：" + T3_Data);
//        if (isMeasureFinished) {
//            APP.toast("已完成闭合测量", 0);
//        }

        PI_Extra = PI_Extra.trim();
        if (PI_Extra.length() >= 9) {
            PI_Extra = PI_Extra.substring(0, 8);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            PI_Extra = sdf.format(new Date()) + " " + PI_Extra;
        }

        T1_Type = T1_Type.trim();
        T1_Data = T1_Data.trim();
        T2_Type = T2_Type.trim();
        T2_Data = T2_Data.trim();
        T3_Type = T3_Type.trim();
        T3_Data = T3_Data.trim();


        //包含###或者非单点测量模式，均认为错误格式
        if (message.contains("#") || !T1_Type.equals("R") || !T2_Type.equals("HD")) {
            APP.toast("格式错误", 0);
            return;
        }

        if (Float.valueOf(T1_Data) > Float.valueOf(Config.THRESHOLD_SETTING_MAX_SIGHT_HEIGHT)) {
            APP.toast("读取的标尺高度不能大于设置的最大视线高", 0);
            return;
        }

        if (Float.valueOf(T1_Data) < Float.valueOf(Config.THRESHOLD_SETTING_MIN_SIGHT_HEIGHT)) {
            APP.toast("读取的标尺高度不能小于设置的最小视线高", 0);
            return;
        }

        measureTime++;
        //奇偶顺序交替，奇数次BFFB，偶数次FBBF
        //奇数点模式 BFFB
        if (measureMode == Config.ODD_MEASURE_POINT_MODE) {
            //第1次后视Rb1 B
            if (measureTime % 4 == 1) {
                setOdd1stPointView(T1_Data, T2_Data);
            }
            //第2次前视Rf1 F
            else if (measureTime % 4 == 2) {
                setOdd2ndPointView(T1_Data, T2_Data);
            }
            //第3次前视Rf2 F
            else if (measureTime % 4 == 3) {
                setOdd3rdPointView(T1_Data, T2_Data);
            }
            //第4次后视 Rb2  B
            else if (measureTime % 4 == 0) {
                setOdd4thPointView(PI_Extra, T1_Data, T2_Data);
            }
        } else {//偶数点模式 FBBF
            //第1次前视Rf1 F
            if (measureTime % 4 == 1) {
                setEven1stPointView(T1_Data, T2_Data);
            }
            //第2次后视Rb1 B
            else if (measureTime % 4 == 2) {
                setEven2ndPointView(T1_Data, T2_Data);
            }
            //第3次后视 Rb2 B
            else if (measureTime % 4 == 3) {
                setEven3rdPointView(T1_Data, T2_Data);
            }
            //第4次前视 Rf2 F
            else if (measureTime % 4 == 0) {
                setEven4thPointView(PI_Extra, T1_Data, T2_Data);
            }
        }

    }

    private void parseM5Format(String message) {
        Log.d(TAG,   message);
        Util.writeTxtToSDPathFile(message);

        //For M5|Adr     1|TO  2004298.dat                |                      |                      |                      |
        //For M5|Adr     2|TO  Start-Line      aBFFB    01|                      |                      |                      |
        //For M5|Adr     3|KD1     BM07                 01|                      |                      |Z         6.98741 m   |
        //For M5|Adr     4|KD1     BM07      16:12:121  01|Rb        1.35150 m   |HD         22.778 m   |                      |
        //For M5|Adr     5|KD1      DB1      16:12:311  01|Rf        1.66964 m   |HD         22.443 m   |                      |
        //For M5|Adr     6|KD1      DB1      16:12:351  01|Rf        1.66967 m   |HD         22.442 m   |                      |
        //For M5|Adr     7|KD1     BM07      16:12:541  01|Rb        1.35147 m   |HD         22.782 m   |                      |
        //For M5|Adr     8|KD1      DB1      16:12:54   01|                      |                      |Z         6.66924 m   |
        //For M5|Adr     9|TO  Intermediate sight.      01|                      |                      |                      |
        //For M5|Adr    10|KD1      DB2      16:13:361  01|Rz        1.64456 m   |HD          6.649 m   |Z         6.69434 m   |
        //For M5|Adr    11|KD1      DB3      16:13:591  01|Rz        1.48340 m   |HD         17.326 m   |Z         6.85550 m   |
        //For M5|Adr    12|TO  End of interm. sight.    01|                      |                      |                      |

//        message = "For M5|Adr     1|TO  2004298.dat                |                      |                      |                      |";
//        message = "For M5|Adr     2|TO  Start-Line      aBFFB    01|                      |                      |                      |";
//        message = "For M5|Adr     3|KD1     BM07                 01|                      |                      |Z         6.98741 m   |";
//        message = "For M5|Adr     4|KD1     BM07      16:12:121  01|Rb        1.35150 m   |HD         22.778 m   |                      |";
//        message = "For M5|Adr     5|KD1      DB1      16:12:311  01|Rf        1.66964 m   |HD         22.443 m   |                      |";
//        message = "For M5|Adr     6|KD1      DB1      16:12:351  01|Rf        1.66967 m   |HD         22.442 m   |                      |";
//        message = "For M5|Adr     7|KD1     BM07      16:12:541  01|Rb        1.35147 m   |HD         22.782 m   |                      |";
//        message = "For M5|Adr     8|KD1      DB1      16:12:54   01|                      |                      |Z         6.66924 m   |";
//        message = "For M5|Adr     9|TO  Intermediate sight.      01|                      |                      |                      |";
//        message = "For M5|Adr    10|KD1      DB2      16:13:361  01|Rz        1.64456 m   |HD          6.649 m   |Z         6.69434 m   ";
//        message = "For M5|Adr    11|KD1      DB3      16:13:591  01|Rz        1.48340 m   |HD         17.326 m   |Z         6.85550 m   |";
//        message = "For M5|Adr    12|TO  End of interm. sight.    01|                      |                      |                      |";

        //6位，定义M5格式
        String m5Format = message.substring(0, 6);
        //3位，地址的类型识别器
        String adr1 = message.substring(7, 10);
        //5位地址 存储器地址 数字
        String WI = message.substring(11, 16);
        //3位 类型识别器Pla(a=1-0,对于10个标记)或T1
        String Pla = message.substring(17, 20);
        //17位  信息块P1或T1(点识别器P1或文本信息T1,T0等)
        String P1 = message.substring(21, 48);
        //2位  块3的类型识别器
        String P3_Type = message.substring(49, 51);
        //14位 块3的数值
        String P3_Data = message.substring(52, 66);
        //4位 块3的单位
        String P3_Unit = message.substring(67, 71);
        //2位  块4的类型识别器
        String P4_Type = message.substring(72, 74);
        //14位 块4的数值
        String P4_Data = message.substring(75, 89);
        //4位 块4的单位
        String P4_Unit = message.substring(90, 93);
        //2位  块5的类型识别器
        String P5_Type = message.substring(95, 96);
        //14位 块5的数值
        String P5_Data = message.substring(98, 112);
        //4位 块5的单位
        String P5_Unit = message.substring(113, 116);

        P1 = P1.trim();

        /*****************************水准线路模式*******************************************/

        //包含#认为是错误格式
        if (message.contains("##")) {
            APP.toast("格式错误", 0);
            return;
        }

        //判断是否模式的数据包
        if (P1.contains("Start-Line"))
        {
            if (P1.contains("BBFF"))//后前前后
            {
                measureMode = Config.ODD_MEASURE_POINT_MODE;
                isMiddleMode = false;
            }
            else if (P1.contains("aFBBF"))//前后后前
            {
                measureMode = Config.EVEN_MEASURE_POINT_MODE;
                isMiddleMode = false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            datFileName = Util.createNewDatFile(String.format("%s_%s.dat", levelingLineNo, sdf.format(new Date())));
            Util.writeMeasureDataToSDPathFile(message,datFileName);
            return;
        }
        else if (P1.contains("Intermediate sight."))//开始测量中间点
        {
            isMiddleMode = true;
            if (!TextUtils.isEmpty(datFileName))
            {
                Util.writeMeasureDataToSDPathFile(message,datFileName);
            }
            return;
        } else if (P1.contains("End of interm. sight."))//结束中间点测量
        {
            isMiddleMode = false;
            if (!TextUtils.isEmpty(datFileName))
            {
                Util.writeMeasureDataToSDPathFile(message,datFileName);
            }
            return;
        }
        else if (P1.contains("End-Line"))//结束测量
        {
            isMeasureFinished = true;
            if (!TextUtils.isEmpty(datFileName))
            {
                Util.writeMeasureDataToSDPathFile(message,datFileName);
            }

            datFileName = "";
            return;
        }

        if (!TextUtils.isEmpty(datFileName))
        {
            Util.writeMeasureDataToSDPathFile(message,datFileName);
        }

        //P3或者P4为空时，认为是无效测量数据
        if (TextUtils.isEmpty(P3_Data) || TextUtils.isEmpty(P4_Data)) {
            return;
        }

        int length = P1.length();
//        Log.d(TAG, "P1 length is " + length);
        if (length >= 24) {
            P1 = P1.substring(11, 20);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            P1 = sdf.format(new Date()) + " " + P1;
        } else if (length >= 9 && length < 24) {
            P1 = P1.substring(0, 8);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            P1 = sdf.format(new Date()) + " " + P1;
        }

        P3_Data = P3_Data.trim();
        P3_Type = P3_Type.trim();
        P3_Unit = P3_Unit.trim();

        P4_Data = P4_Data.trim();
        P4_Type = P4_Type.trim();
        P4_Unit = P4_Unit.trim();

        P5_Data = P5_Data.trim();
        P5_Type = P5_Type.trim();
        P5_Unit = P5_Unit.trim();

//        if (isMeasureFinished) {
//            APP.toast("已完成闭合测量", 0);
//        }

        //判断标尺读数高度是否满足限制最大以及最小阈值
        if (Float.valueOf(P3_Data) > Float.valueOf(Config.THRESHOLD_SETTING_MAX_SIGHT_HEIGHT)) {
            APP.toast("读取的标尺高度不能大于设置的最大视线高", 0);
            return;
        }

        if (Float.valueOf(P3_Data) < Float.valueOf(Config.THRESHOLD_SETTING_MIN_SIGHT_HEIGHT)) {
            APP.toast("读取的标尺高度不能小于设置的最小视线高", 0);
            return;
        }

        //先判断是否中间点
        if (isMiddleMode) {
            setMidWayItemView(P3_Data,P4_Data);
            return;
        }


        measureTime++;

        if (isSingleMeasureMode)
        {
            singlePointMeasureMode(P1,P3_Type,P3_Data,P4_Data);
        }
        else
        {
            lineMeasureMode(P1,P3_Type,P3_Data,P4_Data);
        }
    }


    //缓存初始化
    private void setMidWayItemView(String P3_Data,String P4_Data)
    {
        updateMeasurePointIndex();
        final List<String> pointName = new ArrayList<>();
        for (MeasurePointBean measurePointBean : measurePointBeanList) {
            if (TextUtils.equals(measurePointBean.getCdlx(), Config.BASE_POINT_TYPE)) {
                pointName.add(String.format("%s(基点)", measurePointBean.getCdmc()));
            } else if (TextUtils.equals(measurePointBean.getCdlx(), Config.MEASURE_POINT_TYPE)) {
                pointName.add(String.format("%s(测点)", measurePointBean.getCdmc()));
            }
        }

        mMeasureStationBean = new MeasureStationBean();
        mMeasureStationBean.setMeasureType(Config.MIDWAY_POINT_TYPE);
        mMeasureStationBean.setL(Double.valueOf(P4_Data));
        double deltaRd = Arith.sub(Double.valueOf(P3_Data),lastRb);
        mMeasureStationBean.setH(deltaRd);

        mMeasureStationBean.setRz(P3_Data);
        mMeasureStationBean.setHD(P4_Data);

        measureStationBeanList.add(mMeasureStationBean);
        String jsonText = JSON.toJSONString(measureStationBeanList);
        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_data", levelingLineId), jsonText);


        addMidWayMeasureDataItemView(pointName,measurePointIndex,currentMeasurePos);

        txtRz.setText("Rz " + P3_Data);
        txtHD.setText("HD " + P4_Data);
        txtRd.setText(Arith.doubleToStr(Arith.add(lastHeight,deltaRd)));
        txtDaltaRd.setText(Arith.doubleToStr(deltaRd));
    }


    //水准线路测量模式
    private void lineMeasureMode(String P1,String P3_Type,String P3_Data,String P4_Data)
    {
        Log.d(TAG,"measure Time is " + P1);
        if (measureMode == Config.ODD_MEASURE_POINT_MODE) {
            if (TextUtils.equals(P3_Type, "Rb") && measureTime % 4 == 1) {
                setOdd1stPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rf") && measureTime % 4 == 2) {
                setOdd2ndPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rf") && measureTime % 4 == 3) {
                setOdd3rdPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rb") && measureTime % 4 == 0) {
                setOdd4thPointView(P1, P3_Data, P4_Data);
            }
        } else {
            if (TextUtils.equals(P3_Type, "Rf") && measureTime % 4 == 1) {
                setEven1stPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rb") && measureTime % 4 == 2) {
                setEven2ndPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rb") && measureTime % 4 == 3) {
                setEven3rdPointView(P3_Data, P4_Data);
            } else if (TextUtils.equals(P3_Type, "Rf") && measureTime % 4 == 0) {
                setEven4thPointView(P1, P3_Data, P4_Data);
            }

        }
    }

    //单点测量模式
    private void singlePointMeasureMode(String P1,String P3_Type,String P3_Data,String P4_Data)
    {
        /*****************************单点测量模式*******************************************/
        //奇偶顺序交替，奇数次BFFB，偶数次FBBF
        //奇数点模式 BFFB
        if (measureMode == Config.ODD_MEASURE_POINT_MODE) {
            //第1次后视Rb1 B
            if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 1)) {
                setOdd1stPointView(P3_Data, P4_Data);
            }
            //第2次前视Rf1 F
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 2)){
                setOdd2ndPointView(P3_Data, P4_Data);
            }
            //第3次前视Rf2 F
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 3)) {
                setOdd3rdPointView(P3_Data, P4_Data);
            }
            //第4次后视 Rb2  B
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 0)) {
                setOdd4thPointView(P1, P3_Data, P4_Data);
            }
        } else {//偶数点模式 FBBF
            //第1次前视Rf1 F
            if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 1)) {
                setEven1stPointView(P3_Data, P4_Data);
            }
            //第2次后视Rb1 B
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 2)) {
                setEven2ndPointView(P3_Data, P4_Data);
            }
            //第3次后视 Rb2 B
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 3)) {
                setEven3rdPointView(P3_Data, P4_Data);
            }
            //第4次前视 Rf2 F
            else if (TextUtils.equals(P3_Type,"R") && (measureTime % 4 == 0)) {
                setEven4thPointView(P1, P3_Data, P4_Data);
            }
        }
    }


    //设置奇数点BFFB模式的第1个测量值B1
    private void setOdd1stPointView(String rValue, String hdValue) {
        //初始化测站信息
        mMeasureStationBean = new MeasureStationBean();
        mMeasureStationBean.setMeasureIndex(measurePointIndex);
        mMeasureStationBean.setMeasureType(Config.MEASURE_POINT_TYPE);

        //奇数点第一个值为后视1 以及视距1
        txtRb1.setText(String.format("%s", rValue));
        txtRbHD1.setText(String.format("%s", hdValue));

        rb1 = rValue;
        hd1 = hdValue;
        //设置后视1和视距1
        mMeasureStationBean.setRb1(rValue);
        mMeasureStationBean.setRb1HD(hdValue);

        //如果当前点为拐点，增加拐点
        if (TextUtils.equals(currentMPB.getCdlx(), Config.ASSIST_POINT_TYPE)) {
            addAssistMPB();
        }
        //当前点位基点且非第一个基点
//        else if (TextUtils.equals(currentMPB.getCdlx(), Config.BASE_POINT_TYPE)
//                && !TextUtils.equals(currentMPB.getId(), startMPB.getId())
//                && (measurePointIndex - 1) >= measureNum) {
//            APP.toast("已完成闭合测量", 0);
//        }
        else {
            //显示待测点视图
            addMeasureDataItemView();
        }
    }

    //设置奇数点BFFB的第2个测量值F1
    private void setOdd2ndPointView(String rValue, String hdValue) {

        //奇数点第2个值前视1以及视距2
        rf1 = rValue;
        hd2 = hdValue;

        //判断前视和后视的视距差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(hd2),
                Double.valueOf(hd1))) > Double.valueOf(Config.THRESHOLD_SETTING_FB_HD_DELTA)) {
            APP.toast("前后两次读取的视距差超过限定值", 0);
            measureTime--;
            return;
        }

        //界面显示前视1以及视距2
        txtRf2.setText(String.format("%s", rValue));
        txtRfHD2.setText(String.format("%s", hdValue));

        //设置前视1和视距2
        mMeasureStationBean.setRf1(rValue);
        mMeasureStationBean.setRf1HD(hdValue);
    }

    //设置奇数点的第3个测量值F2
    private void setOdd3rdPointView(String rValue, String hdValue) {

        //奇数点的第3个值前视2以及视距3
        rf2 = rValue;
        hd3 = hdValue;

        //判定两次前视读数差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(rf2), Double.valueOf(rf1)))
                > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
            APP.toast("前后两次读取的前视值超过限定值", 0);
            measureTime--;
            return;
        }

        //界面显示前视2以及视距3
        txtRf3.setText(String.format("%s", rValue));
        txtRfHD3.setText(String.format("%s", hdValue));

        //设置前视2和视距3
        mMeasureStationBean.setRf2(rValue);
        mMeasureStationBean.setRf2HD(hdValue);
    }

    //设置奇数点的第4个测量值
    private void setOdd4thPointView(String time, String rValue, String hdValue) {

        //奇数点的第4个值后视2以及视距4
        rb2 = rValue;
        hd4 = hdValue;

        //判断两次后视读数差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(rb2), Double.valueOf(rb1)))
                > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
            APP.toast("前后两次读取的后视值超过限定值", 0);
            measureTime--;
            return;
        }

        //判断前视和后视的视距差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(hd3), Double.valueOf(hd4)))
                > Double.valueOf(Config.THRESHOLD_SETTING_FB_HD_DELTA)) {
            APP.toast("前后两次读取的视距差超过限定值", 0);
            measureTime--;
            return;
        }

        //界面显示后视2以及视距4
        txtRb4.setText(String.format("%s", rValue));
        txtRbHD4.setText(String.format("%s", hdValue));

        //设置后视2和视距4
        mMeasureStationBean.setRb2(rValue);
        mMeasureStationBean.setRb2HD(hdValue);

        mMeasureStationBean.setMeasureTime(time);

        //切换到偶数点模式
        measureMode = Config.EVEN_MEASURE_POINT_MODE;
        //重置测点次数
        measureTime = 0;

        //如果当前点为拐点，增加拐点
        if (TextUtils.equals(currentMPB.getCdlx(), Config.ASSIST_POINT_TYPE)) {
            addAssistMPB();
        }


        calcAndSaveProcess();

    }


    private void calcAndSaveProcess()
    {
        //计算两次后视视距和
        double lb = Arith.add(Double.valueOf(mMeasureStationBean.getRb1HD()), Double.valueOf(mMeasureStationBean.getRb2HD()));
        //计算两次前视视距和
        double lf = Arith.add(Double.valueOf(mMeasureStationBean.getRf1HD()), Double.valueOf(mMeasureStationBean.getRf2HD()));
        double length = Arith.add(lb, lf);
        //计算视距平均值
        length = Arith.div(length, 2.0, 5);
        //设置视距平均值
        mMeasureStationBean.setL(length);


        //计算前后高程差1
        double diffR1 = Arith.sub(Double.valueOf(mMeasureStationBean.getRb1()), Double.valueOf(mMeasureStationBean.getRf1()));
        //计算前后高程差2
        double diffR2 = Arith.sub(Double.valueOf(mMeasureStationBean.getRb2()), Double.valueOf(mMeasureStationBean.getRf2()));
        //计算后视与前视的高差
        double height = Arith.add(diffR1, diffR2);
        //计算后视与前视的高差
        height = Arith.div(height, 2.0, 5);
        //设置后视与前视高差
        mMeasureStationBean.setH(height);

        //计算当前点的近似高程
        lastHeight = Arith.add(lastHeight, height);

        if (isOddTimes)
        {
            //设置近似高程
            txtRd2.setText(Arith.doubleToStr(lastHeight));
            //设置与前一点的高差
            txtDeltaRd2.setText(Arith.doubleToStr(height));
        }
        else
        {
            //设置近似高程
            txtRd1.setText(Arith.doubleToStr(lastHeight));
            //设置与前一点的高差
            txtDeltaRd1.setText(Arith.doubleToStr(height));
        }


        //校验是否完成单程闭合测量
//        checkSingleMeasureFinished();

        //前后视距差1
        double diff1 = Arith.sub(Double.valueOf(mMeasureStationBean.getRb1HD()),
                Double.valueOf(mMeasureStationBean.getRf1HD()));
        //前后视距差2
        double diff2 = Arith.sub(Double.valueOf(mMeasureStationBean.getRb2HD()),
                Double.valueOf(mMeasureStationBean.getRf2HD()));
        //累积前后视距差
        sumSightDis = Arith.add(sumSightDis, Arith.div(Arith.add(diff1, diff2), 2.0, 6));
        mMeasureStationBean.setSumSightDistance(Arith.doubleToStr(sumSightDis));

        //计算后视读数差
        double diffB = Arith.sub(Double.valueOf(mMeasureStationBean.getRb2()), Double.valueOf(mMeasureStationBean.getRb1()));
        //计算前视读数差
        double diffF = Arith.sub(Double.valueOf(mMeasureStationBean.getRf2()), Double.valueOf(mMeasureStationBean.getRf1()));


        //前后视距差1
        mMeasureStationBean.setDeltaHD1(Arith.doubleToStr(diff1));
        //前后视距差2
        mMeasureStationBean.setDeltaHD2(Arith.doubleToStr(diff2));
        //后视读数差
        mMeasureStationBean.setDeltaB(Arith.doubleToStr(diffB));
        //前视读数差
        mMeasureStationBean.setDeltaF(Arith.doubleToStr(diffF));
        //前后高差1
        mMeasureStationBean.setDeltaR1(Arith.doubleToStr(diffR1));
        //前后高差2
        mMeasureStationBean.setDeltaR2(Arith.doubleToStr(diffR2));

        //更新前一个测点的后视值
        lastRb = Arith.div(Arith.add(Double.valueOf(mMeasureStationBean.getRb1()),
                Double.valueOf(mMeasureStationBean.getRb2())),2.0,5);

        measureStationBeanList.add(mMeasureStationBean);
        String jsonText = JSON.toJSONString(measureStationBeanList);
        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_data", levelingLineId), jsonText);
    }


    //设置偶数点FBBF的第1个测量值F
    private void setEven1stPointView(String rValue, String hdValue) {
        //添加偶数点视图
        addMeasureDataItemView();

        mMeasureStationBean = new MeasureStationBean();
        mMeasureStationBean.setMeasureIndex(measurePointIndex);
        mMeasureStationBean.setMeasureType(Config.MEASURE_POINT_TYPE);

        //偶数点模式前视值1以及视距1
        rf1 = rValue;
        hd1 = hdValue;

        txtRf5.setText(String.format("%s", rValue));
        txtRfHD5.setText(String.format("%s", hdValue));

        mMeasureStationBean.setRf1(rValue);
        mMeasureStationBean.setRf1HD(hdValue);
    }

    //设置偶数点的第2个测量值B
    private void setEven2ndPointView(String rValue, String hdValue) {
        //偶数点模式后视值1以及视距2
        rb1 = rValue;
        hd2 = hdValue;

        //判断前视和后视的视距差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(hd1), Double.valueOf(hd2)))
                > Double.valueOf(Config.THRESHOLD_SETTING_FB_HD_DELTA)) {
            APP.toast("前后两次读取的视距差超过限定值", 0);
            measureTime--;
            return;
        }

        txtRb6.setText(String.format("%s", rValue));
        txtRbHD6.setText(String.format("%s", hdValue));

        mMeasureStationBean.setRb1(rValue);
        mMeasureStationBean.setRb1HD(hdValue);
    }

    //设置偶数点的第3个测量值B
    private void setEven3rdPointView(String rValue, String hdValue) {

        //偶数点模式后视值2以及视距3
        rb2 = rValue;
        hd3 = hdValue;

        //判断两次后视读数差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(rb2), Double.valueOf(rb1)))
                > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
            APP.toast("前后两次读取的后视值超过限定值", 0);
            measureTime--;
            return;
        }

        txtRb7.setText(String.format("%s", rValue));
        txtRbHD7.setText(String.format("%s", hdValue));

        mMeasureStationBean.setRb2(rValue);
        mMeasureStationBean.setRb2HD(hdValue);
    }

    //设置偶数点的第4个测量值
    private void setEven4thPointView(String time, String rValue, String hdValue) {
        //偶数点模式前视值2以及视距4
        rf2 = rValue;
        hd4 = hdValue;
        //判断两次前视读数差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(rf2), Double.valueOf(rf1)))
                > Double.valueOf(Config.THRESHOLD_SETTING_NEAR_R_DELTA)) {
            APP.toast("前后两次读取的后视值超过限定值", 0);
            measureTime--;
            return;
        }

        //判断前视和后视的视距差是否满足限定值
        if (Math.abs(Arith.sub(Double.valueOf(hd3), Double.valueOf(hd4)))
                > Double.valueOf(Config.THRESHOLD_SETTING_FB_HD_DELTA)) {
            APP.toast("前后两次读取的视距差超过限定值", 0);
            measureTime--;
            return;
        }

        txtRf8.setText(String.format("%s", rValue));
        txtRfHD8.setText(String.format("%s", hdValue));

        mMeasureStationBean.setRf2(rValue);
        mMeasureStationBean.setRf2HD(hdValue);

        //重置测点次数
        measureTime = 0;
        //切换到奇数点模式
        measureMode = Config.ODD_MEASURE_POINT_MODE;

        //如果当前点为拐点，增加拐点
        if (TextUtils.equals(currentMPB.getCdlx(), Config.ASSIST_POINT_TYPE)) {
            addAssistMPB();
        }

        mMeasureStationBean.setMeasureTime(time);
        calcAndSaveProcess();
    }



    private void startScan() {
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
    public void onAfterApplyAllPermission(int requestCode) {
        if (requestCode == Config.PERMISSIONS_REQUEST_COMMON_CODE) {
            startScan();
        }
    }

    //保存测量数据
    private void saveMeasureDataInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < measureStationBeanList.size(); i++) {
                        MeasureStationBean bean = measureStationBeanList.get(i);
                        if (i + 1 < selectedMPBList.size()) {
                            MeasurePointBean mpb = selectedMPBList.get(i + 1);
                            JSONObject object = new JSONObject();
                            object.put("sx", i + 1);
                            object.put("szxlid", levelingLineId);
                            object.put("cljlmc", measureRecordName);
                            object.put("cdmc", mpb.getCdmc());
                            object.put("cdid", mpb.getId());
                            object.put("clgc", String.valueOf(Arith.add(Double.valueOf(startMPB.getCdcsgc()), bean.getH())));
                            object.put("scclgc", mpb.getScclgc());
                            object.put("dycds", bean.getRf1());
                            object.put("decds", bean.getRf2());
                            object.put("dscds", bean.getRb1());
                            object.put("dsicds", bean.getRb2());
                            object.put("dyqssj", bean.getRf1HD());
                            object.put("dyhssj", bean.getRb1HD());
                            object.put("deqssj", bean.getRf2HD());
                            object.put("dehssj", bean.getRb2HD());
                            object.put("clry", mMeasureInfoBean.getOperaterName());
                            object.put("yxbhc", String.valueOf(fh_threshold));
                            object.put("cllx", "1");
                            object.put("cdlx", Integer.valueOf(mpb.getCdlx()));
                            object.put("jlzt", 1);
                            object.put("clsj", bean.getMeasureTime());

                            jsonArray.add(object);
                        } else {
                            break;
                        }
                    }
                    jsonObject.put("body", jsonArray);

                    String localURL = Config.SERVER_PREFIXX + "levelingLineLog/saveLingLineLogList";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token", Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            finishMeasure();
                        } else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "保存测量信息失败，错误原因：" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "responseBody is " + response.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为 " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    dismissWaitingDialog();
                }
            }
        }).start();
    }

    private void clearCache() {
        measureStationBeanList.clear();
        String jsonText = JSON.toJSONString(measureStationBeanList);
        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_data", levelingLineId), jsonText);

        selectedMPBList.clear();
        String jsonMPBText = JSON.toJSONString(selectedMPBList);
        ShareRefrenceUtil.save(mActivity.getApplicationContext(), String.format("%s_measure_list", levelingLineId), jsonMPBText);
        APP.toast("已完成测量", 0);
        finish();
    }

    private void finishMeasure() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    //水准线路id
                    jsonObject.put("szxlid", levelingLineId);

                    //测量记录名称
                    jsonObject.put("cljlmc", measureRecordName);

                    String localURL = Config.SERVER_PREFIXX + "levelingLineLog/checkLingLingLog";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token", Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);
                    } else {
                        Log.d(TAG, "response code is " + response.code()
                                + " , message is " + response.message());
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    clearCache();
                }
            }
        }).start();
    }


    //保存测量信息
    private void saveMeasureInfo(final MeasureInfoBean bean) {
        showWaitingDialog("正在保存测量信息中，请稍等...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    //水准线路id
                    jsonObject.put("szxlid", levelingLineId);

                    //测量记录名称
                    jsonObject.put("cljlmc", measureRecordName);
                    //仪器品牌
                    jsonObject.put("yqpp", bean.getInstrumentBrand());
                    //仪器型号
                    jsonObject.put("yqxh", bean.getInstrumentModel());
                    //温度
                    jsonObject.put("wd", bean.getTemperature());
                    //气压
                    jsonObject.put("qy", bean.getPressure());
                    //天气
                    jsonObject.put("tq", bean.getWeather());
                    //观测类型
                    jsonObject.put("gclx", bean.getMeasureType());
                    //司镜人员
                    jsonObject.put("sjry", bean.getOperaterName());
                    //设备序列号
                    jsonObject.put("sbxlh", bean.getOperaterName());
                    //工作基点名称序列
                    jsonObject.put("gzjdmcxl", workPointId);
                    //水准线路编码
                    jsonObject.put("szxlbm", levelingLineNo);

                    String localURL = Config.SERVER_PREFIXX + "measurement/save";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token", Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);
                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            saveMeasureDataInfo();
                        } else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "保存测量信息失败，错误原因： " + responseJson.getString("msg").toLowerCase(), Toast.LENGTH_LONG).show();
                                }
                            });
                            dismissWaitingDialog();
                        }

                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为 " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });

                        dismissWaitingDialog();
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    dismissWaitingDialog();
                } finally {
                }
            }
        }).start();
    }
}
