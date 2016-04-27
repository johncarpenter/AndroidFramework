package com.twolinessoftware;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import timber.log.Timber;

/**
 *
 */
public class ScreenshotUtil {

    public static void take(Activity activity, String name) {
        final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test-screenshots/";
        final String path = dir + name + ".png";

        File filePath = new File(dir);     // Create directory if not present
        if ( !filePath.isDirectory() ) {
            Timber.v("Creating directory " + filePath);
            filePath.mkdirs();
        }

        Timber.v("Saving to path: " + path);

        /*View phoneView = activity.getWindow().getDecorView().getRootView();
        phoneView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(phoneView.getDrawingCache());
        phoneView.setDrawingCacheEnabled(false);
*/

        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();

        OutputStream out = null;

        File imageFile = new File(path);

        try {
            out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (FileNotFoundException e) {
            Timber.v(e.toString());
        } catch (IOException e) {
            Timber.v(e.toString());
        } finally {
            try {
                if ( out != null ) {
                    out.close();
                }
            } catch (IOException e) {
                Timber.v(e.toString());
            }
        }
    }
}