package com.huawei.cloud.drive.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.huawei.cloud.drive.MainActivity;
import com.huawei.cloud.drive.adapter.TabsAdapter;
import com.huawei.cloud.drive.hms.HmsServiceManager;
import com.huawei.cloud.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import boogiepop.memo.R;

public class NotesFragment extends Fragment {
    private HmsServiceManager hmsServiceManager;
    private static final String TAG = "NotesFragment";
    private TabsAdapter mTabsAdapter;
    private ListView listViewNotes;
    private Button buttonAddNote;
    private EditText editText;
    private ArrayList<String> notesList;
    private ArrayAdapter<String> notesAdapter;
    public File mDirectory;
    public File mFile;
    public List<File> filesList;
    public NotesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memo_fragment, container, false);

        editText = view.findViewById(R.id.editTextNote);
        buttonAddNote = view.findViewById(R.id.btnAddNote);
        listViewNotes = view.findViewById(R.id.listViewNotes);
        hmsServiceManager = new HmsServiceManager(getContext());

        notesList = new ArrayList<>();
        // 创建适配器并设置给 ListView
        notesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, notesList);

        listViewNotes.setAdapter(notesAdapter);
        mTabsAdapter = ((MainActivity) getActivity()).mTabsAdapter;


        new FileListAsyncTask().execute();
        new FileInFolderListAsyncTask().execute();


        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 当用户点击列表项时，切换到显示备忘录详情的 Fragment
                String selectedNote = notesList.get(position);
                mFile = filesList.get(position-1);
                Bundle bundle = new Bundle();
                bundle.putString("fileName", selectedNote);
                bundle.putString("fileId", mFile.getId());

                mTabsAdapter.addTab(NoteDetailFragment.class, bundle);
                mTabsAdapter.notifyDataSetChanged();
                Log.d("emergency", String.valueOf(mTabsAdapter.mTabs.size()));
                mTabsAdapter.selectPaper(1); // 调用onTabSelected方法切换选项卡
            }
        });
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FileCreateAsyncTask().execute();
            }
        });
        return view;
    }
    @SuppressLint("StaticFieldLeak")
    private class FileCreateAsyncTask extends AsyncTask<Void, Void, File> {
        @Override
        protected File doInBackground(Void... voids) {
            String fileName = editText.getText().toString();
            File file = hmsServiceManager.createTxTFile(fileName,mDirectory);
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            // 直接进行跳转
            mFile = file;
            String selectedNote = file.getFileName();
            Bundle bundle = new Bundle();
            bundle.putString("fileName", selectedNote);
            bundle.putString("fileId", file.getId());
            notesList.add(selectedNote);
            mTabsAdapter.addTab(NoteDetailFragment.class, bundle);
            notesAdapter.notifyDataSetChanged();
            mTabsAdapter.notifyDataSetChanged();
            mTabsAdapter.selectPaper(1);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class FileListAsyncTask extends AsyncTask<Void, Void, File> {
        @Override
        protected File doInBackground(Void... voids) {
            File tempDir = new File();
            List<File> folders = null;
            try {
                folders = hmsServiceManager.getFileList("mimeType = 'application/vnd.huawei-apps.folder'", "fileName", 10, "*");
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean flag = false;
            if (folders != null) {
                for(File file : folders){
                    if(file.getFileName().equals("memo")){
                        tempDir = file;
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    tempDir = hmsServiceManager.createDirectoryWithName("memo");
                }
            } else {
                // 处理获取文件列表失败的情况
                // 可以进行错误处理或者提示用户获取失败
            }
            return tempDir;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mDirectory = file;

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FileInFolderListAsyncTask extends AsyncTask<Void, Void, List<File>> {
        @Override
        protected List<File> doInBackground(Void... voids) {
            List<File> files = null;
            try {
                files = hmsServiceManager.listFilesInFolder(mDirectory);
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
            return files;
        }

        @Override
        protected void onPostExecute(List<File> files) {
            super.onPostExecute(files);
            filesList = files;
            for(File file: files){
                notesList.add(file.getFileName());
            }
            notesAdapter.notifyDataSetChanged();
            mTabsAdapter.notifyDataSetChanged();
        }
    }
}
