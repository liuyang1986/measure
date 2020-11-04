package win.lioil.bluetooth.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.activity.BasicInfoActivity;
import win.lioil.bluetooth.activity.HomeActivity;
import win.lioil.bluetooth.adapter.MeasureRecordAdapter;
import win.lioil.bluetooth.bean.MeasureRecordBean;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.GlideImageLoader;
import win.lioil.bluetooth.util.ShareRefrenceUtil;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.RecycleViewDivider;

public class HomeFragment extends BaseFragment {

    private LinearLayout llBaseInfo;

    private LinearLayout llMeasureRecord;

    private LinearLayout llDataAnalyze;

    private LinearLayout llThresholdSetting;

    private Banner mBanner;

    private FragmentActivity mActivity;

    private List mImageList;//图片资源
    private List<String> mTitleList;//轮播标题

    private SmartRefreshLayout refreshLayout;
    private RecyclerView measureRecordRecyclerView;

    private List<MeasureRecordBean> measureRecordBeanList = new ArrayList<>();

    private MeasureRecordAdapter adapter;

    @Override
    public void fetchData() {
        MeasureRecordBean bean = new MeasureRecordBean();
        bean.setMeasureName("汉十铁路孝十段");
        bean.setMeasureStatus("正常");
        bean.setMeasureTime("2019.04.07-2019.12.03");
        bean.setMeasureType("项目");
        bean.setMeasureWatcher("监测人：胡彬");
        measureRecordBeanList.add(bean);

        MeasureRecordBean bean1 = new MeasureRecordBean();
        bean1.setMeasureName("T5570G63");
        bean1.setMeasureStatus("正常");
        bean1.setMeasureTime("2019.04.07-2019.12.04");
        bean1.setMeasureType("标段");
        bean1.setMeasureWatcher("监测人：胡晨");
        measureRecordBeanList.add(bean1);

        MeasureRecordBean bean2 = new MeasureRecordBean();
        bean2.setMeasureName("G5570G63");
        bean2.setMeasureStatus("正常");
        bean2.setMeasureTime("2019.04.07-2019.12.05");
        bean2.setMeasureType("标段");
        bean2.setMeasureWatcher("监测人：周杰伦");
        measureRecordBeanList.add(bean2);

        adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
        initClickEvent();
        return view;
    }

    private void initView(View view)
    {
        llBaseInfo = view.findViewById(R.id.ll_base_info);

        llMeasureRecord = view.findViewById(R.id.ll_measure_record);

        llDataAnalyze = view.findViewById(R.id.ll_data_analyze);

        llThresholdSetting = view.findViewById(R.id.ll_threshold_setting);

        mBanner = view.findViewById(R.id.main_banner);

        initBanner();

        refreshLayout = view.findViewById(R.id.refreshLayout);

        refreshLayout.setRefreshHeader(new ClassicsHeader(mActivity).setSpinnerStyle(SpinnerStyle.Scale));

        refreshLayout.setRefreshFooter(new ClassicsFooter(mActivity).setSpinnerStyle(SpinnerStyle.Scale));
        //设置样式后面的背景颜色
        refreshLayout.setPrimaryColorsId(R.color.background_color, android.R.color.white);

        measureRecordRecyclerView = view.findViewById(R.id.recyclerview_measure_record);

        initRecyclerView();
    }

    private void initBanner()
    {
        mImageList = new ArrayList();
        mImageList.add(R.mipmap.banner1);
        mTitleList = new ArrayList<>();
        mBanner.setImageLoader(new GlideImageLoader());
        mBanner.setImages(mImageList);
        mBanner.setDelayTime(2000);
        mBanner.setBannerAnimation(Transformer.Stack);

        mBanner.setIndicatorGravity(BannerConfig.CENTER);//设置指示器的位置

        mBanner.start();//开
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        // 定义一个线性布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        // 设置布局管理器
        measureRecordRecyclerView.setLayoutManager(manager);
        // 设置adapter
        adapter = new MeasureRecordAdapter(mActivity, measureRecordBeanList);
        measureRecordRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MeasureRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= measureRecordBeanList.size()) {
                    return;
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (position >= measureRecordBeanList.size()) {
                    return;
                }

            }
        });

        // 添加分割线
        measureRecordRecyclerView.addItemDecoration(new RecycleViewDivider(mActivity,
                LinearLayoutManager.HORIZONTAL, Util.dip2px(mActivity, 10), Color.rgb(0xf0, 0xf0, 0xf0)));
    }


    private void initClickEvent()
    {
        llBaseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, BasicInfoActivity.class);
                startActivity(intent);
            }
        });

        llDataAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APP.toast("功能暂未开放，期待中",0);
            }
        });

        llMeasureRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APP.toast("功能暂未开放，期待中",0);
            }
        });

        llThresholdSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThresholdSettingDialog();
            }
        });

        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishLoadMore(1000);

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh(1000);
            }
        });
    }

    private void showThresholdSettingDialog()
    {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.threshold_setting_dialog,null);
        final AlertDialog.Builder builder  = new AlertDialog.Builder(mActivity);
        builder.setView(view);

        //前后视距差
        final EditText edtHDDelta = view.findViewById(R.id.fb_hd_delta);
        //累积视距差
        final EditText edtHDSum = view.findViewById(R.id.fb_hd_sum);
        //前后两次读数差
        final EditText edtNearVD = view.findViewById(R.id.near_vd_delta);
        //高差之差
        final EditText edtNearHD = view.findViewById(R.id.near_hd_delta);
        //最大视线高
        final EditText edtMaxHD = view.findViewById(R.id.max_hd);
        //最小视线高
        final EditText edtMinHD = view.findViewById(R.id.min_hd);

        edtHDDelta.setText(TextUtils.isEmpty((String) ShareRefrenceUtil.get(mActivity,"fb_hd_delta"))?
                Config.FB_HD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"fb_hd_delta")));
        edtHDSum.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"fb_hd_sum"))?
                Config.FB_HD_SUM:((String)ShareRefrenceUtil.get(mActivity,"fb_hd_sum")));
        edtNearVD.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"near_vd_delta"))?
                Config.NEAR_VD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"near_vd_delta")));
        edtNearHD.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"near_hd_delta"))?
                Config.NEAR_HD_DELTA:((String)ShareRefrenceUtil.get(mActivity,"near_hd_delta")));
        edtMaxHD.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"max_hd"))?
                Config.MAX_SIGHT_HEIGHT:((String)ShareRefrenceUtil.get(mActivity,"max_hd")));
        edtMinHD.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"min_hd"))?
                Config.MIN_SIGHT_HEIGHT:((String)ShareRefrenceUtil.get(mActivity,"min_hd")));

        builder.setTitle("测量限值设定");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShareRefrenceUtil.save(mActivity,"fb_hd_delta",edtHDDelta.getText().toString());
                ShareRefrenceUtil.save(mActivity,"fb_hd_sum",edtHDSum.getText().toString());
                ShareRefrenceUtil.save(mActivity,"near_vd_delta",edtNearVD.getText().toString());
                ShareRefrenceUtil.save(mActivity,"near_hd_delta",edtNearHD.getText().toString());
                ShareRefrenceUtil.save(mActivity,"max_hd",edtMaxHD.getText().toString());
                ShareRefrenceUtil.save(mActivity,"min_hd",edtMinHD.getText().toString());

                if (mActivity instanceof HomeActivity)
                {
                    ((HomeActivity) mActivity).readThresholdSettings();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBanner.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBanner.stopAutoPlay();
    }
}
