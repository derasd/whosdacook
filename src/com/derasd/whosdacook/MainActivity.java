package com.derasd.whosdacook;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity {

	String calName = "";
	String calType = "";
	String calOwner = "";

	
	private Cursor cur = null;
	public static final String DEBUG_TAG = "com.derasd.whosdacook";

	private static final int RESULT_SETTINGS = 1; // is this needed?

	// Array projection with calendar details
	public static final String[] INSTANCE_PROJECTION = new String[] {
			Calendars._ID, // 0
			Instances.BEGIN, // 1
			Instances.END, // 2
			Instances.TITLE, // 3
			Instances.DESCRIPTION // 4
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_BEGIN_INDEX = 1;
	private static final int PROJECTION_END_INDEX = 2;
	private static final int PROJECTION_TITLE_INDEX = 3;
	private static final int PROJECTION_DESCRIPTION_INDEX = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			break;
		}
		return true;
	}

	/** Called when the user clicks the Send button */
	public void setInput(View view) {
		Intent intent = new Intent(this, SetInputActivity.class);
		startActivity(intent);

	}

	private void getCalNames() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		calName = sharedPrefs.getString("prefCalName", "NULL");
		calType = sharedPrefs.getString("prefCalType", "NULL");
		calOwner = sharedPrefs.getString("prefCalOwner", "NULL");
	}

	/** Opens the default calendar */
	public void openCal(View view) {

		long startMillis = 0;
		Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
		builder.appendPath("time");
		ContentUris.appendId(builder, startMillis);
		Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
		startActivity(intent);
	}

	/** Grabs todays calendar events and displays them */
	public void showCalEvents(View view) {
		getCalNames();

		TextView calData = (TextView) findViewById(R.id.calData);
		calData.setSingleLine(false);
		StringBuilder sb = new StringBuilder();
		String details = "";

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DATE);

		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, date, 0, 0);
		long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, date, 23, 59);
		long endMillis = endTime.getTimeInMillis();

		ContentResolver cr = getContentResolver();

		// Construct the query with the desired date range.
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);
		String selection;
		String[] selectionArgs;
		CheckBox cbShowAll = (CheckBox) findViewById(R.id.cbShowAll);
		if (cbShowAll.isChecked()) {
			selection = null;
			selectionArgs = null;
		} else {
			selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
					+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
					+ Calendars.OWNER_ACCOUNT + " = ?))";
			selectionArgs = new String[] { calName, calType, calOwner };

			Log.i(DEBUG_TAG, "Don't show all calendars!");
		}

		// Submit the query
		cur = cr.query(builder.build(), INSTANCE_PROJECTION, selection,
				selectionArgs, null);
		cur.moveToFirst();

		// Go through Calendar events and print them
		while (cur.moveToNext()) {

			long calID = 0;
			long beginVal = 0;
			long endVal = 0;
			String title = null;
			String description = null;

			// Get the field values
			calID = cur.getLong(PROJECTION_ID_INDEX);
			beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
			endVal = cur.getLong(PROJECTION_END_INDEX);
			title = cur.getString(PROJECTION_TITLE_INDEX);
			description = cur.getString(PROJECTION_DESCRIPTION_INDEX);

			Log.w(DEBUG_TAG, "CalID1: " + calID);

			Calendar calBegin = Calendar.getInstance();
			calBegin.setTimeInMillis(beginVal);
			Calendar calEnd = Calendar.getInstance();
			calEnd.setTimeInMillis(endVal);

			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			String eventStart = formatter.format(calBegin.getTime());
			String eventEnd = formatter.format(calEnd.getTime());

			Log.i(DEBUG_TAG, title + ": " + eventStart + " - " + eventEnd + " "
					+ description);

			// Write events to TextView, one event per line
			details = title + ": " + eventStart + " - " + eventEnd + ":  "
					+ description;
			sb.append(details + "\n");
			calData.setText(sb.toString());

		}
	}
}
