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

import java.util.Calendar;

import net.micode.notes.R;
import net.micode.notes.ui.DateTimePicker;
import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

public class DateTimePickerDialog extends AlertDialog implements OnClickListener {

    private Calendar mDate = Calendar.getInstance(); // 创建一个日期对象，用于存储选择的日期时间
    private boolean mIs24HourView; // 是否是24小时制
    private OnDateTimeSetListener mOnDateTimeSetListener; // 时间设置监听器
    private DateTimePicker mDateTimePicker; // 日期和时间选择器

    public interface OnDateTimeSetListener { // 时间设置监听器接口
        void OnDateTimeSet(AlertDialog dialog, long date);
    }

    public DateTimePickerDialog(Context context, long date) {
        super(context);
        mDateTimePicker = new DateTimePicker(context); // 创建一个日期和时间选择器对象
        setView(mDateTimePicker); // 将日期和时间选择器设置为对话框的视图
        mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() { // 设置日期和时间选择器的监听器
            public void onDateTimeChanged(DateTimePicker view, int year, int month,
                                          int dayOfMonth, int hourOfDay, int minute) {
                mDate.set(Calendar.YEAR, year); // 设置年份
                mDate.set(Calendar.MONTH, month); // 设置月份
                mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth); // 设置日期
                mDate.set(Calendar.HOUR_OF_DAY, hourOfDay); // 设置小时
                mDate.set(Calendar.MINUTE, minute); // 设置分钟
                updateTitle(mDate.getTimeInMillis()); // 更新对话框的标题
            }
        });
        mDate.setTimeInMillis(date); // 设置日期和时间选择器的默认时间
        mDate.set(Calendar.SECOND, 0); // 将秒数设置为0
        mDateTimePicker.setCurrentDate(mDate.getTimeInMillis()); // 设置日期和时间选择器的当前时间
        setButton(context.getString(R.string.datetime_dialog_ok), this); // 设置对话框的确定按钮
        setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener)null); // 设置对话框的取消按钮
        set24HourView(DateFormat.is24HourFormat(this.getContext())); // 设置是否是24小时制
        updateTitle(mDate.getTimeInMillis()); // 更新对话框的标题
    }

    public void set24HourView(boolean is24HourView) { // 设置是否是24小时制
        mIs24HourView = is24HourView;
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) { // 设置时间设置监听器
        mOnDateTimeSetListener = callBack;
    }

    private void updateTitle(long date) { // 更新对话框的标题
        int flag =
                DateUtils.FORMAT_SHOW_YEAR |
                        DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_TIME;
        flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_24HOUR;
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    public void onClick(DialogInterface arg0, int arg1) { // 处理点击确定按钮事件
        if (mOnDateTimeSetListener != null) {
            mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis()); // 调用时间设置监听器的回调方法
        }
    }

}
