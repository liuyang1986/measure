package win.lioil.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import win.lioil.bluetooth.activity.HomeActivity;
import win.lioil.bluetooth.config.Config;
import win.lioil.bluetooth.util.ShareRefrenceUtil;

import static android.widget.Toast.LENGTH_LONG;

public class LoginActivity extends Activity {

    private static final String TAG = "Test LoginActivity";

    private EditText edtAccount;

    private ImageView accountClear;

    private View accountBL;

    private EditText edtPassword;

    private ImageView passwordClear;

    private View passwordBL;

    private TextView txtLogin;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initView();
        setOnClickEvent();
    }

    private void initView() {
        edtAccount = findViewById(R.id.edit_account);
        accountClear = findViewById(R.id.account_clear);
        accountBL = findViewById(R.id.account_bottom_line);

        edtPassword = findViewById(R.id.edit_password);
        passwordClear = findViewById(R.id.password_clear);
        passwordBL = findViewById(R.id.password_bottom_line);

        txtLogin = findViewById(R.id.txt_login);

        accountClear.setVisibility(View.GONE);
        passwordClear.setVisibility(View.GONE);

    }

    private void setOnClickEvent() {
        InputFilter inputFilter = new InputFilter() {
            Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Matcher emojiMatcher = emoji.matcher(source);
                if (emojiMatcher.find()) {
                    Toast.makeText(LoginActivity.this, "不支持输入表情", LENGTH_LONG).show();
                    return "";
                }
                return null;
            }
        };

        edtAccount.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(32)});
        edtAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (passwordClear.getVisibility() == View.VISIBLE) {
                    passwordClear.setVisibility(View.GONE);
                }

                if (edtAccount.getText().toString().length() > 0) {
                    accountClear.setVisibility(View.VISIBLE);
                } else {
                    accountClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtAccount.getText().toString().length() > 0) {
                    accountClear.setVisibility(View.VISIBLE);
                } else {
                    accountClear.setVisibility(View.GONE);
                }

                checkLoginButton();
            }
        });
        edtAccount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    accountBL.setBackground(getDrawable(R.color.background_color));
                    passwordBL.setBackground(getDrawable(R.color.background_lose_focus));

                    if (passwordClear.getVisibility() == View.VISIBLE) {
                        passwordClear.setVisibility(View.GONE);
                    }

                    if (edtAccount.getText().toString().length() > 0) {
                        accountClear.setVisibility(View.VISIBLE);
                    } else {
                        accountClear.setVisibility(View.GONE);
                    }
                }

            }
        });
        edtAccount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    edtAccount.setCursorVisible(true);// 再次点击显示光标
                }
                return false;
            }
        });


        edtPassword.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(32)});
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (edtPassword.getText().toString().length() > 0) {
                    passwordClear.setVisibility(View.VISIBLE);
                } else {
                    passwordClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtPassword.getText().toString().length() > 0) {
                    passwordClear.setVisibility(View.VISIBLE);
                } else {
                    passwordClear.setVisibility(View.GONE);
                }

                checkLoginButton();
            }
        });
        edtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    passwordBL.setBackground(getDrawable(R.color.background_color));
                    accountBL.setBackground(getDrawable(R.color.background_lose_focus));

                    if (accountClear.getVisibility() == View.VISIBLE) {
                        accountClear.setVisibility(View.GONE);
                    }

                    if (edtPassword.getText().toString().length() > 0) {
                        passwordClear.setVisibility(View.VISIBLE);
                    } else {
                        passwordClear.setVisibility(View.GONE);
                    }
                }
            }
        });
        edtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    edtPassword.setCursorVisible(true);// 再次点击显示光标
                }
                return false;
            }
        });

        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        accountClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountClear.setVisibility(View.GONE);
                edtAccount.setText("");

                checkLoginButton();
            }
        });

        passwordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordClear.setVisibility(View.GONE);
                edtPassword.setText("");

                checkLoginButton();
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInput(LoginActivity.this, v);
                accountClear.setVisibility(View.GONE);
                passwordClear.setVisibility(View.GONE);

                edtAccount.setCursorVisible(false);
                edtPassword.setCursorVisible(false);
                requestLogin(edtAccount.getText().toString(), edtPassword.getText().toString());
            }
        });
    }


    private void checkLoginButton() {
        if (!TextUtils.isEmpty(edtPassword.getText().toString())
                && !TextUtils.isEmpty(edtAccount.getText().toString())) {
            txtLogin.setBackground(getDrawable(R.drawable.button_enable_corner_rect));
            txtLogin.setTextColor(Color.parseColor("#ffffff"));
        } else {
            txtLogin.setBackground(getDrawable(R.drawable.button_disable_corner_rect));
            txtLogin.setTextColor(Color.parseColor("#999999"));
        }
    }

    private void requestLogin(final String loginName, final String loginPassword) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showWaitingDialog();
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    Log.d(TAG, "request login , name is " + loginName + " , password is " + loginPassword);
                    jsonObject.put("dlzh", loginName);
                    jsonObject.put("dlmm", loginPassword);

                    String localURL = Config.SERVER_PREFIXX + "user/login";

                    String requestJSon = jsonObject.toString();

                    Request request = new Request.Builder().url(localURL).post(
                            RequestBody.create(mediaType, requestJSon)).build();

                    final Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody is " + responseBody);

                        final JSONObject responseJson = new JSONObject(responseBody);
                        if (TextUtils.equals(responseJson.optString("msg").toLowerCase(), "success")) {
                            ShareRefrenceUtil.save(LoginActivity.this, "userName", loginName);
                            ShareRefrenceUtil.save(LoginActivity.this, "userPassword", loginPassword);

                            JSONObject dataJSON = responseJson.optJSONObject("data");
                            Config.AUTH_TOKEN = dataJSON.optString("token");
                            Config.AUTH_XM = dataJSON.optString("xm");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "欢迎您回来，" + Config.AUTH_XM, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            ShareRefrenceUtil.save(LoginActivity.this, "userName", "");
                            ShareRefrenceUtil.save(LoginActivity.this, "userPassword", "");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "登录失败，" + responseJson.optString("data"), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "网络异常，错误码为" + response.code(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "发生异常，" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } finally {
                    dismissWaitingDialog();
                }
            }
        }).start();


    }

    private void showWaitingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(LoginActivity.this);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage("登陆中，请稍等...");
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
     * 强制隐藏输入法键盘
     *
     * @param context Context
     * @param view    EditText
     */
    public static void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String userName = (String) ShareRefrenceUtil.get(LoginActivity.this, "userName");
        String userPassword = (String) ShareRefrenceUtil.get(LoginActivity.this, "userPassword");

        if (edtAccount != null) {
            edtAccount.setText(TextUtils.isEmpty(userName) ? "" : userName);
        }

        if (edtPassword != null) {
            edtPassword.setText(TextUtils.isEmpty(userPassword) ? "" : userPassword);
        }

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPassword)) {
            requestLogin(userName, userPassword);
        }
    }
}
