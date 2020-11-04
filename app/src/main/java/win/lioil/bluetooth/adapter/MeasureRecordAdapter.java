package win.lioil.bluetooth.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.MeasureRecordBean;

public class MeasureRecordAdapter extends RecyclerView.Adapter {

    private static final String TAG = "Test MeasureRecordAdapter";

    private Context mContext;
    private List<MeasureRecordBean> mDetailBeanList;

    public MeasureRecordAdapter(Context context, List<MeasureRecordBean> list) {
        this.mContext = context;
        this.mDetailBeanList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_measure_record, parent, false);
        return new MeasureRecordAdapter.MeasureRecordBeanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (position >= mDetailBeanList.size())
        {
            return ;
        }
        MeasureRecordBean bean = mDetailBeanList.get(position);
        ((MeasureRecordBeanViewHolder)holder).txtMeasureName.setText(bean.getMeasureName());
        ((MeasureRecordBeanViewHolder) holder).txtMeasureStatus.setText(bean.getMeasureStatus());
        ((MeasureRecordBeanViewHolder)holder).txtMeasureTime.setText(bean.getMeasureTime());
        ((MeasureRecordBeanViewHolder) holder).txtMeasureType.setText(bean.getMeasureType());
        ((MeasureRecordBeanViewHolder)holder).txtMeasureWatcher.setText(bean.getMeasureWatcher());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnItemClickListener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.itemView, pos);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return  (mDetailBeanList==null)?0:mDetailBeanList.size();
    }


    private class MeasureRecordBeanViewHolder extends RecyclerView.ViewHolder{

        private TextView txtMeasureType;
        private TextView txtMeasureName;
        private TextView txtMeasureStatus;
        private TextView txtMeasureTime;
        private TextView txtMeasureWatcher;

        public MeasureRecordBeanViewHolder(View itemView) {
            super(itemView);
            txtMeasureType =  itemView.findViewById(R.id.txt_measure_type);
            txtMeasureName =  itemView.findViewById(R.id.txt_measure_name);
            txtMeasureStatus = itemView.findViewById(R.id.txt_measure_status);
            txtMeasureTime =  itemView.findViewById(R.id.txt_measure_time);
            txtMeasureWatcher = itemView.findViewById(R.id.txt_measure_watcher);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);

        void onItemLongClick(View view,int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
}

