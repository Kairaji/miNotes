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

import android.content.Context;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.widget.EditText;

import net.micode.notes.R;

import java.util.HashMap;
import java.util.Map;

public class NoteEditText extends EditText {
    private static final String TAG = "NoteEditText";
    private int mIndex;  // 光标所在位置
    private int mSelectionStartBeforeDelete;  // 删除前的光标位置

    private static final String SCHEME_TEL = "tel:";  // 电话链接
    private static final String SCHEME_HTTP = "http:";  // 网页链接
    private static final String SCHEME_EMAIL = "mailto:";  // 电子邮件链接

    // 不同链接对应的资源映射
    private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();

    static {
        sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);  // 电话链接对应的资源
        sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);  // 网页链接对应的资源
        sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);  // 电子邮件链接对应的资源
    }

    /**
     * 由NoteEditActivity调用，用于删除或添加EditText
     */
    public interface OnTextViewChangeListener {
        /**
         * 当发生KeyEvent.KEYCODE_DEL并且文本为空时，删除当前EditText
         */
        void onEditTextDelete(int index, String text);

        /**
         * 当发生KeyEvent.KEYCODE_ENTER时，在当前EditText后添加EditText
         */
        void onEditTextEnter(int index, String text);

        /**
         * 文本变化时，显示或隐藏项目选项
         */
        void onTextChange(int index, boolean hasText);
    }

    private OnTextViewChangeListener mOnTextViewChangeListener;

    public NoteEditText(Context context) {
        super(context, null);
        mIndex = 0;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
        mOnTextViewChangeListener = listener;
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO 自动生成的构造函数存根
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 处理触摸事件，设置选中文本
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();

                Layout layout = getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                Selection.setSelection(getText(), off);
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (mOnTextViewChangeListener != null) {
                    return false;
                }
                break;
            case KeyEvent.KEYCODE_DEL:
                mSelectionStartBeforeDelete = getSelectionStart();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                if (mOnTextViewChangeListener != null) {
                    // 如果删除到了开头且不是第一个EditText，则通知删除当前EditText
                    if (0 == mSelectionStartBeforeDelete && mIndex != 0) {
                        mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                        return true;
                    }
                } else {
                    Log.d(TAG, "未设置OnTextViewChangeListener");
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
                if (mOnTextViewChangeListener != null) {
                    // 在当前EditText后添加一个EditText
                    int selectionStart = getSelectionStart();
                    String text = getText().subSequence(selectionStart, length()).toString();
                    setText(getText().subSequence(0, selectionStart));
                    mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                } else {
                    Log.d(TAG, "未设置OnTextViewChangeListener");
                }
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (mOnTextViewChangeListener != null) {
            // 当失去焦点且文本为空时，通知文本变化
            if (!focused && TextUtils.isEmpty(getText())) {
                mOnTextViewChangeListener.onTextChange(mIndex, false);
            } else {
                mOnTextViewChangeListener.onTextChange(mIndex, true);
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    /**
     * 创建上下文菜单的方法，用于显示点击链接时的菜单选项
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (getText() instanceof Spanned) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            // 获取选中文本范围内的URLSpan对象数组
            final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
            if (urls.length == 1) {
                int defaultResId = 0;
                for (String schema : sSchemaActionResMap.keySet()) {
                    // 判断URLSpan中的URL是否包含特定的链接类型
                    if (urls[0].getURL().indexOf(schema) >= 0) {
                        defaultResId = sSchemaActionResMap.get(schema);
                        break;
                    }
                }

                if (defaultResId == 0) {
                    defaultResId = R.string.note_link_other;
                }

                // 添加菜单项，并设置点击事件监听器
                menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener(
                        new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                // 执行点击链接后的操作
                                urls[0].onClick(NoteEditText.this);
                                return true;
                            }
                        });
            }
        }
        super.onCreateContextMenu(menu);
    }
}
