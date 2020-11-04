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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.adapter.CommonAdapter;
import win.lioil.bluetooth.adapter.SubGradeAdapter;
import win.lioil.bluetooth.bean.BDBean;
import win.lioil.bluetooth.bean.GDBean;
import win.lioil.bluetooth.bean.SubGradeBean;
import win.lioil.bluetooth.bean.XMBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.HRecyclerView;

public class SubGradeActivity extends Activity {

    private static final String TAG = "Test SubGradeActivity";

    private LinearLayout llBack;

    private TextView txtTitle;

    private RefreshLayout refreshLayout;

    private HRecyclerView hRecyclerView;

    private SubGradeAdapter adapter;

    private LinearLayout llOption;

    private Spinner mProjectSpin,mSectionSpin,mWorkPointSpin,mSubGradeSpin;

    private static final int PAGE_SIZE = 10;

    private int mPageIndex = 0;

    private int mTotalSize = 0;

    private ProgressDialog mProgressDialog;

    private List<SubGradeBean> mSubGradeBeanList = new ArrayList<>();

//    private List<DetailBean> mDetailBeanList = new ArrayList<>();

    private List<XMBean> mProjectBeanList = new ArrayList<>();

    private List<BDBean> mSectionBeanList = new ArrayList<>();

    private List<GDBean> mWorkPointBeanList = new ArrayList<>();
//
//    private Map<List<XMBean>,List<BDBean>>dataMap = new HashMap();

    private Map<String,String> map = new HashMap<>();

    private String workPointId;

    private int requestType;

    private String projectName;

    private String sectionName;

    private String workPointName;

    private Spinner projectSpin,sectionSpin,workPointSpin,subGradeSpin;

    private ArrayAdapter<String> xmAdapter;
    private ArrayAdapter<String> bdAdapter;
    private ArrayAdapter<String> gdAdapter;
    private ArrayAdapter<String> subGradeAdapter;

    private List<String> projectNameList = new ArrayList<>();
    private List<String> sectionNameList = new ArrayList<>();
    private List<String> workPointNameList = new ArrayList<>();
    private List<String> subGradeList = new ArrayList<>();



    private String subGradeType = "";

    private int mProjectPos = 0, mSectionPos = 0, mWorkPointPos = 0,mSubGradePos = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subgrade);

        Intent intent = getIntent();
        requestType = intent.getIntExtra("requestType", Config.SUBGRADE_TYPE);
        projectName = intent.getStringExtra("projectName");
        sectionName = intent.getStringExtra("sectionName");
        workPointName = intent.getStringExtra("workPointName");
        workPointId = intent.getStringExtra("requestID");

//        workPointId = "d514f4f9005a4ffa91c98c10ee170cdc";

        txtTitle = findViewById(R.id.txt_title);

        llBack = findViewById(R.id.ll_back);

        llOption = findViewById(R.id.ll_option);



        projectSpin = findViewById(R.id.project_spin);

        sectionSpin = findViewById(R.id.section_spin);

        workPointSpin = findViewById(R.id.workpoint_spin);

        subGradeSpin = findViewById(R.id.subgrade_spin);

        mProjectSpin = findViewById(R.id.project_spin);

        mWorkPointSpin = findViewById(R.id.workpoint_spin);

        mSectionSpin = findViewById(R.id.section_spin);

        mSubGradeSpin = findViewById(R.id.subgrade_spin);

        refreshLayout = findViewById(R.id.refreshLayout);

        hRecyclerView=  findViewById(R.id.id_hrecyclerview);

        refreshLayout.setRefreshHeader(new ClassicsHeader(this).setSpinnerStyle(SpinnerStyle.Scale));

        refreshLayout.setRefreshFooter(new ClassicsFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        //设置样式后面的背景颜色
        refreshLayout.setPrimaryColorsId(R.color.background_color, android.R.color.white);

        hRecyclerView.setHeaderListData(getResources().getStringArray(R.array.right_title_name));

        adapter = new SubGradeAdapter(SubGradeActivity.this, mSubGradeBeanList, R.layout.subgrade_item_layout, new CommonAdapter.CommonViewHolder.onItemCommonClickListener() {
            @Override
            public void onItemClickListener(int position) {
                if (position >= mSubGradeBeanList.size())
                {
                    return ;
                }

                requestSubGradeInfo(mSubGradeBeanList.get(position).getId());
            }

            @Override
            public void onItemLongClickListener(int position) {

            }
        });

        hRecyclerView.setAdapter(adapter);


        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mPageIndex * PAGE_SIZE < mTotalSize) {
                    mPageIndex++;
                    requestSubgradeList(workPointId,subGradeType);
                } else {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPageIndex = 1;
                mSubGradeBeanList.clear();
                requestSubgradeList(workPointId,subGradeType);
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



        initArrayAdapter();
    }

    private void initArrayAdapter()
    {
        subGradeList.add("路基");
        subGradeList.add("桥涵");
        subGradeList.add("隧道");
        subGradeList.add("过渡段");
        subGradeAdapter = new ArrayAdapter<>(SubGradeActivity.this,
                android.R.layout.simple_spinner_item,subGradeList);
        subGradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubGradeSpin.setAdapter(subGradeAdapter);
        mSubGradeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    subGradeType = String.valueOf(position+1);
                    onWorkPointOptionChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSubGradeSpin.setSelection(0);
    }

    private void showAlertDialog(final String title,final String messageContent)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SubGradeActivity.this);
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

    //查询工点下面的断面列表
    private void requestSubgradeList(final String sectionId,final String subGradeType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("gdid", sectionId);
                    jsonObject.put("pageNow", mPageIndex);
                    jsonObject.put("pageSize", PAGE_SIZE);

                    String localURL = Config.SERVER_PREFIXX + "section/showAllSectionByWorkPoinId";

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
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                SubGradeBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(),SubGradeBean.class);
                                if (TextUtils.isEmpty(subGradeType))
                                {
                                    mSubGradeBeanList.add(bean);
                                }
                                else
                                {
                                    if (TextUtils.equals(subGradeType,bean.getDmlx()))
                                    {
                                        mSubGradeBeanList.add(bean);
                                    }
                                }
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SubGradeActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SubGradeActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SubGradeActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.finishLoadMore();
                            refreshLayout.finishRefresh();
                            dismissWaitingDialog();
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        }).start();
    }


    private void showWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(SubGradeActivity.this);
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
        mSubGradeBeanList.clear();
        mProjectBeanList.clear();
        mSectionBeanList.clear();
        mWorkPointBeanList.clear();

        if (Config.XM_TYPE == requestType)
        {
            mProjectBeanList.addAll(Util.getAppCacheProjectListData());
            projectNameList = new ArrayList<>();
            for(XMBean bean : mProjectBeanList)
            {
                projectNameList.add(bean.getXmmc());
            }

            xmAdapter = new ArrayAdapter<>(SubGradeActivity.this,
                    android.R.layout.simple_spinner_item,projectNameList);
            xmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProjectSpin.setAdapter(xmAdapter);
            mProjectSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    Log.d(TAG,"project pos is " + position);
                    mProjectPos = position;
                    onProjectOptionChanged(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mProjectSpin.setSelection(0);
        }
        else
        {
            workPointId = "d514f4f9005a4ffa91c98c10ee170cdc";
            requestSubgradeList(workPointId,subGradeType);
        }
    }


    private void onProjectOptionChanged(int pos)
    {
        if (pos >= mProjectBeanList.size())
        {
            return;
        }
        XMBean xmBean = mProjectBeanList.get(pos);
        mSectionBeanList.clear();
        sectionNameList.clear();
        mSectionBeanList.addAll(Util.getAppCacheSectionListData());
        for(BDBean bdBean : mSectionBeanList)
        {
            if (TextUtils.equals(xmBean.getXmmc(),bdBean.getXmmc()))
            {
                sectionNameList.add(bdBean.getBdmc());
            }
        }

        bdAdapter = new ArrayAdapter<>(SubGradeActivity.this,
                android.R.layout.simple_spinner_item,sectionNameList);
        bdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSectionSpin.setAdapter(bdAdapter);
        mSectionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"section pos is " + position);
                mSectionPos = position;
                onSectionOptionChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSectionSpin.setSelection(0);
    }

    private void onSectionOptionChanged(int pos)
    {
        if (pos >= mSectionBeanList.size())
        {
            return;
        }
        BDBean bdBean = mSectionBeanList.get(pos);
        mWorkPointBeanList.clear();
        workPointNameList.clear();
        mWorkPointBeanList.addAll(Util.getAppCacheWorkPointListData());
        for(GDBean gdBean : mWorkPointBeanList)
        {
            if (TextUtils.equals(bdBean.getBdmc(),gdBean.getBdmc()))
            {
                workPointNameList.add(gdBean.getGdmc());
            }
        }

        gdAdapter = new ArrayAdapter<>(SubGradeActivity.this,
                android.R.layout.simple_spinner_item,workPointNameList);
        gdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWorkPointSpin.setAdapter(gdAdapter);
        mWorkPointSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"workpoint pos is " + position);
                mWorkPointPos = position;
                onWorkPointOptionChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mWorkPointSpin.setSelection(0);
    }

    private void onWorkPointOptionChanged(int pos)
    {
        if (pos >= mWorkPointBeanList.size())
        {
            return;
        }
        mSubGradeBeanList.clear();
        GDBean gdBean = mWorkPointBeanList.get(pos);
        requestSubgradeList(gdBean.getId(),subGradeType);
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

    //查看断面详细信息
    private void requestSubGradeInfo(final String subGradeId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", subGradeId);

                    String localURL = Config.SERVER_PREFIXX + "section/showSectionById";

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
                            SubGradeBean bean = JSON.parseObject(dataJSON.toJSONString(), SubGradeBean.class);
                            showAlertDialog("断面详细信息", parseSubGradeBeanToMessage(bean));
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SubGradeActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SubGradeActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SubGradeActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.finishRefresh();
                            refreshLayout.finishLoadMore();
                            dismissWaitingDialog();
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        }).start();
    }
}
