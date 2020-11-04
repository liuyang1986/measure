package win.lioil.bluetooth.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.ShareRefrenceUtil;

public class MineFragment extends BaseFragment {

    private TextView txtLoginName;

    private FragmentActivity mActivity;

    @Override
    public void fetchData() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        initView(view);
        return view;
    }

    private void initView(View view)
    {
        txtLoginName = view.findViewById(R.id.txt_login_name);

        String userName = (String) ShareRefrenceUtil.get(mActivity,"userName");
        txtLoginName.setText(TextUtils.isEmpty(userName)?"未设置":userName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity)context;
    }
}
