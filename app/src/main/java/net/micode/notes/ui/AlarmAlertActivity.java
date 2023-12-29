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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;

import java.io.IOException;


public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    private long mNoteId; // 存储备忘录ID的变量
    private String mSnippet; // 存储备忘录摘要的变量
    private static final int SNIPPET_PREW_MAX_LEN = 60; // 备忘录摘要预览的最大长度
    MediaPlayer mPlayer; // 用于播放闹钟声音的媒体播放器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 请求在活动中不显示标题栏

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); // 在锁屏时显示活动

        if (!isScreenOn()) { // 检查屏幕是否关闭
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR); // 保持屏幕常亮、点亮屏幕、在锁屏时允许交互，并布局插图
        }

        Intent intent = getIntent();

        try {
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1)); // 从意图数据中检索备忘录ID
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId); // 根据ID获取备忘录摘要
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? mSnippet.substring(0,
                    SNIPPET_PREW_MAX_LEN) + getResources().getString(R.string.notelist_string_info)
                    : mSnippet; // 如果摘要超过最大长度，则截断，并附加额外信息
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        mPlayer = new MediaPlayer(); // 初始化媒体播放器
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog(); // 显示操作对话框
            playAlarmSound(); // 播放闹钟声音
        } else {
            finish(); // 如果备忘录在数据库中不可见，则结束活动
        }
    }


    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    private void playAlarmSound() {
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 如果静音模式对闹钟流有影响，则设置媒体播放器的音频流类型为静音模式流
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams);
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // 否则，设置媒体播放器的音频流类型为闹钟流
        }
        try {
            mPlayer.setDataSource(this, url); // 设置媒体播放器的数据源为闹钟铃声的URI
            mPlayer.prepare(); // 准备媒体播放器
            mPlayer.setLooping(true); // 设置循环播放
            mPlayer.start(); // 开始播放铃声
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void showActionDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.app_name); // 设置对话框标题为应用名称
        dialog.setMessage(mSnippet); // 设置对话框消息为备忘录摘要
        dialog.setPositiveButton(R.string.notealert_ok, this); // 设置对话框的“确定”按钮，并将点击事件交给当前活动处理
        if (isScreenOn()) { // 如果屏幕是点亮状态
            dialog.setNegativeButton(R.string.notealert_enter, this); // 设置对话框的“查看”按钮，并将点击事件交给当前活动处理
        }
        dialog.show().setOnDismissListener(this); // 显示对话框，并设置对话框消失时的监听器为当前活动
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_NEGATIVE: // 如果点击了对话框的“查看”按钮
                Intent intent = new Intent(this, NoteEditActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Intent.EXTRA_UID, mNoteId); // 将备忘录ID作为额外数据传递给备忘录编辑活动
                startActivity(intent); // 启动备忘录编辑活动
                break;
            default:
                break;
        }
    }


    public void onDismiss(DialogInterface dialog) {
        stopAlarmSound(); // 停止播放闹钟铃声
        finish(); // 销毁当前活动
    }

    private void stopAlarmSound() {
        if (mPlayer != null) { // 如果媒体播放器不为空
            mPlayer.stop(); // 停止播放铃声
            mPlayer.release(); // 释放媒体播放器的资源
            mPlayer = null; // 将媒体播放器引用置为空
        }
    }
}

