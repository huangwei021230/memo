package com.huawei.cloud.drive.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.Fragment;

import com.huawei.cloud.drive.MainActivity;
import com.huawei.cloud.drive.hms.HmsServiceManager;

import boogiepop.memo.R;

public class NoteDetailFragment extends Fragment {
    // ... 其他代码
    private HmsServiceManager hmsServiceManager = new HmsServiceManager(getContext());
    private ImageView imageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_detail_fragment, container, false);

        TextView textTitleView = view.findViewById(R.id.textViewNoteTitle);
        TextView textContentView = view.findViewById(R.id.textViewNoteContent);
        imageView = view.findViewById(R.id.imageViewBack);


        // 获取传递过来的备忘录内容并显示在 TextView 中
        if (getArguments() != null && getArguments().containsKey("noteText")) {
            String noteText = getArguments().getString("noteText");
            textContentView.setText(noteText);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.mTabsAdapter.selectPaper(0);
                mainActivity.mTabsAdapter.mTabs.remove(1);
            }
        });
        return view;
    }

}

