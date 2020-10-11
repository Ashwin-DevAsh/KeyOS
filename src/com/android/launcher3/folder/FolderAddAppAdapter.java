/*
 *     Copyright (C) 2020 Kiosk Team.
 *
 *     This file is part of Kiosk Launcher.
 *
 *     Kiosk Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Kiosk Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Kiosk Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.android.launcher3.folder;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

public class FolderAddAppAdapter extends ArrayAdapter<FolderAddAppItem> {

    Context context;
    int layoutResourceId;
    List<FolderAddAppItem> data;
    public boolean showHighlight = false;
    IconCache iconCache;


    public FolderAddAppAdapter(Context context, int layoutResourceId, List<FolderAddAppItem> data, IconCache iconCache) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.iconCache = iconCache;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AppInfoHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new AppInfoHolder();
            holder.icon = row.findViewById(R.id.folder_add_app_icon);
            holder.name = row.findViewById(R.id.folder_add_app_name);
            holder.pkg = row.findViewById(R.id.folder_add_app_package_name);
            holder.cb = row.findViewById(R.id.folder_add_app_checkbox);
            row.setTag(holder);
        } else {
            holder = (AppInfoHolder) row.getTag();
        }

        FolderAddAppItem app = data.get(position);
        ShortcutInfo info = app.shortcutInfo;
        holder.name.setText(info.title);
        holder.pkg.setText(info.getPackageName());
        iconCache.getTitleAndIcon(info, false);
        holder.icon.setImageBitmap(info.iconBitmap);
        holder.cb.setChecked(app.checked);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderAddAppItem app = data.get(position);
                app.checked = !app.checked;
                notifyDataSetChanged();
            }
        });
        return row;

    }

    static class AppInfoHolder {
        ImageView icon;
        TextView name;
        TextView pkg;
        CheckBox cb;
    }

    public ArrayList<ShortcutInfo> getCheckedItems() {
        ArrayList<ShortcutInfo> result = new ArrayList<>();
        for (FolderAddAppItem item : data) {
            if (item.checked) {
                result.add(item.shortcutInfo);
            }
        }
        return result;
    }

}
