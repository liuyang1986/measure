package win.lioil.bluetooth.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.DetailBean;
import win.lioil.bluetooth.config.Config;

public class DetailAdapter extends RecyclerView.Adapter {

    private static final String TAG = "Test FeedBackAdapter";

    private Context mContext;
    private List<DetailBean> mDetailBeanList;
    private int mType = Config.XM_TYPE;

    public DetailAdapter(Context context, List<DetailBean> list,int type) {
        this.mContext = context;
        this.mDetailBeanList = list;
        this.mType = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_detail, parent, false);
        return new DetailBeanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (position >= mDetailBeanList.size())
        {
            return ;
        }
        DetailBean bean = mDetailBeanList.get(position);
        ((DetailBeanViewHolder)holder).txtTitle.setText(bean.getName());
        ((DetailBeanViewHolder) holder).txtDesc.setText(bean.getDesc());

        if (mType == Config.SUBGRADE_TYPE)
        {
            ((DetailBeanViewHolder) holder).rlNext.setVisibility(View.GONE);
        }


        ((DetailBeanViewHolder) holder).rlNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);
                }
            }
        });


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


    private class DetailBeanViewHolder extends RecyclerView.ViewHolder{

        private TextView txtTitle;
        private TextView txtDesc;
        private RelativeLayout rlNext;

        public DetailBeanViewHolder(View itemView) {
            super(itemView);
            txtTitle =  itemView.findViewById(R.id.txt_name);
            txtDesc =  itemView.findViewById(R.id.txt_desc);
            rlNext = itemView.findViewById(R.id.rl_goto_detail);
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
