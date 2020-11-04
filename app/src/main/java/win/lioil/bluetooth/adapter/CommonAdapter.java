package win.lioil.bluetooth.adapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.R;


/**
 * Created by chawei on 2018/4/29.
 */

public abstract class CommonAdapter<T> extends RecyclerView.Adapter<CommonAdapter.CommonViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<T> mDataList;
    private int mLayoutId;
    private int mFixX;
    private ArrayList<View> mMoveViewList = new ArrayList<>();

    public CommonAdapter(Context context, List<T> dataList, int layoutId) {
        mLayoutInflater = LayoutInflater.from(context);
        mDataList = dataList;
        mLayoutId = layoutId;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(mLayoutId, parent, false);
        CommonViewHolder holder = new CommonViewHolder(itemView);
        //获取可滑动的view布局
        LinearLayout moveLayout = holder.getView(R.id.id_move_layout);
        moveLayout.scrollTo(mFixX, 0);
        mMoveViewList.add(moveLayout);
        return holder;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {
        bindData(holder, mDataList.get(position),position);
    }

    @Override
    public int getItemCount() {
        return mDataList==null?0:mDataList.size();
    }

    public abstract void bindData(CommonViewHolder holder, T data,int position);

    public ArrayList<View> getMoveViewList(){
        return mMoveViewList;
    }

    public void setFixX(int fixX){
        mFixX=fixX;
    }



    public static class CommonViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        private SparseArray<View> viewSparseArray;

        private onItemCommonClickListener commonClickListener;

        public CommonViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            viewSparseArray = new SparseArray<>();
        }

        /**
         * 根据 ID 来获取 View
         *
         * @param viewId viewID
         * @param <T>    泛型
         * @return 将结果强转为 View 或 View 的子类型
         */
        public <T extends View> T getView(int viewId) {
            // 先从缓存中找，找打的话则直接返回
            // 如果找不到则 findViewById ，再把结果存入缓存中
            View view = viewSparseArray.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                viewSparseArray.put(viewId, view);
            }
            return (T) view;
        }

        public CommonViewHolder setText(int viewId, CharSequence text) {
            TextView tv = getView(viewId);
            tv.setText(text);
            return this;
        }

        public CommonViewHolder setViewVisibility(int viewId, int visibility) {
            getView(viewId).setVisibility(visibility);
            return this;
        }

        public CommonViewHolder setImageResource(int viewId, int resourceId) {
            ImageView imageView = getView(viewId);
            imageView.setImageResource(resourceId);
            return this;
        }

        public interface onItemCommonClickListener {

            void onItemClickListener(int position);

            void onItemLongClickListener(int position);

        }

        public void setCommonClickListener(onItemCommonClickListener commonClickListener) {
            this.commonClickListener = commonClickListener;
        }

        @Override
        public void onClick(View v) {
            if (commonClickListener != null) {
                commonClickListener.onItemClickListener(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (commonClickListener != null) {
                commonClickListener.onItemLongClickListener(getAdapterPosition());
            }
            return false;
        }
    }

}
