package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
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
import win.lioil.bluetooth.adapter.DetailAdapter;
import win.lioil.bluetooth.bean.BDBean;
import win.lioil.bluetooth.bean.DetailBean;
import win.lioil.bluetooth.bean.GDBean;
import win.lioil.bluetooth.bean.SubGradeBean;
import win.lioil.bluetooth.bean.XMBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.RecycleViewDivider;

public class DetailListActivity extends Activity {

    private static final String TAG = "Test DetailListActivity";

    private LinearLayout llBack;

    private TextView txtTitle;

    private RecyclerView mRecyclerView;

    private RefreshLayout refreshLayout;

    private static final int PAGE_SIZE = 10;

    private int mPageIndex = 0;

    private int mTotalSize = 0;

    private List<DetailBean> mDetailBeanList = new ArrayList<>();

    private List<XMBean> mProjectBeanList = new ArrayList<>();

    private List<BDBean> mSectionBeanList = new ArrayList<>();

    private List<GDBean> mWorkPointBeanList = new ArrayList<>();

    private List<SubGradeBean> mSubGradeBeanList = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private DetailAdapter adapter;

    private int requestType = 0;

    private Intent mIntent;

    private String projectName;

    private String sectionName;

    private String workPointName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xm_list_layout);

        mIntent = getIntent();
        requestType = mIntent.getIntExtra("requestType", Config.XM_TYPE);
        projectName = mIntent.getStringExtra("projectName");
        sectionName = mIntent.getStringExtra("sectionName");
        workPointName = mIntent.getStringExtra("workPointName");

        initView();
        initRecyclerView();
    }

    private void initView() {
        txtTitle = findViewById(R.id.txt_title);

        llBack = findViewById(R.id.ll_back);

        mRecyclerView = findViewById(R.id.rc_view);

        refreshLayout = findViewById(R.id.refreshLayout);

        refreshLayout.setRefreshHeader(new ClassicsHeader(this).setSpinnerStyle(SpinnerStyle.Scale));

        refreshLayout.setRefreshFooter(new ClassicsFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        //设置样式后面的背景颜色
        refreshLayout.setPrimaryColorsId(R.color.background_color, android.R.color.white);

        switch (requestType) {
            case Config.XM_TYPE:
                txtTitle.setText("项目列表");
                break;
            case Config.BD_TYPE:
                if (mIntent.hasExtra("requestName")) {
                    if (!TextUtils.isEmpty(projectName)) {
                        txtTitle.setText(String.format("%s项目标段列表", projectName));
                    }
                } else {
                    txtTitle.setText("标段列表");
                }
                break;
            case Config.GD_TYPE:
                if (mIntent.hasExtra("requestName")) {
                    if (!TextUtils.isEmpty(projectName) && !TextUtils.isEmpty(sectionName)) {
                        txtTitle.setText(String.format("%s项目>%s标段工点列表", projectName, sectionName));
                    }
                } else {
                    txtTitle.setText("工点列表");
                }
                break;
            case Config.SUBGRADE_TYPE:
                if (!TextUtils.isEmpty(projectName) && !TextUtils.isEmpty(sectionName)
                && !TextUtils.isEmpty(workPointName))
                {
                    txtTitle.setText(String.format("%s项目>%s标段>%s工点断面列表",projectName,sectionName,workPointName));
                }
                else if (TextUtils.isEmpty(projectName))
                {
                    if (TextUtils.isEmpty(sectionName))
                    {
                        txtTitle.setText(String.format("%s工点路基断面列表",workPointName));
                    }
                    else
                    {
                        txtTitle.setText(String.format("%s标段>%s工点断面列表",sectionName,workPointName));
                    }
                }
                break;
            default:
                break;
        }


        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mPageIndex * PAGE_SIZE < mTotalSize) {
                    mPageIndex++;
                    requestDataByType();
                } else {
                    refreshLayout.finishRefresh();
                    refreshLayout.finishLoadMore();
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                clearAllList();
                mPageIndex = 1;
                requestDataByType();
            }
        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void clearAllList() {
        mDetailBeanList.clear();
        mProjectBeanList.clear();
        mWorkPointBeanList.clear();
        mSectionBeanList.clear();
        mSubGradeBeanList.clear();
    }

    private void requestDataByType() {
        switch (requestType) {
            case Config.XM_TYPE:
                if (Util.getAppCacheProjectListData().isEmpty())
                {
                    requestXMData();
                }
                else
                {
                    mProjectBeanList.addAll(Util.getAppCacheProjectListData());
                    for (XMBean bean : mProjectBeanList)
                    {
                        mDetailBeanList.add(convertXMBeanToDetailBean(bean));
                    }
                    adapter.notifyDataSetChanged();
                }
                break;
            case Config.BD_TYPE:
                if (mIntent.hasExtra("requestID")) {
                    String requestID = mIntent.getStringExtra("requestID");
                    if (TextUtils.isEmpty(requestID)) {
                        Toast.makeText(DetailListActivity.this, "入参有误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    requestBDDataByProjectId(requestID);
                } else {
                    if (Util.getAppCacheSectionListData().isEmpty())
                    {
                        requestBDData();
                    }
                    else
                    {
                        mSectionBeanList.addAll(Util.getAppCacheSectionListData());
                        for (BDBean bean : mSectionBeanList)
                        {
                            mDetailBeanList.add(convertBDBeanToDetailBean(bean));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            case Config.GD_TYPE:
                if (mIntent.hasExtra("requestID")) {
                    String requestID = mIntent.getStringExtra("requestID");
                    if (TextUtils.isEmpty(requestID)) {
                        Toast.makeText(DetailListActivity.this, "入参有误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    requestWorkPointsBySectionId(requestID);
                } else {
                    if (Util.getAppCacheWorkPointListData().isEmpty())
                    {
                        requestGDData();
                    }
                    else
                    {
                        mWorkPointBeanList.addAll(Util.getAppCacheWorkPointListData());
                        for (GDBean bean : mWorkPointBeanList)
                        {
                            mDetailBeanList.add(convertGDBeanToDetailBean(bean));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            case Config.SUBGRADE_TYPE:
                if (mIntent.hasExtra("requestID")) {
                    String requestID = mIntent.getStringExtra("requestID");
                    if (TextUtils.isEmpty(requestID)) {
                        Toast.makeText(DetailListActivity.this, "入参有误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    requestSubgradeList(requestID);
                }
                break;
            default:
                requestXMData();
                break;
        }
    }

    private DetailBean convertXMBeanToDetailBean(XMBean xmBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setId(xmBean.getId());
        detailBean.setDesc(xmBean.getXmmx());
        detailBean.setName(xmBean.getXmmc());
        return detailBean;
    }

    private DetailBean convertBDBeanToDetailBean(BDBean xmBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setId(xmBean.getId());
        detailBean.setDesc(xmBean.getBdms());
        detailBean.setName(xmBean.getBdmc());
        return detailBean;
    }

    private DetailBean convertGDBeanToDetailBean(GDBean xmBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setId(xmBean.getId());
        detailBean.setDesc(xmBean.getGdms());
        detailBean.setName(xmBean.getGdmc());
        return detailBean;
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearAllList();
        mPageIndex = 1;
        requestDataByType();
    }

    private void showWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(DetailListActivity.this);
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

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        // 定义一个线性布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        // 设置adapter
        adapter = new DetailAdapter(DetailListActivity.this, mDetailBeanList, requestType);
        mRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new DetailAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= mDetailBeanList.size()) {
                    return;
                }

                skipToNextPage(mDetailBeanList.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (position >= mDetailBeanList.size()) {
                    return;
                }

                if (Config.XM_TYPE == requestType) {
                    requestProjectInfo(mDetailBeanList.get(position).getId());
                } else if (Config.BD_TYPE == requestType) {
                    requestSectionInfo(mDetailBeanList.get(position).getId());
                } else if (Config.GD_TYPE == requestType) {
                    requestWorkPointInfo(mDetailBeanList.get(position).getId());
                } else if (Config.SUBGRADE_TYPE == requestType) {
                    requestSubGradeInfo(mDetailBeanList.get(position).getId());
                }
            }
        });

        // 添加分割线
        mRecyclerView.addItemDecoration(new RecycleViewDivider(DetailListActivity.this,
                LinearLayoutManager.HORIZONTAL, Util.dip2px(DetailListActivity.this, 10), Color.rgb(0xf0, 0xf0, 0xf0)));
    }

    private void skipToNextPage(DetailBean bean) {

        if (Config.XM_TYPE == requestType) {
            Intent intent = new Intent(DetailListActivity.this, DetailListActivity.class);
            intent.putExtra("requestType", Config.BD_TYPE);
            intent.putExtra("requestID", bean.getId());
            intent.putExtra("projectName", bean.getName());
            intent.putExtra("sectionName", "");
            intent.putExtra("workPointName", "");
            intent.putExtra("requestName", String.format("%s项目标段列表", bean.getName()));
            startActivity(intent);
        } else if (Config.BD_TYPE == requestType) {
            Intent intent = new Intent(DetailListActivity.this, DetailListActivity.class);
            intent.putExtra("requestType", Config.GD_TYPE);
            intent.putExtra("requestID", bean.getId());
            intent.putExtra("projectName", projectName);
            intent.putExtra("sectionName", bean.getName());
            intent.putExtra("workPointName", "");
            intent.putExtra("requestName", String.format("%s标段工点列表", bean.getName()));
            startActivity(intent);
        } else if (Config.GD_TYPE == requestType) {
            Intent intent = new Intent(DetailListActivity.this, SubGradeActivity.class);
            intent.putExtra("requestType", Config.SUBGRADE_TYPE);
//            intent.putExtra("requestID", bean.getId());
            intent.putExtra("requestID", "d514f4f9005a4ffa91c98c10ee170cdc");
            intent.putExtra("projectName", projectName);
            intent.putExtra("sectionName", sectionName);
            intent.putExtra("workPointName", bean.getName());
            intent.putExtra("requestName", String.format("%s工点断面列表", bean.getName()));
            startActivity(intent);
        }
    }

    //查看项目详细信息
    private void requestProjectInfo(final String projectId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", projectId);

                    String localURL = Config.SERVER_PREFIXX + "project/selProjectById";

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
                            XMBean bean = JSON.parseObject(dataJSON.toJSONString(), XMBean.class);
                            showAlertDialog("项目详细信息", parseXMBeanToMessage(bean));
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    //查看标段详细信息
    private void requestSectionInfo(final String sectionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", sectionId);

                    String localURL = Config.SERVER_PREFIXX + "bidSection/selBidSectionById";

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
                            BDBean bean = JSON.parseObject(dataJSON.toJSONString(), BDBean.class);
                            showAlertDialog("标段详细信息", parseBDBeanToMessage(bean));
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    //查看工点详细信息
    private void requestWorkPointInfo(final String pointId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", pointId);

                    String localURL = Config.SERVER_PREFIXX + "workPoints/selWorkPointsById";

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
                            GDBean bean = JSON.parseObject(dataJSON.toJSONString(), GDBean.class);
                            showAlertDialog("工点详细信息", parseGDBeanToMessage(bean));
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private String parseGDBeanToMessage(GDBean bean) {
        StringBuilder sb = new StringBuilder();
        sb.append("工点名称：").append(bean.getGdmc()).append("\n").
                append("工点描述：").append(bean.getGdms()).append("\n").
                append("工点负责人：").append(bean.getGdfzr()).append("\n").
                append("重点安全事故：").append(bean.getYwzdaqsg()).append("\n").
                append("是否延期或提前：").append(bean.getSfyqhtiqc()).append("\n");
        return sb.toString();
    }

    private String parseBDBeanToMessage(BDBean bean) {
        StringBuilder sb = new StringBuilder();
//        String startTime = bean.getKssj();
//        if (startTime.contains(","))
//        {
//            startTime = startTime.replaceFirst(",","年").
//                    replaceFirst(",","月").
//                    replaceFirst(",","日 ").
//                    replaceFirst(",","时").
//                    replaceFirst(",","分").replace("[","").replace("]","") + "秒";
//        }
//
//        String endTime = bean.getJssj();
//        if (endTime.contains(","))
//        {
//            endTime = endTime.replaceFirst(",","年").
//                    replaceFirst(",","月").
//                    replaceFirst(",","日 ").
//                    replaceFirst(",","时").
//                    replaceFirst(",","分").replace("[","").replace("]","") + "秒";
//        }

        sb.append("标段名称：").append(bean.getBdmc()).append("\n").
                append("标段描述：").append(bean.getBdms()).append("\n").
                append("标段编码：").append(bean.getBdbm()).append("\n").
                append("标段里程：").append(bean.getBdlc()).append("\n").
//                append("开始时间：").append(startTime).append("\n").
//                append("结束时间：").append(endTime).append("\n").
        append("重点安全事故：").append(bean.getYwzdaqsg()).append("\n").
                append("精度要求：").append(bean.getJdyq());
        return sb.toString();
    }

    private String parseXMBeanToMessage(XMBean bean) {
        StringBuilder sb = new StringBuilder();
        String startTime = bean.getKssj();
        if (startTime.contains(",")) {
            startTime = startTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        String endTime = bean.getJssj();
        if (endTime.contains(",")) {
            endTime = endTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        sb.append("项目名称：").append(bean.getXmmc()).append("\n").
                append("项目描述：").append(bean.getXmmx()).append("\n").
                append("项目所在地：").append(bean.getSzd()).append("\n").
                append("监测单位：").append(bean.getJcdw()).append("\n").
                append("施工单位：").append(bean.getSgdw()).append("\n").
                append("监理单位：").append(bean.getJldw()).append("\n").
                append("设计单位：").append(bean.getSjdw()).append("\n").
                append("开始时间：").append(startTime).append("\n").
                append("结束时间：").append(endTime).append("\n").
                append("重点安全事故：").append(bean.getYwzdaqsg()).append("\n").
                append("标段数量：").append(bean.getBdsl()).append("\n").
                append("是否延期或提前：").append(bean.getSfyqhtiqc()).append("\n");
        return sb.toString();
    }

    private void showAlertDialog(final String title, final String messageContent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailListActivity.this);
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

    //查看项目下标段
    private void requestBDDataByProjectId(final String projectId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("xmid", projectId);
                    jsonObject.put("pageNow", mPageIndex);
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
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
                                mSectionBeanList.add(bean);
                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getBdmc());
                                detailBean.setDesc(bean.getBdms());
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    //查标段下所有工点
    private void requestWorkPointsBySectionId(final String sectionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("bdid", sectionId);
                    jsonObject.put("pageNow", mPageIndex);
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
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
                                mWorkPointBeanList.add(bean);
                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getGdmc());
                                detailBean.setDesc(bean.getGdms());
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

    //请求登录用户所在的所有项目
    private void requestXMData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();

                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", mPageIndex);
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
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                XMBean bean = JSON.parseObject(list.getJSONObject(i).toString(), XMBean.class);
                                mProjectBeanList.add(bean);

                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getXmmx());
                                detailBean.setDesc(bean.getXmmc());
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

                            Util.setAppCacheProjectListData(mProjectBeanList);
                        }
                    });

                }
            }
        }).start();
    }


    //请求登录用户所在的所有标段
    private void requestBDData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", mPageIndex);
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
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                BDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), BDBean.class);
                                mSectionBeanList.add(bean);
                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getBdmc());
                                detailBean.setDesc(bean.getBdms());
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

                            Util.setAppCacheSectionListData(mSectionBeanList);
                        }
                    });

                }
            }
        }).start();
    }

    //请求登录用户所在的所有工点
    private void requestGDData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("token", Config.AUTH_TOKEN);
                    jsonObject.put("pageNow", mPageIndex);
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
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = JSON.parseObject(responseBody);
                        if (TextUtils.equals(responseJson.getString("msg").toLowerCase(), "success")) {
                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            mTotalSize = dataJSON.getIntValue("total");
                            JSONArray list = dataJSON.getJSONArray("list");
                            for (int i = 0; i < list.size(); i++) {
                                GDBean bean = JSON.parseObject(list.getJSONObject(i).toString(), GDBean.class);
                                mWorkPointBeanList.add(bean);
                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getGdmc());
                                detailBean.setDesc(bean.getGdms());
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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

                            Util.setAppCacheWorkPointListData(mWorkPointBeanList);
                        }
                    });

                }
            }
        }).start();
    }

    //根据工点主键获取该工点下的所有断面
    private void requestSubgradeList(final String sectionId) {
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
                                SubGradeBean bean = JSON.parseObject(list.getJSONObject(i).toJSONString(), SubGradeBean.class);
                                mSubGradeBeanList.add(bean);
                                DetailBean detailBean = new DetailBean();
                                detailBean.setId(bean.getId());
                                detailBean.setName(bean.getDmmc());
                                String subType = bean.getDmlx();
                                if (TextUtils.equals(subType,"1"))
                                {
                                    subType = "路基";
                                }
                                else if (TextUtils.equals(subType,"2"))
                                {
                                    subType = "桥涵";
                                }
                                else if (TextUtils.equals(subType,"3"))
                                {
                                    subType = "隧道";
                                }
                                else if (TextUtils.equals(subType,"4"))
                                {
                                    subType = "过渡段";
                                }
                                detailBean.setDesc(bean.getDmbm() + "   "  + subType);
                                mDetailBeanList.add(detailBean);
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(DetailListActivity.this, "查询失败，" + responseJson.getString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailListActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DetailListActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
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
}
