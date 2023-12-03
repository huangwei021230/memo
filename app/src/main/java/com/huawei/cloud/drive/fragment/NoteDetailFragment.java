package com.huawei.cloud.drive.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.AsyncTask;
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

import boogiepop.memo.R;

public class NoteDetailFragment extends Fragment {
    private HmsServiceManager hmsServiceManager;
    private ImageView imageView;
    private TabsAdapter mTabsAdapter;
    private String fileId;
    private String fileName;
    EditText editTextNoteTitle;
    EditText editTextNoteContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_detail_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        mTabsAdapter = mainActivity.mTabsAdapter;
        hmsServiceManager = new HmsServiceManager(getContext());

        imageView = view.findViewById(R.id.imageViewBack);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        editTextNoteTitle = view.findViewById(R.id.editTextNoteTitle);
        editTextNoteContent = view.findViewById(R.id.editTextNoteContent);

        // 获取传递过来的备忘录内容并显示在 TextView 中
        if (getArguments() != null && getArguments().containsKey("fileName")) {
            fileName = getArguments().getString("fileName");
            editTextNoteTitle.setText(fileName);
        }
        if (getArguments() != null && getArguments().containsKey("fileId")) {
            fileId = getArguments().getString("fileId");
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTabsAdapter.selectPaper("NotesFragment");
                mTabsAdapter.notifyDataSetChanged();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        new FileGetAsyncTask().execute();

        return view;
    }
    @SuppressLint("StaticFieldLeak")
    private class FileGetAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String content = hmsServiceManager.getFileContent(fileId, fileName);
            return content;
        }
        @Override
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            editTextNoteContent.setText(str);
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}

