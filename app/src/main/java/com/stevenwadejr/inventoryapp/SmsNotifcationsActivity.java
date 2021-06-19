package com.stevenwadejr.inventoryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SmsNotifcationsActivity extends AppCompatActivity {

	// Logcat tag
	private static final String TAG = "SmsNotificationsActivity";

	public static String PREFERENCE_RECEIVE_NOTIFICATIONS = "pref_receive_notifications";

	private final int REQUEST_SEND_SMS_CODE = 0;

	SwitchMaterial notificationsToggle;

	SharedPreferences sharedPrefs;

	boolean receiveNotifications = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		setContentView(R.layout.activity_sms_notifcations);

		notificationsToggle = findViewById(R.id.notificationsToggle);
		notificationsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				receiveNotifications = isChecked;
				if (isChecked && hasPermissions()) {
					Log.d(TAG, "Wants to receive notifications");
					notificationsToggle.setChecked(true);
				} else {
					Log.d(TAG, "Does not want to receive notifications");
					notificationsToggle.setChecked(false);
					receiveNotifications = false;
				}

				savePreferences();
			}
		});

		// Access the default shared prefs
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		receiveNotifications = sharedPrefs.getBoolean(PREFERENCE_RECEIVE_NOTIFICATIONS, false);

		if (receiveNotifications
				&& ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
				== PackageManager.PERMISSION_GRANTED
		) {
			notificationsToggle.setChecked(true);
		}
	}

	private boolean hasPermissions() {
		String smsPermission = Manifest.permission.SEND_SMS;
		if (ContextCompat.checkSelfPermission(this, smsPermission)
				!= PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(this, smsPermission)) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.sms_notification_dialog_title)
						.setMessage(R.string.sms_notification_justification)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityCompat.requestPermissions(
										SmsNotifcationsActivity.this,
										new String[]{smsPermission},
										REQUEST_SEND_SMS_CODE
								);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create()
						.show();
			} else {
				ActivityCompat.requestPermissions(
						this,
						new String[]{smsPermission},
						REQUEST_SEND_SMS_CODE
				);
			}
			return false;
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_SEND_SMS_CODE: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted!
					Log.d(TAG, "Permission granted");
					receiveNotifications = true;
					notificationsToggle.setChecked(true);
				} else {
					// Permission denied!
					Log.d(TAG, "Permission denied");
					receiveNotifications = false;
					notificationsToggle.setChecked(false);
				}
				savePreferences();
				return;
			}
		}
	}

	private void savePreferences() {
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(PREFERENCE_RECEIVE_NOTIFICATIONS, receiveNotifications);
		editor.commit();
	}
}
