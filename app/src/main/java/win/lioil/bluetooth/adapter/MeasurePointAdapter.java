package win.lioil.bluetooth.adapter;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.LevelingRouteBean;
import win.lioil.bluetooth.bean.MeasurePointBean;

public class MeasurePointAdapter extends CommonAdapter<MeasurePointBean> {

    private CommonViewHolder.onItemCommonClickListener commonClickListener;


    public MeasurePointAdapter(Context context, List<MeasurePointBean> dataList, int layoutId, CommonViewHolder.onItemCommonClickListener listener) {
        super(context, dataList, layoutId);
        commonClickListener = listener;
    }

    @Override
    public void bindData(CommonViewHolder holder, MeasurePointBean data, int pos) {

        String measureType = data.getCdlx();
        if (TextUtils.equals(measureType,"1"))
        {
            measureType = "工作基点";
        }
        else
        {
            measureType = "监测点";
        }

        holder.setText(R.id.id_index, String.valueOf(pos+1))
                .setText(R.id.id_cdmc, data.getCdmc())
                .setText(R.id.id_cdlx, measureType)
                .setText(R.id.id_cdgc, data.getCdcsgc())
                .setText(R.id.id_sccdgc, data.getScclgc())
                .setCommonClickListener(commonClickListener);
    }
}
