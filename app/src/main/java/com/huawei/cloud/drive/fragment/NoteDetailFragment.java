package com.huawei.cloud.drive.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.huawei.cloud.drive.MainActivity;
import com.huawei.cloud.drive.adapter.TabsAdapter;
import com.huawei.cloud.drive.hms.HmsServiceManager;
import com.huawei.cloud.services.drive.model.File;

import boogiepop.memo.R;

public class NoteDetailFragment extends Fragment {
    private HmsServiceManager hmsServiceManager = new HmsServiceManager(getContext());
    private ImageView imageView;
    private TabsAdapter mTabsAdapter;
    private File mFile;
    private File mDirectory;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_detail_fragment, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mTabsAdapter = mainActivity.mTabsAdapter;


        EditText editTextNoteTitle = view.findViewById(R.id.editTextNoteTitle);
        EditText editTextNoteContent = view.findViewById(R.id.editTextNoteContent);
        imageView = view.findViewById(R.id.imageViewBack);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        // 获取传递过来的备忘录内容并显示在 TextView 中
        if (getArguments() != null && getArguments().containsKey("noteText")) {
            String noteText = getArguments().getString("noteText");
            editTextNoteTitle.setText(noteText);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTabsAdapter.selectPaper(0);
                mTabsAdapter.mTabs.remove(1);
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

}

