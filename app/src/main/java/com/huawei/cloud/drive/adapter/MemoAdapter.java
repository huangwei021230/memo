package com.huawei.cloud.drive.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huawei.cloud.drive.bean.MemoInfo;

import java.util.List;

import boogiepop.memo.R;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {
    private List<MemoInfo> memoList;
    private OnItemClickListener onItemClickListener;

    public MemoAdapter(List<MemoInfo> memoList) {
        this.memoList = memoList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MemoInfo memo = memoList.get(position);
        holder.bind(memo);
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void addMemos(List<MemoInfo> list) {
        int startPosition = memoList.size(); // 获取当前列表的末尾位置
        memoList.addAll(list); // 将新的MemoInfo对象列表添加到原有列表中
        notifyItemRangeInserted(startPosition, list.size()); // 通知适配器有新的数据插入
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;
        private TextView contentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            contentTextView = itemView.findViewById(R.id.content_text_view);
            itemView.setOnClickListener(this);
        }

        public void bind(MemoInfo memo) {
            titleTextView.setText(memo.getTitle());
            contentTextView.setText(memo.getContent());
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }
}