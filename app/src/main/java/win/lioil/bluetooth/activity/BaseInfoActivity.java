package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.ShareRefrenceUtil;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.widget.LevelingOptionDialog;

public class BaseInfoActivity extends AppCompatActivity {

    private static final String TAG = "Test BaseInfoActivity";

    private LinearLayout llBack;
    private RelativeLayout rlBaseInfo,rlMeasure,rlSetting;

    private BaseInfoActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_info);
        mActivity = this;

        initView();
        initClickListener();
    }


    private void initView()
    {
        llBack = findViewById(R.id.ll_back);
        rlBaseInfo = findViewById(R.id.rl_base);
        rlMeasure = findViewById(R.id.rl_measure);
        rlSetting = findViewById(R.id.rl_setting);
    }

    private void initClickListener()
    {
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rlBaseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LevelingOptionDialog dialog = new LevelingOptionDialog(mActivity,0);
                dialog.show();
                WindowManager windowManager = getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (int)(display.getWidth()*1);
                dialog.getWindow().setAttributes(lp);


//                Intent intent = new Intent(mActivity, LevelingLinesListActivity.class);
//                intent.putExtra("requestType", Config.XM_TYPE);
//                startActivity(intent);
            }
        });

        rlMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, MeasureActivity.class);
                startActivity(intent);
            }
        });

        rlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                edtHDDelta.setText(TextUtils.isEmpty((String)ShareRefrenceUtil.get(mActivity,"fb_hd_delta"))?
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
                        readThresholdSettings();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.show();
            }
        });
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


    private void showLevelingOptionDialog()
    {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.leveling_line_option_dialog,null);
        final AlertDialog.Builder builder  = new AlertDialog.Builder(mActivity);
        builder.setView(view);

        LinearLayout llProject = view.findViewById(R.id.ll_project);
        Button btnProject = view.findViewById(R.id.project_btn);

        LinearLayout llSection = view.findViewById(R.id.ll_section);
        Button btnSection = view.findViewById(R.id.section_spin);

        LinearLayout llWorkPoint = view.findViewById(R.id.ll_workpoint);
        Button btnWorkPoint = view.findViewById(R.id.workpoint_btn);

        LinearLayout llLevelingLine = view.findViewById(R.id.ll_levelingline);
        Button btnLeveling = view.findViewById(R.id.leveling_btn);

        if (Util.getAppCacheProjectListData().isEmpty())
        {
            llProject.setVisibility(View.GONE);
            if (Util.getAppCacheSectionListData().isEmpty())
            {
                llSection.setVisibility(View.GONE);
            }
            else
            {

            }
        }
        else
        {
            btnProject.setText(Util.getAppCacheProjectListData().get(0).getXmmc());
        }



    }
}
