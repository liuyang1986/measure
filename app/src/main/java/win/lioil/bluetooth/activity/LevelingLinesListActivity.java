package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.adapter.CommonAdapter;
import win.lioil.bluetooth.adapter.MeasurePointAdapter;
import win.lioil.bluetooth.bean.BDBean;
import win.lioil.bluetooth.bean.GDBean;
import win.lioil.bluetooth.bean.LevelingRouteBean;
import win.lioil.bluetooth.bean.MeasurePointBean;
import win.lioil.bluetooth.bean.SubGradeBean;
import win.lioil.bluetooth.bean.XMBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.HRecyclerView;

public class LevelingLinesListActivity extends Activity {
    private static final String TAG = "Test LevelingActivity";

    private LinearLayout llBack;

    private TextView txtTitle;

    private RefreshLayout refreshLayout;

    private HRecyclerView hRecyclerView;

    private MeasurePointAdapter adapter;

    private LinearLayout llOption;

    private Spinner mProjectSpin,mSectionSpin,mWorkPointSpin,mLevelingSpin;

    private static final int PAGE_SIZE = 400;


    private ProgressDialog mProgressDialog;

    private List<MeasurePointBean> mMeasurePointList = new ArrayList<>();

    private String workPointId;

    private int requestType;

    private String projectName;

    private String sectionName;

    private String workPointName;

    private Spinner projectSpin,sectionSpin,workPointSpin,levelingSpin;

    private ArrayAdapter<String> xmAdapter;
    private ArrayAdapter<String> bdAdapter;
    private ArrayAdapter<String> gdAdapter;
    private ArrayAdapter<String> levelingAdapter;

    private MeasurePointBean measurePointBean;

    private LevelingLinesListActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levelingline);

        mActivity = this;

        Intent intent = getIntent();
        requestType = intent.getIntExtra("requestType", Config.SUBGRADE_TYPE);
        projectName = intent.getStringExtra("projectName");
        sectionName = intent.getStringExtra("sectionName");
        workPointName = intent.getStringExtra("workPointName");
        workPointId = intent.getStringExtra("requestID");

        txtTitle = findViewById(R.id.txt_title);

        llBack = findViewById(R.id.ll_back);

        llOption = findViewById(R.id.ll_option);

        projectSpin = findViewById(R.id.project_spin);

        sectionSpin = findViewById(R.id.section_spin);

        workPointSpin = findViewById(R.id.workpoint_spin);

        mProjectSpin = findViewById(R.id.project_spin);

        mWorkPointSpin = findViewById(R.id.workpoint_spin);

        mSectionSpin = findViewById(R.id.section_spin);

        mLevelingSpin = findViewById(R.id.leveling_spin);

        refreshLayout = findViewById(R.id.refreshLayout);

        hRecyclerView=  findViewById(R.id.id_hrecyclerview);

        refreshLayout.setRefreshHeader(new ClassicsHeader(this).setSpinnerStyle(SpinnerStyle.Scale));

        refreshLayout.setRefreshFooter(new ClassicsFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        //设置样式后面的背景颜色
        refreshLayout.setPrimaryColorsId(R.color.background_color, android.R.color.white);

        hRecyclerView.setHeaderListData(getResources().getStringArray(R.array.measure_point_title));

        adapter = new MeasurePointAdapter(mActivity, mMeasurePointList, R.layout.measure_point_item_layout, new CommonAdapter.CommonViewHolder.onItemCommonClickListener() {
            @Override
            public void onItemClickListener(int position) {
                if (position >= mMeasurePointList.size())
                {
                    return ;
                }
                measurePointBean = mMeasurePointList.get(position);
                showAlertDialog("测点信息",parseMeasureBeanToMessage(measurePointBean));
            }

            @Override
            public void onItemLongClickListener(int position) {

            }
        });

        hRecyclerView.setAdapter(adapter);


        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestMeasurePointList(measurePointBean.getId());
            }
        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (requestType != Config.XM_TYPE)
        {
            llOption.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(projectName) && !TextUtils.isEmpty(sectionName)
                    && !TextUtils.isEmpty(workPointName))
            {
                txtTitle.setText(String.format("%s项目>%s标段>%s工点断面列表",projectName,sectionName,workPointName));
            }
            else if (TextUtils.isEmpty(projectName))
            {
                if (TextUtils.isEmpty(sectionName))
                {
                    txtTitle.setText(String.format("%s工点断面列表",workPointName));
                }
                else
                {
                    txtTitle.setText(String.format("%s标段>%s工点断面列表",sectionName,workPointName));
                }
            }
        }
        else
        {
            txtTitle.setText("基础信息");
        }
    }


    private void showAlertDialog(final String title,final String messageContent)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(title);
                builder.setMessage(messageContent);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"click dialog ok");
                    }
                });
                builder.show();
            }
        });
    }

    private void showWaitingDialog() {
        runOnUiThread(new Runnable() {
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
    protected void onResume() {
        super.onResume();
        mMeasurePointList.clear();

        if (Config.XM_TYPE == requestType)
        {
            initProjectOption(Util.getAppCacheProjectListData());
            initSectionOption(Util.getAppCacheSectionListData());
            initWorkPointOption(Util.getAppCacheWorkPointListData());
        }
        else
        {
            requestLevelingBeanList(workPointId);
        }

//        workPointId = "5810278f376846f6ab55b137c930a15c";
//        requestLevelingBeanList(workPointId);
    }

    private void initProjectOption(final List<XMBean> projectBeanList)
    {
        List<String> nameList = new ArrayList<>();
        for(XMBean bean : projectBeanList)
        {
            nameList.add(bean.getXmmc());
        }

        xmAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item,nameList);
        xmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProjectSpin.setAdapter(xmAdapter);
        mProjectSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position>=projectBeanList.size())
                {
                    return ;
                }

                Log.d(TAG,"project pos is " + position);
                requestBDDataByProjectId(projectBeanList.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mProjectSpin.setSelection(0);
    }
    
    private void initSectionOption(final List<BDBean> sectionBeanList)
    {
        List<String> nameList = new ArrayList<>();
        for(BDBean bdBean : sectionBeanList)
        {
            nameList.add(bdBean.getBdmc());
        }

        bdAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item,nameList);
        bdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSectionSpin.setAdapter(bdAdapter);
        mSectionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= sectionBeanList.size())
                {
                    return ;
                }

                Log.d(TAG,"section pos is " + position);
                requestWorkPointsBySectionId(sectionBeanList.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSectionSpin.setSelection(0);
    }

    private void initLevelingOption(final List<LevelingRouteBean> levelingRouteBeanList)
    {
        List<String> nameList = new ArrayList<>();
        for(LevelingRouteBean levelingBean : levelingRouteBeanList)
        {
            nameList.add(levelingBean.getSzxlmc());
        }

        levelingAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item,nameList);
        levelingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevelingSpin.setAdapter(gdAdapter);
        mLevelingSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position >= levelingRouteBeanList.size())
                {
                    return ;
                }

                Log.d(TAG,"workpoint pos is " + position);
                requestLevelingBeanList(levelingRouteBeanList.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mLevelingSpin.setSelection(0);
    }

    private void initWorkPointOption(final List<GDBean> workPointList)
    {
        List<String> nameList = new ArrayList<>();
        for(GDBean gdBean : workPointList)
        {
            nameList.add(gdBean.getGdmc());
        }

        gdAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item,nameList);
        gdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWorkPointSpin.setAdapter(gdAdapter);
        mWorkPointSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= workPointList.size())
                {
                    return ;
                }

                Log.d(TAG,"workpoint pos is " + position);
                requestLevelingBeanList(workPointList.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mWorkPointSpin.setSelection(0);
    }


    //查看项目下标段
    private void requestBDDataByProjectId(final String projectId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                final List<BDBean> sectionBeanList = new ArrayList<>();
                try {
                    jsonObject.put("xmid", projectId);
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "bidSection/selectBidSectionByPorjectId";

                    String requestJSon = jsonObject.toString();
                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
                                sectionBeanList.add(bean);
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
                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
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
                            dismissWaitingDialog();
                            initSectionOption(sectionBeanList);
                        }
                    });

                }
            }
        }).start();
    }

    //查标段下所有工点
    private void requestWorkPointsBySectionId(final String sectionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                List<GDBean> workPointList = new ArrayList<>();
                try {
                    jsonObject.put("bdid", sectionId);
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "workPoints/selectWorkPointsBySectionId";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
                    Headers headers = headersbuilder.build();
                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).headers(headers).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
                                workPointList.add(bean);
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
                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
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
                            dismissWaitingDialog();
                        }
                    });
                }
            }
        }).start();
    }



    private String parseMeasureBeanToMessage(MeasurePointBean bean) {
        StringBuilder sb = new StringBuilder();
        sb.append("测点名称：").append(bean.getCdmc()).append("\n").
                append("测点类型：").append(bean.getCdlx()).append("\n").
                append("测点高程：").append(bean.getCdcsgc()).append("\n").
                append("上次测量高程：").append(bean.getScclgc()).append("\n");
        return sb.toString();
    }

    private String parseLevelingBeanToMessage(LevelingRouteBean bean) {
        String dllx = bean.getDllx();
        if (TextUtils.equals(dllx,"2"))
        {
            dllx = "山地";
        }
        else
        {
            dllx = "平原";
        }

        String zy = bean.getZy();
        if (TextUtils.equals(dllx,"2"))
        {
            zy = "桥涵";
        }
        else if(TextUtils.equals(dllx,"3"))
        {
            zy = "隧道";
        }
        else
        {
            zy = "路基";
        }

        String state = bean.getXlzt();
        if (TextUtils.equals(state,"2"))
        {
            state = "停用";
        }
        else if (TextUtils.equals(state,"3"))
        {
            state = "删除";
        }
        else
        {
            state = "正常";
        }

        String startTime = bean.getTjsj();
        if (startTime.contains(",")) {
            startTime = startTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        String endTime = bean.getXgsj();
        if (endTime.contains(",")) {
            endTime = endTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("水准线路名称：").append(bean.getSzxlmc()).append("\n").
                append("水准线路编号：").append(bean.getSzxlbh()).append("\n").
                append("地理类型：").append(dllx).append("\n").
                append("测点数：").append(bean.getCds()).append("\n").
                append("工作基点数：").append(bean.getGzjds()).append("\n").
                append("专业：").append(zy).append("\n").
                append("起里程：").append(bean.getQslc()).append("\n").
                append("止里程：").append(bean.getJslc()).append("\n").
                append("创建时间：").append(startTime).append("\n").
                append("修改时间：").append(endTime).append("\n").
                append("设置人员：").append(bean.getSzry()).append("\n").
                append("状态：").append(state).append("\n");
        return sb.toString();
    }


    private String parseSubGradeBeanToMessage(SubGradeBean bean) {
        String state = bean.getGczt();
        if (TextUtils.equals(state,"1"))
        {
            state = "在测";
        }
        else
        {
            state = "停测";
        }

        String type = bean.getDmlx();
        if (TextUtils.equals(type,"1"))
        {
            type = "路基";
        }
        else if (TextUtils.equals(type,"2"))
        {
            type = "桥涵";
        }
        else if (TextUtils.equals(type,"3"))
        {
            type = "隧道";
        }
        else if (TextUtils.equals(type,"4"))
        {
            type = "过渡段";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("断面名称：").append(bean.getDmmc()).append("\n").
                append("断面编码：").append(bean.getDmbm()).append("\n").
                append("断面类型：").append(type).append("\n").
                append("构筑物名称：").append(bean.getGzwmc()).append("\n").
                append("构筑物类型：").append(bean.getGzwlx()).append("\n").
                append("地基处理方式：").append(bean.getDjclfs()).append("\n").
                append("施工里程：").append(bean.getSglc()).append("\n").
                append("施工长短链：").append(bean.getSgcdl()).append("\n").
                append("观测状态：").append(state).append("\n");
        return sb.toString();
    }


    //根据工点查询水准线路列表
    private void requestLevelingBeanList(final String workPointId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                final List<LevelingRouteBean> levelingRouteBeanList = new ArrayList<>();
                try {
                    jsonObject.put("gdid", workPointId);
                    jsonObject.put("pageNow", 1);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "levelingLine/levelingLineAllByBidSeId";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
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
                                LevelingRouteBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(),LevelingRouteBean.class);
                                levelingRouteBeanList.add(bean);
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
                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
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
                            dismissWaitingDialog();
                            initLevelingOption(levelingRouteBeanList);
                        }
                    });

                }
            }
        }).start();
    }

    //根据水准线路id查询测点列表
    private void requestMeasurePointList(final String levelingId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("szlxid", levelingId);

                    String localURL = Config.SERVER_PREFIXX + "levelingLineForm/showRouteFormByLineId";

                    String requestJSon = jsonObject.toString();

                    okhttp3.Headers.Builder headersbuilder = new okhttp3.Headers.Builder();
                    headersbuilder.add("token",Config.AUTH_TOKEN);
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
                                MeasurePointBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(),MeasurePointBean.class);
                                mMeasurePointList.add(bean);
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
                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
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
                            dismissWaitingDialog();
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        }).start();
    }

//    //请求登录用户所在的所有项目
//    private void requestXMData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                final List<XMBean> xmBeanList = new ArrayList<>();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("token", Config.AUTH_TOKEN);
//                    jsonObject.put("pageNow", mPageIndex);
//                    jsonObject.put("pageSize", PAGE_SIZE);
//
//                    String localURL = Config.SERVER_PREFIXX + "project/selectProjectByUserId";
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
//                            mTotalSize = dataJSON.getIntValue("total");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                XMBean bean = JSON.parseObject(list.getJSONObject(i).toString(), XMBean.class);
//                                xmBeanList.add(bean);
//                            }
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//                            Util.setAppCacheProjectListData(xmBeanList);
//
//                            initProjectOption(xmBeanList);
//                        }
//                    });
//
//                }
//            }
//        }).start();
//    }
//
//
//    //请求登录用户所在的所有标段
//    private void requestBDData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                final List<BDBean> sectionBeanList = new ArrayList<>();
//                try {
//                    jsonObject.put("token", Config.AUTH_TOKEN);
//                    jsonObject.put("pageNow", mPageIndex);
//                    jsonObject.put("pageSize", PAGE_SIZE);
//
//                    String localURL = Config.SERVER_PREFIXX + "bidSection/selectByUserAcc";
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
//                            mTotalSize = dataJSON.getIntValue("total");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
//                                sectionBeanList.add(bean);
//                            }
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//
//                            Util.setAppCacheSectionListData(sectionBeanList);
//                            initSectionOption(sectionBeanList);
//                        }
//                    });
//
//                }
//            }
//        }).start();
//    }
//
//    //请求登录用户所在的所有工点
//    private void requestGDData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                showWaitingDialog();
//                OkHttpClient client = new OkHttpClient();
//                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
//                JSONObject jsonObject = new JSONObject();
//                final List<GDBean> workPointList = new ArrayList<>();
//                try {
//                    jsonObject.put("token", Config.AUTH_TOKEN);
//                    jsonObject.put("pageNow", mPageIndex);
//                    jsonObject.put("pageSize", PAGE_SIZE);
//
//                    String localURL = Config.SERVER_PREFIXX + "workPoints/selectWorkByUser";
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
//                            mTotalSize = dataJSON.getIntValue("total");
//                            JSONArray list = dataJSON.getJSONArray("list");
//                            for (int i = 0; i < list.size(); i++) {
//                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
//                                workPointList.add(bean);
//                            }
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mActivity, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } else {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(mActivity, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (final Exception e) {
//                    Log.e(TAG, e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mActivity, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } finally {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            dismissWaitingDialog();
//                            Util.setAppCacheWorkPointListData(workPointList);
//                            initWorkPointOption(workPointList);
//                        }
//                    });
//
//                }
//            }
//        }).start();
//    }
}
