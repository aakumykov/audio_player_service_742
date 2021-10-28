package com.gitlab.aakumykov.aps_demo;

import android.view.View;

import androidx.annotation.Nullable;

public class ViewUtils {

    public static void show(@Nullable View view) {
        if (null != view)
            view.setVisibility(View.VISIBLE);
    }

    public static void hide(@Nullable View view) {
        if (null != view)
            view.setVisibility(View.GONE);
    }

    public static void enable(@Nullable View view) {
        if (null != view)
            view.setEnabled(true);
    }

    public static void disable(@Nullable View view) {
        if (null != view)
            view.setEnabled(false);
    }
}
