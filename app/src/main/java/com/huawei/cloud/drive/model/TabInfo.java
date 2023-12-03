/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.cloud.drive.model;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Tab Info
 */
public class TabInfo {
    public Class<?> loadedClass;
    public Bundle args;
    public Fragment fragment;
    public String Name;
    public TabInfo(Class<?> _Class, Bundle _args, String Name) {
        loadedClass = _Class;
        args = _args;
        this.Name = Name;
    }
    public String getName(){
        return Name;
    }
    /**
     * Get fragment
     */
    public Fragment getFragment() {
        return fragment;
    }
}
