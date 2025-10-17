package com.jorge.inmobiliaria2025.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * ✅ FileUtils actualizado
 * Compatible con Android 5 → 14 (Scoped Storage incluido).
 */
public class FileUtils {

    public static String getPathFromUri(Context context, Uri uri) {
        try {
            // 🔹 Android 9 o menor
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(columnIndex);
                    cursor.close();
                    return path;
                }
                return null;
            }

            // 🔹 Android 10+ (Scoped Storage)
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(context.getCacheDir(), System.currentTimeMillis() + "_temp.jpg");
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // ✅ CORRECTO: escribir desde 0 hasta bytesRead
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            inputStream.close();
            Log.d("FileUtils", "📂 Archivo temporal creado: " + tempFile.getAbsolutePath());
            return tempFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("FileUtils", "❌ Error obteniendo ruta desde Uri: " + e.getMessage());
            return null;
        }
    }
}
