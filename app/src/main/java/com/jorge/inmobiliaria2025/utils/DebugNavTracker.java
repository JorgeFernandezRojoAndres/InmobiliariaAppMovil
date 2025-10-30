package com.jorge.inmobiliaria2025.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

public class DebugNavTracker {

    private static final String TAG = "NAV_DEBUG";

    public static void logFragment(Fragment f, String point) {
        Log.d(TAG, "📍 " + point +
                " | Fragment=" + f.getClass().getSimpleName() +
                " | hash=" + f.hashCode());
    }

    public static void logViewModel(ViewModel vm, String point) {
        Log.d(TAG, "🧠 VM " + point +
                " | " + vm.getClass().getSimpleName() +
                " | hash=" + vm.hashCode());
    }

    public static void logNavController(NavController nc, String point) {
        Log.d(TAG, "🧭 Nav " + point +
                " | hash=" + nc.hashCode() +
                " | current=" + (nc.getCurrentDestination() != null ?
                nc.getCurrentDestination().getLabel() : "null"));
    }
}
