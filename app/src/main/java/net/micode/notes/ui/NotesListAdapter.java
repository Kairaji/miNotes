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
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import net.micode.notes.data.Notes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class NotesListAdapter extends CursorAdapter {
    private static final String TAG = "NotesListAdapter";
    private Context mContext;
    private HashMap<Integer, Boolean> mSelectedIndex; // 用于存储选中状态的 HashMap
    private int mNotesCount;
    private boolean mChoiceMode;

    public static class AppWidgetAttribute {
        public int widgetId;
        public int widgetType;
    }

    ;

    public NotesListAdapter(Context context) {
        super(context, null);
        mSelectedIndex = new HashMap<Integer, Boolean>(); // 初始化选中状态 HashMap
        mContext = context;
        mNotesCount = 0;
    }

    // 创建新的视图
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new NotesListItem(context);
    }

    // 将数据绑定到视图上
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof NotesListItem) {
            NoteItemData itemData = new NoteItemData(context, cursor);
            ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                    isSelectedItem(cursor.getPosition())); // 绑定列表项数据
        }
    }

    // 设置列表项的选中状态
    public void setCheckedItem(final int position, final boolean checked) {
        mSelectedIndex.put(position, checked); // 更新选中状态
        notifyDataSetChanged(); // 刷新列表
    }

    // 判断是否在多选模式下
    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    // 设置多选模式
    public void setChoiceMode(boolean mode) {
        mSelectedIndex.clear(); // 清除选中状态
        mChoiceMode = mode; // 设置多选模式
    }


    public void selectAll(boolean checked) {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                    setCheckedItem(i, checked); // 设置列表项的选中状态
                }
            }
        }
    }

    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<Long>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Long id = getItemId(position); // 获取列表项的ID
                if (id == Notes.ID_ROOT_FOLDER) {
                    Log.d(TAG, "Wrong item id, should not happen");
                } else {
                    itemSet.add(id); // 将选中的列表项ID添加到集合中
                }
            }
        }

        return itemSet;
    }

    public HashSet<AppWidgetAttribute> getSelectedWidget() {
        HashSet<AppWidgetAttribute> itemSet = new HashSet<AppWidgetAttribute>();
        for (Integer position : mSelectedIndex.keySet()) {
            if (mSelectedIndex.get(position) == true) {
                Cursor c = (Cursor) getItem(position); // 获取列表项对应的Cursor
                if (c != null) {
                    AppWidgetAttribute widget = new AppWidgetAttribute();
                    NoteItemData item = new NoteItemData(mContext, c);
                    widget.widgetId = item.getWidgetId(); // 获取小部件的ID
                    widget.widgetType = item.getWidgetType(); // 获取小部件的类型
                    itemSet.add(widget); // 将选中的小部件添加到集合中
                    /**
                     * Don't close cursor here, only the adapter could close it
                     */
                } else {
                    Log.e(TAG, "Invalid cursor");
                    return null;
                }
            }
        }
        return itemSet;
    }


    public int getSelectedCount() {
        Collection<Boolean> values = mSelectedIndex.values(); // 获取所有选中状态的值
        if (null == values) {
            return 0;
        }
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (true == iter.next()) { // 如果值为true，表示选中
                count++; // 计数加一
            }
        }
        return count; // 返回选中的数量
    }

    public boolean isAllSelected() {
        int checkedCount = getSelectedCount(); // 获取选中的数量
        return (checkedCount != 0 && checkedCount == mNotesCount); // 判断是否全部选中
    }

    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false; // 指定位置未被选中
        }
        return mSelectedIndex.get(position); // 返回指定位置的选中状态
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        calcNotesCount(); // 内容改变时重新计算笔记数量
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calcNotesCount(); // 更改Cursor时重新计算笔记数量
    }

    private void calcNotesCount() {
        mNotesCount = 0; // 重置笔记数量
        for (int i = 0; i < getCount(); i++) {
            Cursor c = (Cursor) getItem(i); // 获取列表项对应的Cursor
            if (c != null) {
                if (NoteItemData.getNoteType(c) == Notes.TYPE_NOTE) { // 判断是否为笔记类型
                    mNotesCount++; // 笔记数量加一
                }
            } else {
                Log.e(TAG, "Invalid cursor"); // 记录无效的Cursor日志
                return;
            }
        }
    }
}

