/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟接收器，用于接收系统的闹钟广播并启动闹钟提醒页面。
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将启动的Activity设置为AlarmAlertActivity
        intent.setClass(context, AlarmAlertActivity.class);
        // 添加一个FLAG，表示要在新的任务栈中启动Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动Activity
        context.startActivity(intent);
    }
}

