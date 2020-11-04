package win.lioil.bluetooth.widget;

import android.app.Activity;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/*
 * 监听输入内容是否超出最大长度，并设置光标位置
 * */
public class MaxLengthWatcher implements TextWatcher {

  private int maxLen = 0;
  private EditText editText = null;
  private WeakReference<Activity> activity;
  private TextView tvCount;


  public MaxLengthWatcher(Activity activity, TextView tvCount, int maxLen, EditText editText) {
    this.maxLen = maxLen;
    this.editText = editText;
    this.activity = new WeakReference<Activity>(activity);
    this.tvCount = tvCount;
  }

  public void afterTextChanged(Editable arg0) {
    // TODO Auto-generated method stub

  }

  public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                int arg3) {
    // TODO Auto-generated method stub

  }

  public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    // TODO Auto-generated method stub
    Editable editable = editText.getText();
    int len = editable.length();

    if (tvCount != null)
    {
      tvCount.setText(String.format("%d/500", len));
    }

    if(len > maxLen)
    {
      int selEndIndex = Selection.getSelectionEnd(editable);
      String str = editable.toString();
      //截取新字符串
      String newStr = str.substring(0,maxLen);
      editText.setText(newStr);
      editable = editText.getText();

      //新字符串的长度
      int newLen = editable.length();
      //旧光标位置超过字符串长度
      if(selEndIndex > newLen)
      {
        selEndIndex = editable.length();
      }
      //设置新光标所在的位置
      Selection.setSelection(editable, selEndIndex);

      Toast.makeText(activity.get(), String.format("最多只允许输入%d字",maxLen), Toast.LENGTH_SHORT).show();
    }
  }
}

