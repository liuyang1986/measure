package win.lioil.bluetooth.adapter;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.LevelingRouteBean;

public class LevelingLineAdapter extends CommonAdapter<LevelingRouteBean> {

    private CommonViewHolder.onItemCommonClickListener commonClickListener;


    public LevelingLineAdapter(Context context, List<LevelingRouteBean> dataList, int layoutId, CommonViewHolder.onItemCommonClickListener listener) {
        super(context, dataList, layoutId);
        commonClickListener = listener;
    }

    @Override
    public void bindData(CommonViewHolder holder, LevelingRouteBean data, int pos) {

        String zy = data.getZy();
        if (TextUtils.equals("2",zy))
        {
            zy = "桥涵";
        }
        else if (TextUtils.equals("3",zy))
        {
            zy = "隧道";
        }
        else
        {
            zy = "路基";
        }

        String states = data.getXlzt();
        if (TextUtils.equals(states,"2"))
        {
            states = "停用";
        }
        else if (TextUtils.equals(states,"3"))
        {
            states = "删除";
        }
        else
        {
            states = "正常";
        }

        String startTime = data.getTjsj();
        if (startTime.contains(",")) {
            startTime = startTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        String endTime = data.getXgsj();
        if (endTime.contains(",")) {
            endTime = endTime.replaceFirst(",", "年").
                    replaceFirst(",", "月").
                    replaceFirst(",", "日 ").
                    replaceFirst(",", "时").
                    replaceFirst(",", "分").replace("[", "").replace("]", "") + "秒";
        }

        holder.setText(R.id.id_index, String.valueOf(pos+1))
                .setText(R.id.id_szxlmc, data.getSzxlmc())
                .setText(R.id.id_szxlbh, data.getSzxlbh())
                .setText(R.id.id_dllx, TextUtils.equals(data.getDllx(),"2")?"山地":"平原")
                .setText(R.id.id_cds, data.getCds())
                .setText(R.id.id_gzjds, data.getGzjds())
                .setText(R.id.id_zy, zy)
                .setText(R.id.id_qslc, data.getQslc())
                .setText(R.id.id_jslc, data.getJslc())
                .setText(R.id.id_tjsj, startTime)
                .setText(R.id.id_xgsj, endTime)
                .setText(R.id.id_szry, data.getSzry())
                .setText(R.id.id_xlzt, states)
                .setCommonClickListener(commonClickListener);
    }

}
