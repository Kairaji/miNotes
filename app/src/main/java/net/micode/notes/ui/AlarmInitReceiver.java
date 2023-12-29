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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;


public class AlarmInitReceiver extends BroadcastReceiver {

    private static final String [] PROJECTION = new String [] {
            NoteColumns.ID, // 备忘录ID列
            NoteColumns.ALERTED_DATE // 提醒日期列
    };

    private static final int COLUMN_ID                = 0; // 备忘录ID列的索引
    private static final int COLUMN_ALERTED_DATE      = 1; // 提醒日期列的索引

    @Override
    public void onReceive(Context context, Intent intent) {
        long currentDate = System.currentTimeMillis(); // 获取当前时间戳
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI, // 查询备忘录内容提供器中的数据
                PROJECTION, // 查询的列
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE, // 查询条件：提醒日期大于当前时间且类型为普通备忘录
                new String[] { String.valueOf(currentDate) }, // 查询条件参数：当前时间
                null); // 排序方式

        if (c != null) {
            if (c.moveToFirst()) { // 如果查询结果不为空，将游标移到第一行
                do {
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE); // 获取提醒日期
                    Intent sender = new Intent(context, AlarmReceiver.class); // 创建发送广播的Intent对象
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID))); // 设置Intent的数据为备忘录的URI
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0); // 创建待定意图用于发送广播
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); // 获取闹钟管理器
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent); // 设置闹钟，使其在提醒日期触发时发送广播
                } while (c.moveToNext()); // 移动游标到下一行继续循环
            }
            c.close(); // 关闭游标
        }
    }
}

