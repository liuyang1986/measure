package win.lioil.bluetooth.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.ClientUploadUtils;
import win.lioil.bluetooth.util.FileUtils;
import win.lioil.bluetooth.widget.ZoomListener;

public class FilePreviewActivity  extends Activity {

    private static final String TAG = "Test FilePreview";

    private RelativeLayout rlHead;

    private TextView txtTitle;

    private TextView lastStep,nextStep;

    private RelativeLayout rlLast,rlNext;

    private int currentStep = 0;

    private String localFilePath;

    private LinearLayout llBack;

    private TextView txtLog;

    private ProgressDialog mProgressDialog;

    private Activity mActivity;

    //水准线路ID
    private String levelingLineId;

    //水准线路编号
    private String levelingLineNo;

    private String fileName = "";

    private ArrayList<String> previewFileNameList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_preview);
        mActivity = this;

        Intent intent = getIntent();
        if (intent != null)
        {
            currentStep = intent.getIntExtra("step",0);
            previewFileNameList  = intent.getStringArrayListExtra("previewFileNameList");
            levelingLineNo = intent.getStringExtra("levelingLineNo");
            levelingLineId = intent.getStringExtra("levelingLineId");

            if (previewFileNameList!=null && previewFileNameList.size()>currentStep)
            {
                localFilePath = previewFileNameList.get(currentStep);
            }

        }

        initView();
        initClickEvent();

        readLog();
    }


    private void initView()
    {
        rlHead = findViewById(R.id.rl_head);
        llBack = findViewById(R.id.ll_back);
        txtTitle = findViewById(R.id.txt_title);
        lastStep = findViewById(R.id.last_step);
        nextStep = findViewById(R.id.next_step);
        rlLast = findViewById(R.id.rl_last);
        rlNext = findViewById(R.id.rl_next);
        txtLog = findViewById(R.id.txt_content);


        txtTitle.setText(FileUtils.getFileName(localFilePath));
        if (currentStep == 0)
        {
            rlLast.setVisibility(View.GONE);
        }
        else if (currentStep == previewFileNameList.size()-1)
        {
            nextStep.setText("确认上传");
        }
        else
        {
            rlLast.setVisibility(View.VISIBLE);
            rlNext.setVisibility(View.VISIBLE);
        }

        txtLog.setOnTouchListener(new ZoomListener());
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

    private void readLog()
    {
        try {
            InputStream is = new FileInputStream(new File(localFilePath));
            byte[] bytes = new byte[is.available()];
            int len = is.read(bytes);
            if (len != 0) {
                String rawTxt = new String(bytes);
                txtLog.setText(rawTxt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initClickEvent()
    {
        lastStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStep == 0)
                {
                    finish();
                }
                else
                {
                    skipToLastFilePreview();
                }
            }
        });

        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStep == previewFileNameList.size()-1)
                {
//                    Toast.makeText(mActivity,"已经是最后一页了",Toast.LENGTH_LONG).show();
                    uploadFileList();
                }
                else
                {
                    skipToNextFilePreview();
                }
            }
        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void skipToLastFilePreview()
    {
        Intent intent = new Intent(FilePreviewActivity.this,FilePreviewActivity.class);
        intent.putExtra("step",currentStep-1);
        intent.putExtra("previewFileNameList",previewFileNameList);
        intent.putExtra("levelingLineNo",levelingLineNo);
        intent.putExtra("levelingLineId",levelingLineId);
        startActivity(intent);
    }



    private void skipToNextFilePreview()
    {
        Intent intent = new Intent(FilePreviewActivity.this,FilePreviewActivity.class);
        intent.putExtra("step",currentStep+1);
        intent.putExtra("previewFileNameList",previewFileNameList);
        intent.putExtra("levelingLineNo",levelingLineNo);
        intent.putExtra("levelingLineId",levelingLineId);
        startActivity(intent);
    }

    private void uploadFileList()
    {
        showWaitingDialog("正在上传文件中，请稍等...");
        for (int i=0; i<previewFileNameList.size(); i++)
        {
            uploadFile(previewFileNameList.get(i),
                    FileUtils.getFileName(previewFileNameList.get(i)));
        }
        dismissWaitingDialog();
    }

    private void uploadFile(final String filePath, final String fileName)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String requestUrl = Config.SERVER_PREFIXX + "monitFile/appUpload";
                HashMap<String,String> param = new HashMap<>();
                //水准线路主键
                param.put("szxlid",levelingLineId);
                //测量记录名称
                param.put("cljlmc",levelingLineNo + "." + FileUtils.getFileExtension(filePath));
                //文件类型
                param.put("wjlx",String.valueOf(1));
                try {
                    ClientUploadUtils.upload(requestUrl,filePath,fileName,param);
                }
                catch (Exception e)
                {
                    Log.e(TAG,e.getMessage());
                }

            }
        }).start();
    }
}
