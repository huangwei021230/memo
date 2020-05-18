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

package com.huawei.cloud.drive.task.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manage runnable tasks
 */
public class TaskManager {
    private ExecutorService cached = Executors.newCachedThreadPool();

    private static final TaskManager task = new TaskManager();

    /**
     * Get TaskManager instance
     */
    public static TaskManager getInstance() {
        return task;
    }


    /**
     * Execute runnable task
     */
    public void execute(DriveTask driveTask) {
        Future future = cached.submit(driveTask);
        driveTask.setFuture(future);
    }
}
