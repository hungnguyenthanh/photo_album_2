package com.framgia.photoalbum.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.util.Log;

import com.framgia.photoalbum.BuildConfig;

import java.io.IOException;
import java.util.List;

/**
 * Created by HungNT on 4/27/16.
 */
public class CommonUtils {

    private static final String TAG = "CommonUtils";

    /**
     * Check whether camera not available
     *
     * @param ctx
     * @param intent
     * @return
     */
    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * get screen size
     *
     * @param activity
     * @return screen size
     */
    public static Point getDisplaySize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new Point(metrics.widthPixels, metrics.heightPixels);
    }

    /**
     * get inSampleSize to match bitmap to image
     *
     * @param width
     * @param height
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (reqHeight < height || reqWidth < width) {
            final int haftHeight = height / 2;
            final int haftWidth = width / 2;
            while ((haftHeight / inSampleSize) >= reqHeight && (haftWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * decode image file with calculated inSampleSize
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapResource(String path, int reqWidth, int reqHeight) {
        if (path == null) return null;
        Log.d(TAG, path);
        Bitmap photoBitmap = null;
        Bitmap rotatedBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    options.inSampleSize = calculateInSampleSize(options.outHeight, options.outWidth, reqWidth, reqHeight);
                    photoBitmap = BitmapFactory.decodeFile(path, options);
                    rotatedBitmap = CommonUtils.rotateImage(photoBitmap, 90);
                    photoBitmap.recycle();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
                    photoBitmap = BitmapFactory.decodeFile(path, options);
                    rotatedBitmap = CommonUtils.rotateImage(photoBitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    options.inSampleSize = calculateInSampleSize(options.outHeight, options.outWidth, reqWidth, reqHeight);
                    photoBitmap = BitmapFactory.decodeFile(path, options);
                    rotatedBitmap = CommonUtils.rotateImage(photoBitmap, 270);
                    break;
                default:
                    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
                    rotatedBitmap = BitmapFactory.decodeFile(path, options);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "" + photoBitmap);
            Log.w(TAG, "" + options.inSampleSize);
        }
        return rotatedBitmap;
    }

    /**
     * rotate image depend on image's orientation
     *
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Change contrast and brightness of bitmap
     * @param bmp source bitmap
     * @param target target bitmap
     * @param contrast  contrast parameter
     * @param brightness brightness parameter
     * @return target bitmap
     */
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, Bitmap target, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });


        Canvas canvas = new Canvas(target);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return target;
    }

}
