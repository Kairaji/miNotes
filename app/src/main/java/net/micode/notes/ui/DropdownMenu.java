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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import net.micode.notes.R;

public class DropdownMenu {
    private Button mButton; // 下拉菜单按钮
    private PopupMenu mPopupMenu; // 弹出菜单
    private Menu mMenu; // 菜单项

    public DropdownMenu(Context context, Button button, int menuId) {
        mButton = button;
        mButton.setBackgroundResource(R.drawable.dropdown_icon); // 设置按钮的背景图标为下拉图标
        mPopupMenu = new PopupMenu(context, mButton); // 创建一个弹出菜单对象，关联到按钮
        mMenu = mPopupMenu.getMenu(); // 获取弹出菜单的菜单项
        mPopupMenu.getMenuInflater().inflate(menuId, mMenu); // 从XML资源中加载菜单项
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPopupMenu.show(); // 点击按钮时显示弹出菜单
            }
        });
    }

    public void setOnDropdownMenuItemClickListener(OnMenuItemClickListener listener) { // 设置下拉菜单的菜单项点击监听器
        if (mPopupMenu != null) {
            mPopupMenu.setOnMenuItemClickListener(listener);
        }
    }

    public MenuItem findItem(int id) { // 查找指定id的菜单项
        return mMenu.findItem(id);
    }

    public void setTitle(CharSequence title) { // 设置按钮的标题文本
        mButton.setText(title);
    }
}
