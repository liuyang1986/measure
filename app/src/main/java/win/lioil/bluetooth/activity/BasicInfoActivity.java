package win.lioil.bluetooth.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import win.lioil.bluetooth.bean.DetailBean;
import win.lioil.bluetooth.bean.LevelingRouteBean;
import win.lioil.bluetooth.bean.MeasurePointBean;
import win.lioil.bluetooth.bean.WorkPointBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.HRecyclerView;

public class BasicInfoActivity extends AppCompatActivity {

    private static final String TAG = "Test BasicInfoActivity";

    private LinearLayout llBack;

    private TextView txtTitle;

    private RefreshLayout refreshLayout;

    private HRecyclerView hRecyclerView;

    private MeasurePointAdapter adapter;

    private LinearLayout llOption;

    private Spinner mProjectSpin,mSectionSpin,mWorkPointSpin,mLevelingSpin;

    private static final int PAGE_SIZE = 100;

    private ProgressDialog mProgressDialog;

    private List<MeasurePointBean> mMeasurePointList = new ArrayList<>();

    private String workPointId;

    private int requestType;

    private String projectName;

    private String sectionName;

    private String workPointName;


    private ArrayAdapter<String> xmAdapter;
    private ArrayAdapter<String> bdAdapter;
    private ArrayAdapter<String> gdAdapter;
    private ArrayAdapter<String> levelingAdapter;

    private MeasurePointBean measurePointBean;

    private List<WorkPointBean> workPointBeanList;

    private BasicInfoActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_basic_info);
        initView();
        initClickEvent();

        initData();
    }

    private void initView()
    {
        txtTitle = (TextView) findViewById(R.id.txt_title);

        llBack = (LinearLayout)findViewById(R.id.ll_back);

        llOption = (LinearLayout)findViewById(R.id.ll_option);


        mProjectSpin = (Spinner)findViewById(R.id.project_spin);

        mWorkPointSpin = (Spinner)findViewById(R.id.workpoint_spin);

        mSectionSpin =  (Spinner)findViewById(R.id.section_spin);

        mLevelingSpin =  (Spinner)findViewById(R.id.leveling_spin);

        refreshLayout = (RefreshLayout)findViewById(R.id.refreshLayout);

        hRecyclerView=  (HRecyclerView)findViewById(R.id.id_hrecyclerview);

        refreshLayout.setRefreshHeader(new ClassicsHeader(this).setSpinnerStyle(SpinnerStyle.Scale));

        refreshLayout.setRefreshFooter(new ClassicsFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        //设置样式后面的背景颜色
        refreshLayout.setPrimaryColorsId(R.color.background_color, android.R.color.white);

        hRecyclerView.setHeaderListData(getResources().getStringArray(R.array.measure_point_title));
    }


    private void initClickEvent()
    {
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

    private void initProjectOption(final List<DetailBean> projectBeanList)
    {
        List<String> nameList = new ArrayList<>();
        for(DetailBean bean : projectBeanList)
        {
            nameList.add(bean.getName());
        }

        xmAdapter = new ArrayAdapter<>(mActivity,
               R.layout.spinner_item_style,nameList);
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

                List<DetailBean> sectionList = new ArrayList<>();
                for(WorkPointBean workPointBean : workPointBeanList)
                {
                    if (TextUtils.equals(workPointBean.getXmid(),projectBeanList.get(position).getId()))
                    {
                        sectionList.add(convertWorkPointBeanToSectionBean(workPointBean));
                    }
                }

                initSectionOption(sectionList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mProjectSpin.setSelection(0);
    }

    private void initSectionOption(final List<DetailBean> sectionBeanList)
    {
        List<String> nameList = new ArrayList<>();
        for(DetailBean bdBean : sectionBeanList)
        {
            nameList.add(bdBean.getName());
        }

        bdAdapter = new ArrayAdapter<>(mActivity,
               R.layout.spinner_item_style,nameList);
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

                List<DetailBean> workPointList = new ArrayList<>();
                for(WorkPointBean workPointBean : workPointBeanList)
                {
                    if (TextUtils.equals(workPointBean.getBdid(),sectionBeanList.get(position).getId()))
                    {
                        workPointList.add(convertWorkPointBeanToWorkPointBean(workPointBean));
                    }
                }

                initWorkPointOption(workPointList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSectionSpin.setSelection(0);
    }

    private void initLevelingOption(final List<LevelingRouteBean> levelingRouteBeanList)
    {
        if (levelingRouteBeanList.isEmpty())
        {
            return ;
        }

        List<String> nameList = new ArrayList<>();
        for(LevelingRouteBean levelingBean : levelingRouteBeanList)
        {
            nameList.add(levelingBean.getSzxlmc());
        }

        levelingAdapter = new ArrayAdapter<>(mActivity,
               R.layout.spinner_item_style,nameList);
        levelingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLevelingSpin.setAdapter(levelingAdapter);
        mLevelingSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position >= levelingRouteBeanList.size())
                {
                    return ;
                }

                Log.d(TAG,"levelingRouteBeanList pos is " + position);
                requestMeasurePointList(levelingRouteBeanList.get(position).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mLevelingSpin.setSelection(0);
    }

    private void initWorkPointOption(final List<DetailBean> workPointList)
    {
        List<String> nameList = new ArrayList<>();
        for(DetailBean gdBean : workPointList)
        {
            nameList.add(gdBean.getName());
        }

        gdAdapter = new ArrayAdapter<>(mActivity,
                R.layout.spinner_item_style,nameList);
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
//                            JSONObject dataJSON = responseJson.getJSONObject("data");
                            JSONArray list = responseJson.getJSONArray("data");
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

    private String parseMeasureBeanToMessage(MeasurePointBean bean) {
        String measureType = bean.getCdlx();
        if (TextUtils.equals(measureType,"1"))
        {
            measureType = "工作基点";
        }
        else
        {
            measureType = "监测点";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("测点名称：").append(bean.getCdmc()).append("\n").
                append("测点类型：").append(measureType).append("\n").
                append("测点高程：").append(bean.getCdcsgc()).append("\n").
                append("上次测量高程：").append(bean.getScclgc()).append("\n");
        return sb.toString();
    }

    private void initData()
    {
        workPointBeanList = new ArrayList<>();
        workPointBeanList.addAll(Util.getAllWorkPointListData());

        if (!workPointBeanList.isEmpty())
        {
            Log.d(TAG,"workPointBeanList size is " + workPointBeanList.size());

            List<DetailBean> projectBeanList = new ArrayList<>();
            projectBeanList.add(convertWorkPointBeanToProjectBean(workPointBeanList.get(0)));
            //找相同标段id的工点
            for (WorkPointBean workPointBean : workPointBeanList)
            {
                boolean bFindFlag = false;
                for(DetailBean projectBean : projectBeanList)
                {
                    if (TextUtils.equals(projectBean.getId(),workPointBean.getXmid()))
                    {
                        bFindFlag = true;
                        break;
                    }
                }

                if (!bFindFlag)
                {
                    projectBeanList.add(convertWorkPointBeanToProjectBean(workPointBean));
                }
            }
            Log.d(TAG,"sectionList size is " + projectBeanList.size());
            initProjectOption(projectBeanList);
        }
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

    private DetailBean convertWorkPointBeanToProjectBean(WorkPointBean workPointBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setName(workPointBean.getXmmc());
        detailBean.setId(workPointBean.getXmid());
        return detailBean;
    }

    private DetailBean convertWorkPointBeanToSectionBean(WorkPointBean workPointBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setName(workPointBean.getBdmc());
        detailBean.setId(workPointBean.getBdid());
        return detailBean;
    }

    private DetailBean convertWorkPointBeanToWorkPointBean(WorkPointBean workPointBean)
    {
        DetailBean detailBean = new DetailBean();
        detailBean.setName(workPointBean.getGdmc());
        detailBean.setId(workPointBean.getGdid());
        return detailBean;
    }

}
