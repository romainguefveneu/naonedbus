/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author Cyril Mottier
 */
public class ViewHelper {
 
    public interface OnTagFoundHandler {
        void onTagFound(View v);
    }
 
    public static final void findViewsByTag(final View root, final String tag, final OnTagFoundHandler handler) {
        if (root == null) {
            throw new NullPointerException();
        }
        if (tag == null || handler == null) {
            return;
        }
        if (tag.equals(root.getTag())) {
            handler.onTagFound(root);
        }
        if (root instanceof ViewGroup) {
            final ViewGroup rootGroup = (ViewGroup) root;
            final int childCount = rootGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                findViewsByTag(rootGroup.getChildAt(i), tag, handler);
            }
        }
    }
 
}
