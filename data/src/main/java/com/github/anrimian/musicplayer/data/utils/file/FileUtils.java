package com.github.anrimian.musicplayer.data.utils.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static byte[] toByteArray(InputStream is) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[is.available()];
            int i;
            while ((i = is.read(buff, 0, buff.length)) > 0) {
                stream.write(buff, 0, i);
            }
            return stream.toByteArray();
        }
    }

    public static byte[] getScaledBitmapByteArray(InputStream stream, int maxSize) throws IOException {
        byte[] rawBytes = toByteArray(stream);
        Bitmap bitmap = createScaledBitmap(rawBytes, maxSize);
        byte[] scaledBitmapBytes = convertBitmapToByteArray(bitmap);
        bitmap.recycle();
        return scaledBitmapBytes;
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(bitmap.getWidth() * bitmap.getHeight());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
        return buffer.toByteArray();
    }

    public static Bitmap createScaledBitmap(byte[] byteArray, int maxSize) {
        BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
        decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        if (maxSize > 0) {
            BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
            decodeBoundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, decodeBoundsOptions);

            int originalWidth = decodeBoundsOptions.outWidth;
            int originalHeight = decodeBoundsOptions.outHeight;

            decodeBitmapOptions.inSampleSize = Math.max(1, Math.min(originalWidth / maxSize, originalHeight / maxSize));
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, decodeBitmapOptions);
    }

}
