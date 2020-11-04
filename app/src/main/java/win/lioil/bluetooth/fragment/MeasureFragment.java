package win.lioil.bluetooth.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.activity.BasicInfoActivity;
import win.lioil.bluetooth.activity.HomeActivity;
import win.lioil.bluetooth.activity.MeasureActivity;
import win.lioil.bluetooth.bt.BtBase;
import win.lioil.bluetooth.bt.BtClient;
import win.lioil.bluetooth.bt.BtDevAdapter;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.BtReceiver;
import win.lioil.bluetooth.util.PermissionHelper;
import win.lioil.bluetooth.util.ShareRefrenceUtil;
import win.lioil.bluetooth.widget.LevelingOptionDialog;

public class MeasureFragment extends BaseFragment {

    private static final String TAG = "Test MeasureFragment";

    private FragmentActivity mActivity;

    private RelativeLayout rlBaseInfo,rlMeasure2nd,rlMeasure4Th,rlSetting;

    @Override
    public void fetchData() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);
        initView(view);
        initClickListener();
        return view;
    }


    private void initView(View view)
    {
        rlBaseInfo = view.findViewById(R.id.rl_base);
        rlMeasure2nd = view.findViewById(R.id.rl_measure_2nd);
        rlSetting = view.findViewById(R.id.rl_setting);
        rlMeasure4Th = view.findViewById(R.id.rl_measure);
    }

    private void initClickListener()
    {
        rlBaseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mActivity, BasicInfoActivity.class);
                startActivity(intent);
            }
        });

        rlMeasure2nd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelingOptionDialog dialog = new LevelingOptionDialog(mActivity,0);
                dialog.show();
                WindowManager windowManager = mActivity.getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (display.getWidth()*1);
                dialog.getWindow().setAttributes(lp);


//                Intent intent = new Intent(mActivity, MeasureActivity.class);
//                startActivity(intent);
            }
        });


        rlMeasure4Th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelingOptionDialog dialog = new LevelingOptionDialog(mActivity,1);
                dialog.show();
                WindowManager windowManager = mActivity.getWindowManager();
                Display display = windowManager.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.width = (display.getWidth()*1);
                dialog.getWindow().setAttributes(lp);
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
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
    }
}
