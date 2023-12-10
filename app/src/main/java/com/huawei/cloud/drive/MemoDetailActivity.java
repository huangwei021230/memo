package com.huawei.cloud.drive;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.cloud.drive.bean.MemoInfo;

import boogiepop.memo.R;

public class MemoDetailActivity extends AppCompatActivity {
    public static final String EXTRA_MEMO = "extra_memo";
    public static final int REQUEST_CODE = 1;
    public static Long memoCounter = 1L;
    private EditText titleEditText;
    private EditText contentEditText;

    private MemoInfo memo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_detail);

        // 获取备忘录对象
        Intent intent = getIntent();
        Bundle bundle = getIntent().getBundleExtra(EXTRA_MEMO);
        if (bundle != null) {
            // 从Bundle中创建MemoInfo对象
            memo = MemoInfo.fromBundle(bundle);
        }

        // 设置标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 初始化视图
        titleEditText = findViewById(R.id.title_edit_text);
        contentEditText = findViewById(R.id.content_edit_text);
        Button saveButton = findViewById(R.id.save_button);

        // 设置返回按钮点击事件
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMemo();
            }
        });

        // 填充备忘录数据
        if (memo != null) {
            titleEditText.setText(memo.getTitle());
            contentEditText.setText(memo.getContent());
        }
    }

    private void saveMemo() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            // 标题和内容都为空，不保存备忘录
            setResult(RESULT_CANCELED);
        } else {
            if (memo == null) {
                // 新建备忘录项
                memo = new MemoInfo(memoCounter++,title, content);
            } else {
                // 更新备忘录项
                memo.setTitle(title);
                memo.setContent(content);
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_MEMO, memo.toBundle());
            setResult(RESULT_OK, resultIntent);
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 处理返回按钮点击事件
            saveMemo(); // 在返回前保存备忘录
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 处理返回按钮点击事件
        super.onBackPressed();
        saveMemo(); // 在返回前保存备忘录
    }
}