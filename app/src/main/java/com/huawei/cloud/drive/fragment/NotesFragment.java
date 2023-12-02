package com.huawei.cloud.drive.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.huawei.cloud.drive.MainActivity;
import com.huawei.cloud.drive.hms.HmsServiceManager;

import java.util.ArrayList;

import boogiepop.memo.R;

public class NotesFragment extends Fragment {
    private HmsServiceManager hmsServiceManager;
    private static final String TAG = "NotesFragment";

    private ListView listViewNotes;
    private ArrayAdapter<String> notesAdapter;
    private ArrayList<String> notesList;

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
        notesList.add("Note 1");
        notesList.add("Note 2");
        notesList.add("Note 3");
        // 创建适配器并设置给 ListView
        notesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, notesList);

        listViewNotes.setAdapter(notesAdapter);
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





}
