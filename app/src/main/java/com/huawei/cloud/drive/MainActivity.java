package com.huawei.cloud.drive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.cloud.drive.adapter.MemoAdapter;
import com.huawei.cloud.drive.bean.MemoInfo;
import com.huawei.cloud.drive.hms.CloudDBManager;
import com.huawei.cloud.drive.hms.CloudDBManager.UiCallBack;

import java.util.ArrayList;
import java.util.List;

import boogiepop.memo.R;


/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements UiCallBack {
    // 假设有一个 Memo 类来表示备忘录项
    private CloudDBManager cloudDBManager;
    private List<MemoInfo> memoList;
    // 适配器用于显示备忘录列表
    private MemoAdapter memoAdapter;

    @Override
    public void onAddOrQuery(List<MemoInfo> memoInfoList) {
        memoAdapter.addMemos(memoInfoList);
    }

    @Override
    public void onSubscribe(List<MemoInfo> memoInfoList) {

    }

    @Override
    public void onDelete(List<MemoInfo> memoInfoList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cloudDBManager = new CloudDBManager();
        cloudDBManager.createObjectType();
        cloudDBManager.addCallBacks(this);
        cloudDBManager.openCloudDBZoneV2(new CloudDBManager.CloudDBZoneOpenCallback() {
            @Override
            public void onCloudDBZoneOpened(CloudDBZone cloudDBZone) {

                cloudDBManager.queryAllMemos();
            }
        });
        // 初始化备忘录列表和适配器
        memoList = new ArrayList<>();
        memoAdapter = new MemoAdapter(memoList);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(memoAdapter);

        // 点击备忘录项时打开详情页面
        memoAdapter.setOnItemClickListener(new MemoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                MemoInfo memo = memoList.get(position);
                openMemoDetail(memo);
            }
        });

        // 添加按钮点击事件
        FloatingActionButton addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMemoDetail(new MemoInfo(MemoDetailActivity.memoCounter++,"this is a title","default")); // 打开详情页面以创建新的备忘录项
            }
        });
    }

    private void openMemoDetail(MemoInfo memo) {
        Intent intent = new Intent(this, MemoDetailActivity.class);
        intent.putExtra(MemoDetailActivity.EXTRA_MEMO, memo.toBundle());
        startActivityForResult(intent, MemoDetailActivity.REQUEST_CODE);
    }
    private boolean isNew(MemoInfo memoInfo){
        for(MemoInfo memo : memoList){
            if(memo.getId() == memoInfo.getId()){
                return false;
            }
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MemoDetailActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            // 处理从详情页面返回的结果
            if (data != null && data.hasExtra(MemoDetailActivity.EXTRA_MEMO)) {
                Bundle bundle = data.getParcelableExtra(MemoDetailActivity.EXTRA_MEMO);
                if(bundle!=null){
                    MemoInfo memo = MemoInfo.fromBundle(bundle);
                    if (memo != null) {
                        if (isNew(memo)) {
                            // 新建备忘录项
                            memoList.add(memo);
                            cloudDBManager.upsertMemoInfos(memo);
                        } else {
                            // 更新备忘录项
                            int position = memoList.indexOf(memo);
                            if (position != -1) {
                                memoList.set(position, memo);
                            }
                        }
                        // 刷新备忘录列表
                        memoAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}

