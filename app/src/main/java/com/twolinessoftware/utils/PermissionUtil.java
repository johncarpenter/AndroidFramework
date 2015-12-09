package com.twolinessoftware.utils;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 *
 */
public class PermissionUtil {

    public static boolean hasPermission(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity,
                permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission){
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }

    public static boolean requestPermission(Activity activity, String permission, int callbackReqId){
        if(!hasPermission(activity,permission)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    callbackReqId);
            return false;
        }
        return true;
    }


}
