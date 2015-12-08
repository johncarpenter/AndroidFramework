package com.twolinessoftware.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.twolinessoftware.BaseApplication;

public class DeviceUtils {
	private static final String TAG=DeviceUtils.class.getSimpleName();
	private static final boolean LOG= BaseApplication.LOG;

	public static String getPhoneNumber() {
		return ((TelephonyManager) BaseApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
	}

	public static void placeCall(String phoneNum) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BaseApplication.getAppContext().startActivity(intent);
	}

	public static void sendEmail(String mailto, String subject, String body) {
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri data = Uri.parse("mailto:" + mailto + "?subject=" + subject + "&body=" + body);
		intent.setData(data);
		BaseApplication.getAppContext().startActivity(intent);
	}


}
