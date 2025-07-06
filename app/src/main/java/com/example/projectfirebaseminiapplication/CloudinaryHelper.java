package com.example.projectfirebaseminiapplication;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper extends Application {
    private static boolean initialized = false;

    public static void init(Context context) {
        if (!initialized) {
            Map config = ObjectUtils.asMap(
                    "cloud_name", "ddsiz8xnl",
                    "api_key",    "522912762588929",
                    "api_secret", "lcttD4ck2jVSdswp9YMC-RsKGgM",
                    "secure",     true
            );
            MediaManager.init(context, config);
            initialized = true;
        }
    }
}
