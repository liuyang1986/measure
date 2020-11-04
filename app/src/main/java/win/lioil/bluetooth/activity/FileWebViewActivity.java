package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.TbsReaderView;
import java.io.File;

import win.lioil.bluetooth.R;

public class FileWebViewActivity extends Activity implements TbsReaderView.ReaderCallback {

    private static final String TAG = "Test FileWebView";

    private RelativeLayout rlHead;

    private TextView txtTitle;

    private TextView lastStep,nextStep;

    private int currentStep = 0;

    private String localFilePath;

    private String title;

    // 文件的存储路径
    private String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/temp/";

    private TbsReaderView mTbsReaderView;
    private RelativeLayout mRelativeLayout;
    private LinearLayout llBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview_webview);

        Intent intent = getIntent();
        if (intent != null)
        {
            currentStep = intent.getIntExtra("step",0);
            localFilePath = intent.getStringExtra("localPath");
            title = intent.getStringExtra("title");
        }

        initX5Core();
        initView();
        initClickEvent();

        if (!TextUtils.isEmpty(localFilePath))
        {
            File file = new File(localFilePath);
            if (file.exists())
            {
                displayFile(localFilePath);
            }
            else
            {
                Log.d(TAG,"file is not exists");
            }
        }
        else
        {
            Log.d(TAG,"file is not exists");
        }

    }

    private void displayFile(String filePath) {

        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        String bsReaderTemp = BASE_PATH;
        File bsReaderTempFile = new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            Log.d(TAG, "准备创建/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if (!mkdir) {
                Log.d(TAG, "创建/TbsReaderTemp失败！！！！！");
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString("filePath", filePath);
        bundle.putString("tempPath", BASE_PATH);
        boolean result = mTbsReaderView.preOpen("txt", false);
        Log.d(TAG, "查看文档---" + result);
        if (result) {
            mTbsReaderView.openFile(bundle);
        }
    }

    private void initX5Core()
    {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.setDownloadWithoutWifi(true);//非wifi条件下允许下载X5内核

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.e("apptbs", " onViewInitFinished is " + b);
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                Log.d("apptbs", "onDownloadFinish");
            }

            @Override
            public void onInstallFinish(int i) {
                Log.d("apptbs", "onInstallFinish");
            }

            @Override
            public void onDownloadProgress(int i) {
                Log.d("apptbs", "onDownloadProgress:" + i);
            }
        });

        QbSdk.initX5Environment(getApplicationContext(), cb);
    }

    private void initView()
    {
        rlHead = findViewById(R.id.rl_head);
        llBack = findViewById(R.id.ll_back);
        txtTitle = findViewById(R.id.txt_title);
        lastStep = findViewById(R.id.last_step);
        nextStep = findViewById(R.id.next_step);

        mTbsReaderView = new TbsReaderView(this, this);
        mRelativeLayout = findViewById(R.id.tbsView);
        mRelativeLayout.addView(mTbsReaderView, new RelativeLayout.LayoutParams(-1, -1));


        if (currentStep == 0)
        {
            lastStep.setVisibility(View.GONE);
        }
        else if (currentStep == 3)
        {
            lastStep.setVisibility(View.VISIBLE);
            nextStep.setVisibility(View.GONE);
        }
        else
        {
            lastStep.setVisibility(View.VISIBLE);
            nextStep.setVisibility(View.VISIBLE);
        }

        txtTitle.setText(title);
    }

    private void initClickEvent()
    {
        lastStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {
        Log.d(TAG, "==================+++++====-=-=++" + integer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTbsReaderView !=null)
        {
            mTbsReaderView.onStop();
        }
    }
}
