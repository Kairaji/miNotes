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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import net.micode.notes.R;


import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

public class DateTimePicker extends FrameLayout {

    private static final boolean DEFAULT_ENABLE_STATE = true;

    private static final int HOURS_IN_HALF_DAY = 12;
    private static final int HOURS_IN_ALL_DAY = 24;
    private static final int DAYS_IN_ALL_WEEK = 7;
    private static final int DATE_SPINNER_MIN_VAL = 0;
    private static final int DATE_SPINNER_MAX_VAL = DAYS_IN_ALL_WEEK - 1;
    private static final int HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW = 0;
    private static final int HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW = 23;
    private static final int HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW = 1;
    private static final int HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW = 12;
    private static final int MINUT_SPINNER_MIN_VAL = 0;
    private static final int MINUT_SPINNER_MAX_VAL = 59;
    private static final int AMPM_SPINNER_MIN_VAL = 0;
    private static final int AMPM_SPINNER_MAX_VAL = 1;

    private final NumberPicker mDateSpinner;
    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mAmPmSpinner;
    private Calendar mDate;

    private String[] mDateDisplayValues = new String[DAYS_IN_ALL_WEEK];

    private boolean mIsAm;

    private boolean mIs24HourView;

    private boolean mIsEnabled = DEFAULT_ENABLE_STATE;

    private boolean mInitialising;

    private OnDateTimeChangedListener mOnDateTimeChangedListener;

    private NumberPicker.OnValueChangeListener mOnDateChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mDate.add(Calendar.DAY_OF_YEAR, newVal - oldVal);
            updateDateControl();
            onDateTimeChanged();
        }
    };

    private NumberPicker.OnValueChangeListener mOnHourChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            boolean isDateChanged = false;
            Calendar cal = Calendar.getInstance();
            if (!mIs24HourView) {
                if (!mIsAm && oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    isDateChanged = true;
                } else if (mIsAm && oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    isDateChanged = true;
                }
                if (oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY ||
                        oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1) {
                    mIsAm = !mIsAm;
                    updateAmPmControl();
                }
            } else {
                if (oldVal == HOURS_IN_ALL_DAY - 1 && newVal == 0) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    isDateChanged = true;
                } else if (oldVal == 0 && newVal == HOURS_IN_ALL_DAY - 1) {
                    cal.setTimeInMillis(mDate.getTimeInMillis());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    isDateChanged = true;
                }
            }
            int newHour = mHourSpinner.getValue() % HOURS_IN_HALF_DAY + (mIsAm ? 0 : HOURS_IN_HALF_DAY);
            mDate.set(Calendar.HOUR_OF_DAY, newHour);
            onDateTimeChanged();
            if (isDateChanged) {
                setCurrentYear(cal.get(Calendar.YEAR));
                setCurrentMonth(cal.get(Calendar.MONTH));
                setCurrentDay(cal.get(Calendar.DAY_OF_MONTH));
            }
        }
    };

    private NumberPicker.OnValueChangeListener mOnMinuteChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            int minValue = mMinuteSpinner.getMinValue(); // 获取分钟选择器的最小值
            int maxValue = mMinuteSpinner.getMaxValue(); // 获取分钟选择器的最大值
            int offset = 0; // 偏移量

            if (oldVal == maxValue && newVal == minValue) { // 如果从最大值切换到最小值
                offset += 1; // 偏移量增加1
            } else if (oldVal == minValue && newVal == maxValue) { // 如果从最小值切换到最大值
                offset -= 1; // 偏移量减少1
            }

            if (offset != 0) { // 如果偏移量不为0
                mDate.add(Calendar.HOUR_OF_DAY, offset); // 将日期向前或向后调整一个小时
                mHourSpinner.setValue(getCurrentHour()); // 更新小时选择器的值
                updateDateControl(); // 更新日期控件
                int newHour = getCurrentHourOfDay(); // 获取当前小时

                if (newHour >= HOURS_IN_HALF_DAY) { // 如果当前小时大于等于12
                    mIsAm = false; // 设置为下午
                    updateAmPmControl(); // 更新上午下午控件
                } else {
                    mIsAm = true; // 设置为上午
                    updateAmPmControl(); // 更新上午下午控件
                }
            }

            mDate.set(Calendar.MINUTE, newVal); // 将日期的分钟字段设置为新选择的值
            onDateTimeChanged(); // 日期时间改变时的回调方法
        }
    };


    private NumberPicker.OnValueChangeListener mOnAmPmChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mIsAm = !mIsAm; // 切换上午下午标志

            if (mIsAm) { // 如果是上午
                mDate.add(Calendar.HOUR_OF_DAY, -HOURS_IN_HALF_DAY); // 将日期向前调整12小时
            } else { // 如果是下午
                mDate.add(Calendar.HOUR_OF_DAY, HOURS_IN_HALF_DAY); // 将日期向后调整12小时
            }

            updateAmPmControl(); // 更新上午下午控件
            onDateTimeChanged(); // 日期时间改变时的回调方法
        }
    };


    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month,
                               int dayOfMonth, int hourOfDay, int minute);
    }

    /**
     * 构造方法，使用当前系统时间初始化 DateTimePicker
     */
    public DateTimePicker(Context context) {
        this(context, System.currentTimeMillis());
    }

    /**
     * 构造方法，使用指定时间戳初始化 DateTimePicker
     */
    public DateTimePicker(Context context, long date) {
        this(context, date, DateFormat.is24HourFormat(context));
    }

    /**
     * 构造方法，使用指定时间戳和是否为24小时制初始化 DateTimePicker
     */
    public DateTimePicker(Context context, long date, boolean is24HourView) {
        super(context);

        mDate = Calendar.getInstance(); // 初始化日期对象
        mInitialising = true;
        mIsAm = getCurrentHourOfDay() >= HOURS_IN_HALF_DAY; // 判断当前时间是上午还是下午
        inflate(context, R.layout.datetime_picker, this); // 填充布局

        mDateSpinner = (NumberPicker) findViewById(R.id.date); // 获取日期选择器控件
        mDateSpinner.setMinValue(DATE_SPINNER_MIN_VAL); // 设置日期选择器的最小值
        mDateSpinner.setMaxValue(DATE_SPINNER_MAX_VAL); // 设置日期选择器的最大值
        mDateSpinner.setOnValueChangedListener(mOnDateChangedListener); // 设置日期选择器值变化的监听器

        mHourSpinner = (NumberPicker) findViewById(R.id.hour); // 获取小时选择器控件
        mHourSpinner.setOnValueChangedListener(mOnHourChangedListener); // 设置小时选择器值变化的监听器
        mMinuteSpinner = (NumberPicker) findViewById(R.id.minute); // 获取分钟选择器控件
        mMinuteSpinner.setMinValue(MINUT_SPINNER_MIN_VAL); // 设置分钟选择器的最小值
        mMinuteSpinner.setMaxValue(MINUT_SPINNER_MAX_VAL); // 设置分钟选择器的最大值
        mMinuteSpinner.setOnLongPressUpdateInterval(100); // 设置长按更新间隔时间
        mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener); // 设置分钟选择器值变化的监听器

        String[] stringsForAmPm = new DateFormatSymbols().getAmPmStrings(); // 获取上午下午字符串数组
        mAmPmSpinner = (NumberPicker) findViewById(R.id.amPm); // 获取上午下午选择器控件
        mAmPmSpinner.setMinValue(AMPM_SPINNER_MIN_VAL); // 设置上午下午选择器的最小值
        mAmPmSpinner.setMaxValue(AMPM_SPINNER_MAX_VAL); // 设置上午下午选择器的最大值
        mAmPmSpinner.setDisplayedValues(stringsForAmPm); // 设置上午下午选择器的显示值
        mAmPmSpinner.setOnValueChangedListener(mOnAmPmChangedListener); // 设置上午下午选择器值变化的监听器

        // 更新控件的初始状态
        updateDateControl();
        updateHourControl();
        updateAmPmControl();

        set24HourView(is24HourView); // 设置是否为24小时制

        setCurrentDate(date); // 设置当前日期

        setEnabled(isEnabled()); // 设置是否可用

        // 设置内容描述
        mInitialising = false;
    }


    @Override
    /**
     * 设置 DateTimePicker 是否可用
     * @param enabled true 为可用，false 为不可用
     */
    public void setEnabled(boolean enabled) {
        if (mIsEnabled == enabled) { // 如果设置的值和当前值相同，则不做处理
            return;
        }
        super.setEnabled(enabled); // 设置父类的可用性状态
        mDateSpinner.setEnabled(enabled); // 设置日期选择器的可用性状态
        mMinuteSpinner.setEnabled(enabled); // 设置分钟选择器的可用性状态
        mHourSpinner.setEnabled(enabled); // 设置小时选择器的可用性状态
        mAmPmSpinner.setEnabled(enabled); // 设置上午下午选择器的可用性状态
        mIsEnabled = enabled; // 更新可用性状态
    }


    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }

    /**
     * Get the current date in millis
     *
     * @return the current date in millis
     */
    public long getCurrentDateInTimeMillis() {
        return mDate.getTimeInMillis();
    }

    /**
     * Set the current date
     *
     * @param date The current date in millis
     */
    public void setCurrentDate(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        setCurrentDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    /**
     * Set the current date
     *
     * @param year The current year
     * @param month The current month
     * @param dayOfMonth The current dayOfMonth
     * @param hourOfDay The current hourOfDay
     * @param minute The current minute
     */
    /**
     * 设置当前日期和时间
     *
     * @param year       年份
     * @param month      月份（1-12）
     * @param dayOfMonth 日期（1-31）
     * @param hourOfDay  小时（0-23）
     * @param minute     分钟（0-59）
     */
    public void setCurrentDate(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        setCurrentYear(year); // 设置当前年份
        setCurrentMonth(month); // 设置当前月份
        setCurrentDay(dayOfMonth); // 设置当前日期
        setCurrentHour(hourOfDay); // 设置当前小时
        setCurrentMinute(minute); // 设置当前分钟
    }


    /**
     * Get current year
     *
     * @return The current year
     */
    public int getCurrentYear() {
        return mDate.get(Calendar.YEAR);
    }

    /**
     * Set current year
     *
     * @param year The current year
     */
    public void setCurrentYear(int year) {
        if (!mInitialising && year == getCurrentYear()) {
            return;
        }
        mDate.set(Calendar.YEAR, year);
        updateDateControl();
        onDateTimeChanged();
    }

    /**
     * Get current month in the year
     *
     * @return The current month in the year
     */
    public int getCurrentMonth() {
        return mDate.get(Calendar.MONTH);
    }

    /**
     * Set current month in the year
     *
     * @param month The month in the year
     */
    public void setCurrentMonth(int month) {
        if (!mInitialising && month == getCurrentMonth()) {
            return;
        }
        mDate.set(Calendar.MONTH, month);
        updateDateControl();
        onDateTimeChanged();
    }

    /**
     * Get current day of the month
     *
     * @return The day of the month
     */
    public int getCurrentDay() {
        return mDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Set current day of the month
     *
     * @param dayOfMonth The day of the month
     */
    public void setCurrentDay(int dayOfMonth) {
        if (!mInitialising && dayOfMonth == getCurrentDay()) {
            return;
        }
        mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateControl();
        onDateTimeChanged();
    }

    /**
     * Get current hour in 24 hour mode, in the range (0~23)
     *
     * @return The current hour in 24 hour mode
     */
    public int getCurrentHourOfDay() {
        return mDate.get(Calendar.HOUR_OF_DAY);
    }

    private int getCurrentHour() {
        if (mIs24HourView) {
            return getCurrentHourOfDay();
        } else {
            int hour = getCurrentHourOfDay();
            if (hour > HOURS_IN_HALF_DAY) {
                return hour - HOURS_IN_HALF_DAY;
            } else {
                return hour == 0 ? HOURS_IN_HALF_DAY : hour;
            }
        }
    }

    /**
     * Set current hour in 24 hour mode, in the range (0~23)
     *
     * @param hourOfDay
     */
    /**
     * 设置当前小时
     *
     * @param hourOfDay 小时（0-23）
     */
    public void setCurrentHour(int hourOfDay) {
        if (!mInitialising && hourOfDay == getCurrentHourOfDay()) { // 如果不是初始化状态并且设置的小时与当前小时相同，则不做处理
            return;
        }
        mDate.set(Calendar.HOUR_OF_DAY, hourOfDay); // 设置日历对象的小时字段

        if (!mIs24HourView) { // 如果不是24小时制
            if (hourOfDay >= HOURS_IN_HALF_DAY) { // 如果大于等于12小时
                mIsAm = false; // 设置为下午
                if (hourOfDay > HOURS_IN_HALF_DAY) {
                    hourOfDay -= HOURS_IN_HALF_DAY; // 转换为下午的小时表示
                }
            } else { // 如果小于12小时
                mIsAm = true; // 设置为上午
                if (hourOfDay == 0) {
                    hourOfDay = HOURS_IN_HALF_DAY; // 转换为上午的小时表示
                }
            }
            updateAmPmControl(); // 更新上午下午控件
        }

        mHourSpinner.setValue(hourOfDay); // 设置小时选择器的值
        onDateTimeChanged(); // 触发日期时间变化事件
    }


    /**
     * Get currentMinute
     *
     * @return The Current Minute
     */
    /**
     * 获取当前分钟
     *
     * @return 当前分钟数
     */
    public int getCurrentMinute() {
        return mDate.get(Calendar.MINUTE); // 返回日历对象的分钟字段
    }

    /**
     * 设置当前分钟
     *
     * @param minute 分钟（0-59）
     */
    public void setCurrentMinute(int minute) {
        if (!mInitialising && minute == getCurrentMinute()) { // 如果不是初始化状态并且设置的分钟与当前分钟相同，则不做处理
            return;
        }
        mMinuteSpinner.setValue(minute); // 设置分钟选择器的值
        mDate.set(Calendar.MINUTE, minute); // 设置日历对象的分钟字段
        onDateTimeChanged(); // 触发日期时间变化事件
    }


    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True for 24 hour mode. False for AM/PM mode.
     */
    /**
     * 设置是否为24小时制
     *
     * @param is24HourView 是否为24小时制
     */
    public void set24HourView(boolean is24HourView) {
        if (mIs24HourView == is24HourView) { // 如果当前是否为24小时制与目标值相同，则不做处理
            return;
        }
        mIs24HourView = is24HourView; // 更新是否为24小时制的标志位
        mAmPmSpinner.setVisibility(is24HourView ? View.GONE : View.VISIBLE); // 根据是否为24小时制设置上午下午选择器的可见性
        int hour = getCurrentHourOfDay(); // 获取当前小时
        updateHourControl(); // 更新小时控件
        setCurrentHour(hour); // 设置当前小时
        updateAmPmControl(); // 更新上午下午控件
    }


    /**
     * 更新日期控件
     */
    private void updateDateControl() {
        Calendar cal = Calendar.getInstance(); // 获取当前时间的日历对象
        cal.setTimeInMillis(mDate.getTimeInMillis()); // 设置日历对象的时间为选择的时间
        cal.add(Calendar.DAY_OF_YEAR, -DAYS_IN_ALL_WEEK / 2 - 1); // 将日期设置为当前日期往前推半周减一天
        mDateSpinner.setDisplayedValues(null); // 清空日期选择器的显示值
        for (int i = 0; i < DAYS_IN_ALL_WEEK; ++i) { // 循环更新日期显示值
            cal.add(Calendar.DAY_OF_YEAR, 1); // 将日期向后推一天
            mDateDisplayValues[i] = (String) DateFormat.format("MM.dd EEEE", cal); // 格式化日期并保存到日期显示值数组中
        }
        mDateSpinner.setDisplayedValues(mDateDisplayValues); // 设置日期选择器的显示值
        mDateSpinner.setValue(DAYS_IN_ALL_WEEK / 2); // 设置日期选择器的当前值为一周的中间位置
        mDateSpinner.invalidate(); // 刷新日期选择器
    }

    /**
     * 更新上午下午控件
     */
    private void updateAmPmControl() {
        if (mIs24HourView) { // 如果是24小时制
            mAmPmSpinner.setVisibility(View.GONE); // 上午下午选择器不可见
        } else { // 如果不是24小时制
            int index = mIsAm ? Calendar.AM : Calendar.PM; // 根据是否为上午设置上午下午选择器的值
            mAmPmSpinner.setValue(index); // 设置上午下午选择器的值
            mAmPmSpinner.setVisibility(View.VISIBLE); // 上午下午选择器可见
        }
    }


    /**
     * 更新小时控件
     */
    private void updateHourControl() {
        if (mIs24HourView) { // 如果是24小时制
            mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_24_HOUR_VIEW); // 设置小时选择器的最小值为0
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_24_HOUR_VIEW); // 设置小时选择器的最大值为23
        } else { // 如果不是24小时制
            mHourSpinner.setMinValue(HOUR_SPINNER_MIN_VAL_12_HOUR_VIEW); // 设置小时选择器的最小值为1
            mHourSpinner.setMaxValue(HOUR_SPINNER_MAX_VAL_12_HOUR_VIEW); // 设置小时选择器的最大值为12
        }
    }

    /**
     * 设置时间改变监听器
     *
     * @param callback 时间改变监听器，如果为null则不做任何操作
     */
    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback; // 将传入的时间改变监听器赋值给成员变量
    }

    /**
     * 当时间发生改变时调用此方法
     */
    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) { // 如果时间改变监听器不为空
            mOnDateTimeChangedListener.onDateTimeChanged(this, getCurrentYear(), getCurrentMonth(),
                    getCurrentDay(), getCurrentHourOfDay(), getCurrentMinute()); // 调用时间改变监听器的onDateTimeChanged方法，并传入当前选择的年月日时分数据
        }
    }
}

