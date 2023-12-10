/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.huawei.cloud.drive.bean;

import android.os.Bundle;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.io.Serializable;

/**
 * Definition of ObjectType MemoInfo.
 *
 * @since 2023-12-10
 */
@PrimaryKeys({"id"})
public final class MemoInfo extends CloudDBZoneObject implements Serializable {
    private Long id;

    private String title;

    private String content;

    public MemoInfo() {
        super(MemoInfo.class);
    }
    public MemoInfo(Long id, String title, String content){
        super(MemoInfo.class);
        this.id = id;
        this.title = title;
        this.content = content;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        bundle.putString("title", title);
        bundle.putString("content", content);
        return bundle;
    }
    // 从Bundle中提取属性值，并创建新的MemoInfo对象
    public static MemoInfo fromBundle(Bundle bundle) {
        Long id = bundle.getLong("id");
        String title = bundle.getString("title");
        String content = bundle.getString("content");
        return new MemoInfo(id, title, content);
    }
}
