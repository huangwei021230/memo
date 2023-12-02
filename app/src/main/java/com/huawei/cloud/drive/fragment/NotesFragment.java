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
    private ArrayList<String> notesList;
    private ArrayAdapter<String> notesAdapter;
    public NotesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memo_fragment, container, false);

        listViewNotes = view.findViewById(R.id.listViewNotes);
        hmsServiceManager = new HmsServiceManager(getContext());
        notesList = new ArrayList<>();
        // 创建适配器并设置给 ListView
        notesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, notesList);

        listViewNotes.setAdapter(notesAdapter);
        mTabsAdapter = ((MainActivity) getActivity()).mTabsAdapter;
        new FileListAsyncTask().execute();

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 当用户点击列表项时，切换到显示备忘录详情的 Fragment
                String selectedNote = notesList.get(position);

                Bundle bundle = new Bundle();
                bundle.putString("noteText", selectedNote);

                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.mTabsAdapter.addTab(NoteDetailFragment.class, bundle);
                mainActivity.mTabsAdapter.notifyDataSetChanged();
                Log.d("emergency", String.valueOf(mainActivity.mTabsAdapter.mTabs.size()));
                mainActivity.mTabsAdapter.selectPaper(1); // 调用onTabSelected方法切换选项卡
            }
        });

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class FileListAsyncTask extends AsyncTask<Void, Void, List<File>> {
        @Override
        protected List<File> doInBackground(Void... voids) {
            List<File> folders = null;
            try {
                folders = hmsServiceManager.getFileList("mimeType = 'application/vnd.huawei-apps.folder'", "fileName", 10, "*");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return folders;
        }

        @Override
        protected void onPostExecute(List<File> folders) {
            if (folders != null) {
                for(File file : folders){
                    notesList.add(file.getFileName());
                }
                // 通知适配器数据已更改
                notesAdapter.notifyDataSetChanged();
            } else {
                // 处理获取文件列表失败的情况
                // 可以进行错误处理或者提示用户获取失败
            }
        }
    }



}
