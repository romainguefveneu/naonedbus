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