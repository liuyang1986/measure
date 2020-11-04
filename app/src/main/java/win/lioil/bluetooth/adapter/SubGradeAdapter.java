package win.lioil.bluetooth.adapter;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.SubGradeBean;

public class SubGradeAdapter extends CommonAdapter<SubGradeBean> {

    private CommonViewHolder.onItemCommonClickListener commonClickListener;


    public SubGradeAdapter(Context context, List<SubGradeBean> dataList, int layoutId, CommonViewHolder.onItemCommonClickListener listener) {
        super(context, dataList, layoutId);
        commonClickListener = listener;
    }

    @Override
    public void bindData(CommonViewHolder holder, SubGradeBean data, int pos) {
        String state = data.getGczt();
        if (TextUtils.equals(state,"1"))
        {
            state = "在测";
        }
        else
        {
            state = "停测";
        }

        String type = data.getDmlx();
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

        holder.setText(R.id.id_index, String.valueOf(pos+1))
                .setText(R.id.id_dmmc, data.getDmmc())
                .setText(R.id.id_dmbm, data.getDmbm())
                .setText(R.id.id_dmlx, type)
                .setText(R.id.id_gzwmc, data.getGzwmc())
                .setText(R.id.id_gzwlx, data.getGzwlx())
                .setText(R.id.id_djclfs, data.getDjclfs())
                .setText(R.id.id_sglc, data.getSglc())
                .setText(R.id.id_sgcdl, data.getSgcdl())
                .setText(R.id.id_gczt, state)
                .setCommonClickListener(commonClickListener);
    }
}
