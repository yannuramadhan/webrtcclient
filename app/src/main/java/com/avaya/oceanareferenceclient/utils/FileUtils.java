package com.avaya.oceanareferenceclient.utils;

import android.content.Context;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;

public class FileUtils {
    public static String getExternalStoragePathFile(Context appContext/**/){
        File[] externalStorageVolumes =
                ContextCompat.getExternalFilesDirs(appContext, null);
        File primaryExternalStorage = externalStorageVolumes[0];/**/
        return primaryExternalStorage.getAbsolutePath();
    }
    // Checks if a volume containing external storage is available
    // for read and write.
    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }
    // Checks if a volume containing external storage is available to at least read.
    public static boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY;
    }
}
