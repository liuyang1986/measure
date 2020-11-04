package win.lioil.bluetooth.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.activity.Measure2ndActivity;
import win.lioil.bluetooth.activity.MeasureActivity;
import win.lioil.bluetooth.bean.DetailBean;
import win.lioil.bluetooth.bean.LevelingRouteBean;
import win.lioil.bluetooth.bean.MeasureInfoBean;
import win.lioil.bluetooth.bean.MeasurePointBean;
import win.lioil.bluetooth.bean.WorkPointBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Util;

public class LevelingOptionDialog extends Dialog {

    private static final String TAG = "Test OptionDialog";
    private static final int PAGE_SIZE = 400;

    private LinearLayout llProject;
    private Button btnProject;

    private LinearLayout llSection;
    private Button btnSection;

    private LinearLayout llWorkPoint;
    private Button btnWorkPoint;

    private LinearLayout llLevelingLine;
    private Button btnLeveling;
    private Button btnMeasure;

    private TextView txtLevelingNo;

    private Activity mActivity;

    private ProgressDialog mProgressDialog;

    //    private List<XMBean> xmBeanList = new ArrayList<>();
//
//    private List<BDBean> bdBeanList = new ArrayList<>();
//
//    private List<GDBean> gdBeanList = new ArrayList<>();
    private List<WorkPointBean> workPointBeanList = new ArrayList<>();

    private List<LevelingRouteBean> levelingBeanList = new ArrayList<>();

    private MeasurePointBean measurePointBean;

    private List<DetailBean> projectBeanList = new ArrayList<>();

    private List<DetailBean> sectionBeanList = new ArrayList<>();

    private List<WorkPointBean> currentSectionWorkBeanList = new ArrayList<>();

    private DetailBean selectedProjectBean;

    private DetailBean selectedSectionBean;

    private WorkPointBean selectedWorkPointBean;

    private LevelingRouteBean selectedLevelingRouteBean;

    private int mode;


    public LevelingOptionDialog(@NonNull Activity activity,int mode) {
        super(activity);
        mActivity = activity;
        this.mode = mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.leveling_line_option_dialog);
        setTitle("");
        init();
    }

    private void init() {
        llProject = findViewById(R.id.ll_project);
        btnProject = findViewById(R.id.project_btn);

        llSection = findViewById(R.id.ll_section);
        btnSection = findViewById(R.id.section_btn);

        llWorkPoint = findViewById(R.id.ll_workpoint);
        btnWorkPoint = findViewById(R.id.workpoint_btn);

        llLevelingLine = findViewById(R.id.ll_levelingline);
        btnLeveling = findViewById(R.id.leveling_btn);

        txtLevelingNo = findViewById(R.id.levelingline_name);

        btnMeasure = findViewById(R.id.button_measure);

        btnProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProjectRadioDialog();
            }
        });

        btnSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSectionRadioDialog();
            }
        });

        btnWorkPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkPointRadioDialog();
            }
        });

        btnLeveling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLevelingLineRadioDialog();
            }
        });


        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (levelingBeanList.isEmpty()) {
                    APP.toast("水准线路为空，请选择有效的水准线路", 0);
                    return;
                }

                if (selectedLevelingRouteBean == null) {
                    APP.toast("水准线路为空，请选择有效的水准线路", 0);
                    return;
                }

//                showMeasureInfoEditDialog();
                skipToMeasureActivity();
            }
        });

        initProjectBeanList();

        if (projectBeanList.isEmpty()) {
            initSectionBeanList();
        } else {
            onProjectBeanOption(projectBeanList.get(0));
        }

        if (sectionBeanList.isEmpty()) {
            initSectionBeanList();
        } else {
            onSectionBeanOption(sectionBeanList.get(0));
        }

        if (currentSectionWorkBeanList.isEmpty()) {
            currentSectionWorkBeanList.addAll(Util.getAllWorkPointListData());
        } else {
            requestLevelingBeanList(currentSectionWorkBeanList.get(0));
        }

    }

    private void showMeasureInfoEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.measure_info_edit_dialog, null);
        builder.setView(view);

        final EditText edtMeasureName = view.findViewById(R.id.measure_name);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        edtMeasureName.setText(String.format("%s", sdf.format(new Date())));

        final EditText edtInstrumentBrand = view.findViewById(R.id.instrument_brand);

        final EditText edtInstrumentModel = view.findViewById(R.id.instrument_model);

        final EditText edtTemperature = view.findViewById(R.id.temperature);

        final EditText edtPressure = view.findViewById(R.id.pressure);

        final Spinner spinnerWeather = view.findViewById(R.id.weather);
        final List<String> nameList = new ArrayList<>();
        nameList.add("晴");
        nameList.add("阴");
        nameList.add("雨");
        nameList.add("雪");
        nameList.add("风");
        nameList.add("其它");

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

                Log.d(TAG, "project pos is " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerWeather.setSelection(0);

        final EditText edtOperaterName = view.findViewById(R.id.operater_name);

        final EditText edtSerialNo = view.findViewById(R.id.serial_no);

        TextView btnOK = view.findViewById(R.id.txt_ok);

        final Dialog dialog = builder.create();
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click dialog ok");

                final String measureName = edtMeasureName.getText().toString();
                final String instrumentBrand = edtInstrumentBrand.getText().toString();
                final String instrumentModel = edtInstrumentModel.getText().toString();
                final String temperature = edtTemperature.getText().toString();
                final String pressure = edtPressure.getText().toString();
                final String weather = nameList.get(spinnerWeather.getSelectedItemPosition());
                final String operaterName = edtOperaterName.getText().toString();
                final String serialNo = edtSerialNo.getText().toString();

                if (TextUtils.isEmpty(measureName))
                {
                    APP.toast("请输入测量记录信息",0);
                    return ;
                }

                if (TextUtils.isEmpty(instrumentBrand))
                {
                    APP.toast("请输入仪器品牌",0);
                    return ;
                }

                if (TextUtils.isEmpty(instrumentModel))
                {
                    APP.toast("请输入仪器型号",0);
                    return ;
                }

                if (TextUtils.isEmpty(temperature))
                {
                    APP.toast("请输入当前温度",0);
                    return ;
                }

                if (TextUtils.isEmpty(pressure))
                {
                    APP.toast("请输入当前气压",0);
                    return ;
                }

                if (TextUtils.isEmpty(operaterName))
                {
                    APP.toast("请输入司镜人员姓名",0);
                    return ;
                }

                if (TextUtils.isEmpty(serialNo))
                {
                    APP.toast("请输入设备序列号",0);
                    return ;
                }

                MeasureInfoBean bean = new MeasureInfoBean();
                bean.setInstrumentBrand(instrumentBrand);
                bean.setInstrumentModel(instrumentModel);
                bean.setLevelingLineId(selectedLevelingRouteBean.getId());
                bean.setLevelingNo(selectedLevelingRouteBean.getSzxlbh());
                bean.setMeasureName(measureName);
                bean.setMeasureType("");
                bean.setOperaterName(operaterName);
                bean.setSerialNo(serialNo);
                bean.setTemperature(temperature);
                bean.setPressure(pressure);
                bean.setWeather(weather);
                bean.setWorkPointNo(selectedWorkPointBean.getGdid());

                saveMeasureInfo(bean);

                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void saveMeasureInfo(final MeasureInfoBean bean) {
        showWaitingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("szxlid", selectedLevelingRouteBean.getId());
                    //测量记录名称
                    jsonObject.put("cljlmc", bean.getMeasureName());
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
                    jsonObject.put("gzjdmcxl", selectedWorkPointBean.getGdid());
                    //水准线路编码
                    jsonObject.put("szxlbm", selectedLevelingRouteBean.getSzxlbh());

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

//                        skipToMeasureActivity(bean);
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为 " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    mActivity.runOnUiThread(new Runnable() {
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

    private void skipToMeasureActivity() {
        Intent intent = new Intent(mActivity, mode==0?MeasureActivity.class: Measure2ndActivity.class);
        intent.putExtra("levelingLineId", selectedLevelingRouteBean.getId());
        intent.putExtra("workPointId", selectedWorkPointBean.getGdid());
        intent.putExtra("levelingLineNo", selectedLevelingRouteBean.getSzxlbh());
        mActivity.startActivity(intent);
        dismiss();
    }

    private void initProjectBeanList() {
        workPointBeanList = new ArrayList<>();
        workPointBeanList.addAll(Util.getAllWorkPointListData());

        if (!workPointBeanList.isEmpty()) {
            Log.d(TAG, "workPointBeanList size is " + workPointBeanList.size());
            projectBeanList = new ArrayList<>();
            projectBeanList.add(convertWorkPointBeanToProjectBean(workPointBeanList.get(0)));
            //找相同标段id的工点
            for (WorkPointBean workPointBean : workPointBeanList) {
                boolean bFindFlag = false;
                for (DetailBean projectBean : projectBeanList) {
                    if (TextUtils.equals(projectBean.getId(), workPointBean.getXmid())) {
                        bFindFlag = true;
                        break;
                    }
                }

                if (!bFindFlag) {
                    projectBeanList.add(convertWorkPointBeanToProjectBean(workPointBean));
                }
            }
            Log.d(TAG, "sectionList size is " + projectBeanList.size());

            btnProject.setText(projectBeanList.get(0).getName());
        } else {
            llProject.setVisibility(View.GONE);
        }
    }

    private void onProjectBeanOption(DetailBean projectDetailBean) {
        selectedProjectBean = projectDetailBean;
        sectionBeanList = new ArrayList<>();
        //找相同标段id的工点
        for (WorkPointBean workPointBean : workPointBeanList) {
            if (TextUtils.equals(projectDetailBean.getId(), workPointBean.getXmid())) {
                sectionBeanList.add(convertWorkPointBeanToSectionBean(workPointBean));
            }
        }

        if (sectionBeanList.isEmpty()) {
            llSection.setVisibility(View.GONE);
        } else {
            btnSection.setText(sectionBeanList.get(0).getName());
            onSectionBeanOption(sectionBeanList.get(0));
        }
    }


    private void initSectionBeanList() {
        if (!workPointBeanList.isEmpty()) {
            Log.d(TAG, "workPointBeanList size is " + workPointBeanList.size());
            sectionBeanList = new ArrayList<>();
            sectionBeanList.add(convertWorkPointBeanToSectionBean(workPointBeanList.get(0)));
            //找相同标段id的工点
            for (WorkPointBean workPointBean : workPointBeanList) {
                boolean bFindFlag = false;
                for (DetailBean sectionBean : sectionBeanList) {
                    if (TextUtils.equals(sectionBean.getId(), workPointBean.getBdid())) {
                        bFindFlag = true;
                        break;
                    }
                }

                if (!bFindFlag) {
                    sectionBeanList.add(convertWorkPointBeanToSectionBean(workPointBean));
                }
            }
            Log.d(TAG, "sectionList size is " + sectionBeanList.size());

            btnSection.setText(sectionBeanList.get(0).getName());
        } else {
            llSection.setVisibility(View.GONE);
        }
    }

    private void onSectionBeanOption(DetailBean sectionDetailBean) {
        selectedSectionBean = sectionDetailBean;
        currentSectionWorkBeanList = new ArrayList<>();
        //找相同标段id的工点
        for (WorkPointBean workPointBean : workPointBeanList) {
            if (TextUtils.equals(sectionDetailBean.getId(), workPointBean.getBdid())) {
                currentSectionWorkBeanList.add(workPointBean);
            }
        }

        if (currentSectionWorkBeanList.isEmpty()) {
            btnWorkPoint.setText("");
        } else {
            btnWorkPoint.setText(currentSectionWorkBeanList.get(0).getGdmc());
            requestLevelingBeanList(currentSectionWorkBeanList.get(0));
        }
    }

    private void showProjectRadioDialog() {
//        synchronized (xmBeanList)
//        {
//            if (xmBeanList.isEmpty())
//            {
//                return ;
//            }
//
//            final String[] projectNames = new String[xmBeanList.size()];
//            for (int i=0; i<xmBeanList.size(); i++)
//            {
//                projectNames[i] = xmBeanList.get(i).getXmmc();
//            }
//
//            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
//            alertBuilder.setTitle("项目列表");
//            alertBuilder.setSingleChoiceItems(projectNames, 0, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    btnProject.setText(projectNames[i]);
//                    requestBDDataByProjectId(xmBeanList.get(i).getId());
//                    dialogInterface.dismiss();
//                }
//            });
//
//            alertBuilder.show();
//        }

        final String[] projectNames = new String[projectBeanList.size()];
        for (int i = 0; i < projectBeanList.size(); i++) {
            projectNames[i] = projectBeanList.get(i).getName();
        }

        int checkedItem = 0;
        if (selectedProjectBean == null) {
            checkedItem = 0;
        } else {
            for (int j = 0; j < projectBeanList.size(); j++) {
                if (TextUtils.equals(selectedProjectBean.getId(), projectBeanList.get(j).getId())) {
                    checkedItem = j;
                    break;
                }
            }
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
        alertBuilder.setTitle("项目列表");
        alertBuilder.setSingleChoiceItems(projectNames, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btnProject.setText(projectNames[i]);
                onProjectBeanOption(projectBeanList.get(i));
                dialogInterface.dismiss();
            }
        });

        alertBuilder.show();
    }


    private void showSectionRadioDialog() {
//        synchronized (bdBeanList)
//        {
//            if (bdBeanList.isEmpty())
//            {
//                return ;
//            }
//
//            final String[] sectionNames = new String[bdBeanList.size()];
//            for (int i=0; i<bdBeanList.size(); i++)
//            {
//                sectionNames[i] = bdBeanList.get(i).getBdmc();
//            }
//
//            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
//            alertBuilder.setTitle("标段列表");
//            alertBuilder.setSingleChoiceItems(sectionNames, 0, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    btnSection.setText(sectionNames[i]);
//                    requestWorkPointsBySectionId(bdBeanList.get(i).getId());
//                    dialogInterface.dismiss();
//                }
//            });
//
//            alertBuilder.show();
//        }

        final String[] sectionNames = new String[sectionBeanList.size()];
        for (int i = 0; i < sectionBeanList.size(); i++) {
            sectionNames[i] = sectionBeanList.get(i).getName();
        }

        int checkedItem = 0;
        if (selectedSectionBean == null) {
            checkedItem = 0;
        } else {
            for (int j = 0; j < sectionBeanList.size(); j++) {
                if (TextUtils.equals(selectedSectionBean.getId(), sectionBeanList.get(j).getId())) {
                    checkedItem = j;
                    break;
                }
            }
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
        alertBuilder.setTitle("标段列表");
        alertBuilder.setSingleChoiceItems(sectionNames, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btnSection.setText(sectionNames[i]);
                onSectionBeanOption(sectionBeanList.get(i));
                dialogInterface.dismiss();
            }
        });

        alertBuilder.show();
    }

    private void showWorkPointRadioDialog() {
//        synchronized (gdBeanList)
//        {
//            if (gdBeanList.isEmpty())
//            {
//                return ;
//            }
//
//            final String[] workPointNames = new String[gdBeanList.size()];
//            for (int i=0; i<gdBeanList.size(); i++)
//            {
//                workPointNames[i] = gdBeanList.get(i).getGdmc();
//            }
//
//            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
//            alertBuilder.setTitle("工点列表");
//            alertBuilder.setSingleChoiceItems(workPointNames, 0, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    btnWorkPoint.setText(workPointNames[i]);
//                    requestLevelingBeanList(gdBeanList.get(i).getId());
//                    dialogInterface.dismiss();
//                }
//            });
//
//            alertBuilder.show();
//        }

        final String[] workPointNames = new String[currentSectionWorkBeanList.size()];
        for (int i = 0; i < currentSectionWorkBeanList.size(); i++) {
            workPointNames[i] = currentSectionWorkBeanList.get(i).getGdmc();
        }

        int checkedItem = 0;
        for (int j = 0; j < currentSectionWorkBeanList.size(); j++) {
            if (TextUtils.equals(currentSectionWorkBeanList.get(j).getGdid(), selectedWorkPointBean.getGdid())) {
                checkedItem = j;
                break;
            }
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
        alertBuilder.setTitle("工点列表");
        alertBuilder.setSingleChoiceItems(workPointNames, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                btnWorkPoint.setText(workPointNames[i]);
                requestLevelingBeanList(currentSectionWorkBeanList.get(i));
                dialogInterface.dismiss();
            }
        });

        alertBuilder.show();
    }

    private void showLevelingLineRadioDialog() {
        synchronized (levelingBeanList) {
            if (levelingBeanList.isEmpty()) {
                return;
            }

            final String[] levelingNames = new String[levelingBeanList.size()];
            for (int i = 0; i < levelingBeanList.size(); i++) {
                levelingNames[i] = levelingBeanList.get(i).getSzxlmc();
            }

            int chechedItem = 0;

            if (selectedLevelingRouteBean != null) {
                for (int j = 0; j < levelingBeanList.size(); j++) {
                    if (TextUtils.equals(selectedLevelingRouteBean.getSzxlbh(), levelingBeanList.get(j).getSzxlbh())) {
                        chechedItem = j;
                        break;
                    }
                }
            }


            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mActivity);
            alertBuilder.setTitle("水准线路列表");
            alertBuilder.setSingleChoiceItems(levelingNames, chechedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedLevelingRouteBean = levelingBeanList.get(i);
                    btnLeveling.setText(String.format("水准线路编号：", levelingNames[i]));
                    dialogInterface.dismiss();
                }
            });

            alertBuilder.show();
        }
    }

    private void showWaitingDialog() {
        if (mActivity == null) {
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(mActivity);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage("查询中，请稍等...");
                }
                mProgressDialog.show();
            }
        });
    }

    private void dismissWaitingDialog() {
        if (mActivity == null) {
            return;
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private DetailBean convertWorkPointBeanToProjectBean(WorkPointBean workPointBean) {
        DetailBean detailBean = new DetailBean();
        detailBean.setName(workPointBean.getXmmc());
        detailBean.setId(workPointBean.getXmid());
        return detailBean;
    }

    private DetailBean convertWorkPointBeanToSectionBean(WorkPointBean workPointBean) {
        DetailBean detailBean = new DetailBean();
        detailBean.setName(workPointBean.getBdmc());
        detailBean.setId(workPointBean.getBdid());
        return detailBean;
    }


//    //查看项目下标段
//    private void requestBDDataByProjectId(final String projectId) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                final List<BDBean> sectionBeanList = new ArrayList<>();
//                try {
//                    jsonObject.put("xmid", projectId);
//                    jsonObject.put("pageNow", 1);
//                    jsonObject.put("pageSize", PAGE_SIZE);
//
//                    String localURL = Config.SERVER_PREFIXX + "bidSection/selectBidSectionByPorjectId";
//
//                    String requestJSon = jsonObject.toString();
//                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
//                    headersbuilder.add("token",Config.AUTH_TOKEN);
//                    Headers headers = headersbuilder.build();
//                    Request request = new Request.Builder().url(localURL).post(
//                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();
//
//                    final Response response = client.newCall(request).execute();
//
//                    if (response.isSuccessful()) {
//                        String responseBody = response.body().string();
//                        Log.d(TAG, "responseBody is " + responseBody);
//
//                        final JSONObject responseJson = JSON.parseObject(responseBody);
//                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
//                            JSONObject dataJSON = responseJson.getJSONObject("data");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
//                                sectionBeanList.add(bean);
//                            }
//                        } else {
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//                            synchronized (bdBeanList)
//                            {
//                                bdBeanList.clear();
//                                bdBeanList.addAll(sectionBeanList);
//                                if (!bdBeanList.isEmpty())
//                                {
//                                    btnSection.setText(bdBeanList.get(0).getBdmc());
//                                }
//                            }
//                        }
//                    });
//
//                }
//            }
//        }).start();
//    }
//
//    //查标段下所有工点
//    private void requestWorkPointsBySectionId(final String sectionId) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                final List<GDBean> workPointList = new ArrayList<>();
//                try {
//                    jsonObject.put("bdid", sectionId);
//                    jsonObject.put("pageNow", 1);
//                    jsonObject.put("pageSize", PAGE_SIZE);
//
//                    String localURL = Config.SERVER_PREFIXX + "workPoints/selectWorkPointsBySectionId";
//
//                    String requestJSon = jsonObject.toString();
//
//                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
//                    headersbuilder.add("token",Config.AUTH_TOKEN);
//                    Headers headers = headersbuilder.build();
//                    Request request = new Request.Builder().url(localURL).post(
//                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();
//
//                    final Response response = client.newCall(request).execute();
//
//                    if (response.isSuccessful()) {
//                        String responseBody = response.body().string();
//                        Log.d(TAG, "responseBody is " + responseBody);
//
//                        final JSONObject responseJson = JSON.parseObject(responseBody);
//                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
//                            JSONObject dataJSON = responseJson.getJSONObject("data");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
//                                workPointList.add(bean);
//                            }
//                        } else {
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//                            synchronized (gdBeanList)
//                            {
//                                gdBeanList.clear();
//                                gdBeanList.addAll(workPointList);
//                                if (!gdBeanList.isEmpty())
//                                {
//                                    btnWorkPoint.setText(gdBeanList.get(0).getGdmc());
//                                }
//                                }
//
//                        }
//                    });
//                }
//            }
//        }).start();
//    }

    //根据工点查询水准线路列表
    private void requestLevelingBeanList(final WorkPointBean workPointBean) {
        selectedWorkPointBean = workPointBean;
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                final List<LevelingRouteBean> levelingRouteBeanList = new ArrayList<>();
                try {
                    jsonObject.put("gdid", workPointBean.getGdid());
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "levelingLine/levelingLineAllByBidSeId";

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
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                LevelingRouteBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(), LevelingRouteBean.class);
                                levelingRouteBeanList.add(bean);
                            }
                        } else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissWaitingDialog();
                            synchronized (levelingBeanList) {
                                levelingBeanList.clear();
                                levelingBeanList.addAll(levelingRouteBeanList);

                                if (!levelingBeanList.isEmpty()) {
                                    selectedLevelingRouteBean = levelingRouteBeanList.get(0);
                                    btnLeveling.setText(levelingRouteBeanList.get(0).getSzxlmc());
                                    txtLevelingNo.setText(String.format("水准线路编号：%s", levelingRouteBeanList.get(0).getSzxlbh()));
                                } else {
                                    btnLeveling.setText("");
                                    txtLevelingNo.setText("未选择有效水准线路");
                                }
                            }
                        }
                    });

                }
            }
        }).start();
    }

//    //根据水准线路id查询测点列表
//    private void requestMeasurePointList(final String levelingId) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("szlxid", levelingId);
//
//                    String localURL = Config.SERVER_PREFIXX + "levelingLineForm/showRouteFormByLineId";
//
//                    String requestJSon = jsonObject.toString();
//
//                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
//                    headersbuilder.add("token",Config.AUTH_TOKEN);
//                    Headers headers = headersbuilder.build();
//                    Request request = new Request.Builder().url(localURL).post(
//                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();
//
//                    Log.d(TAG, "requestUrl is " + localURL + " , requestJSon is " + requestJSon);
//                    final Response response = client.newCall(request).execute();
//
//                    if (response.isSuccessful()) {
//                        String responseBody = response.body().string();
//                        Log.d(TAG, "responseBody is " + responseBody);
//
//                        final JSONObject responseJson = JSON.parseObject(responseBody);
//                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
//                            JSONObject dataJSON = responseJson.getJSONObject("data");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                MeasurePointBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(),MeasurePointBean.class);
//                                mMeasurePointList.add(bean);
//                            }
//                        } else {
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        mActivity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    mActivity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//                        }
//                    });
//
//                }
//            }
//        }).start();
//    }

//    private void initProjectOption(final List<XMBean> projectBeanList)
//    {
//        List<String> nameList = new ArrayList<>();
//        for(XMBean bean : projectBeanList)
//        {
//            nameList.add(bean.getXmmc());
//        }
//
//        xmAdapter = new ArrayAdapter<>(mActivity,
//                android.R.layout.simple_spinner_item,nameList);
//        xmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mProjectSpin.setAdapter(xmAdapter);
//        mProjectSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position>=projectBeanList.size())
//                {
//                    return ;
//                }
//
//                Log.d(TAG,"project pos is " + position);
//                requestBDDataByProjectId(projectBeanList.get(position).getId());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        mProjectSpin.setSelection(0);
//    }
//
//    private void initSectionOption(final List<BDBean> sectionBeanList)
//    {
//        List<String> nameList = new ArrayList<>();
//        for(BDBean bdBean : sectionBeanList)
//        {
//            nameList.add(bdBean.getBdmc());
//        }
//
//        bdAdapter = new ArrayAdapter<>(mActivity,
//                android.R.layout.simple_spinner_item,nameList);
//        bdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSectionSpin.setAdapter(bdAdapter);
//        mSectionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position >= sectionBeanList.size())
//                {
//                    return ;
//                }
//
//                Log.d(TAG,"section pos is " + position);
//                requestWorkPointsBySectionId(sectionBeanList.get(position).getId());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        mSectionSpin.setSelection(0);
//    }
//
//    private void initLevelingOption(final List<LevelingRouteBean> levelingRouteBeanList)
//    {
//        List<String> nameList = new ArrayList<>();
//        for(LevelingRouteBean levelingBean : levelingRouteBeanList)
//        {
//            nameList.add(levelingBean.getSzxlmc());
//        }
//
//        levelingAdapter = new ArrayAdapter<>(mActivity,
//                android.R.layout.simple_spinner_item,nameList);
//        levelingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mLevelingSpin.setAdapter(gdAdapter);
//        mLevelingSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//                if (position >= levelingRouteBeanList.size())
//                {
//                    return ;
//                }
//
//                Log.d(TAG,"workpoint pos is " + position);
//                requestLevelingBeanList(levelingRouteBeanList.get(position).getId());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        mLevelingSpin.setSelection(0);
//    }
//
//    private void initWorkPointOption(final List<GDBean> workPointList)
//    {
//        List<String> nameList = new ArrayList<>();
//        for(GDBean gdBean : workPointList)
//        {
//            nameList.add(gdBean.getGdmc());
//        }
//
//        gdAdapter = new ArrayAdapter<>(mActivity,
//                android.R.layout.simple_spinner_item,nameList);
//        gdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mWorkPointSpin.setAdapter(gdAdapter);
//        mWorkPointSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position >= workPointList.size())
//                {
//                    return ;
//                }
//
//                Log.d(TAG,"workpoint pos is " + position);
//                requestLevelingBeanList(workPointList.get(position).getId());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        mWorkPointSpin.setSelection(0);
//    }
}
