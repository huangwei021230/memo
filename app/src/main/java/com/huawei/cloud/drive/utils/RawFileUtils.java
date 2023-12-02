package com.huawei.cloud.drive.utils;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RawFileUtils {
    public static File copyRawFileToTempFile(Context context, int resId, String fileName) throws IOException {
        File tempFile = createTempFile(context, fileName);
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Resources resources = context.getResources();
            inputStream = resources.openRawResource(resId);
            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return tempFile;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static File createTempFile(Context context, String fileName) throws IOException {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return new File(cacheDir, fileName);
    }
}