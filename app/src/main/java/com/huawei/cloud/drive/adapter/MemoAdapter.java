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
        for (MemoInfo newMemo : list) {
            boolean isExisting = false;
            for (int i = 0; i < memoList.size(); i++) {
                MemoInfo existingMemo = memoList.get(i);
                if (existingMemo.getId().equals(newMemo.getId())) {
                    // 相同id的对象已存在，更新内容
                    existingMemo.setTitle(newMemo.getTitle());
                    existingMemo.setContent(newMemo.getContent());
                    notifyItemChanged(i); // 通知适配器有数据更新
                    isExisting = true;
                    break;
                }
            }
            if (!isExisting) {
                // 相同id的对象不存在，添加到列表末尾
                memoList.add(newMemo);
                notifyItemInserted(memoList.size() - 1); // 通知适配器有新的数据插入
            }
        }
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