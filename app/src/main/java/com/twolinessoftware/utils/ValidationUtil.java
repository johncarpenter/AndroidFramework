package com.twolinessoftware.utils;

/**
 * Created by johncarpenter on 2015-12-08.
 */
public class ValidationUtil {

    public final static boolean isValidEmail(CharSequence target) {
        if ( target == null )
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
