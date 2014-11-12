package com.derasd.whosdacook;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.provider.CalendarContract.Reminders;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String DEBUG_TAG = "com.derasd.whosdacook";

	private static final int RESULT_SETTINGS = 1; // is this needed?

	private String calTitle = "";
	private String calPlusOne = "";
	private String tfProp = "";
	private String calName = "";
	private String calType = "";
	private String calOwner = "";
	private String calEntry = "";

	private boolean alertEmail;
	private boolean alertPop;
	private long startMillis = 0;
	private long endMillis = 0;
	private int startHour = 0;
	private int startMin = 0;
	private int endHour = 23; // if no endhour is chosen asume we stay all night
	private int endMin = 0;
	private boolean startSet = false;

	// Array projection with calendar details
	public static final String[] INSTANCE_PROJECTION = new String[] { Calendars._ID, // 0
			Instances.BEGIN, // 1
			Instances.END, // 2
			Instances.TITLE, // 3
			Instances.DESCRIPTION // 4
	};

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] { Calendars._ID, // 0
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;

	String[] array = { "Reis", "Nudeln", "Spaghetti", "Hamburger", "Roesti", "Thai Curry", "Ravioli", "Eight", "Nine", "Ten" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupActionBar(); // Show the Up button in the action bar.
		addListenerOnButton(); // add button listener

		// Adds autocomplete to the menu proposal field
		AutoCompleteTextView autoTV = (AutoCompleteTextView) findViewById(R.id.tfProp);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, array);
		autoTV.setThreshold(1);
		autoTV.setAdapter(adapter);
	}

	private void getPreferences() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		calTitle = sharedPrefs.getString("prefUsername", "NULL");

		alertEmail = sharedPrefs.getBoolean("prefAlertEmail", false);
		alertPop = sharedPrefs.getBoolean("prefAlertPop", false);

		calName = sharedPrefs.getString("prefCalName", "NULL");
		calType = sharedPrefs.getString("prefCalType", "NULL");
		calOwner = sharedPrefs.getString("prefCalOwner", "NULL");

		// Remove spaces and convert to lower case
		calName = calName.replaceAll("\\s+$", "");
		calName = calName.toLowerCase();
		calType = calType.replaceAll("\\s+$", "");
		calType = calType.toLowerCase();
		calOwner = calOwner.replaceAll("\\s+$", "");
		calOwner = calOwner.toLowerCase();

	}

	/** Defines start and endtime according to the checked checkboxes */
	public void addListenerOnButton() {

		final RadioGroup rg1 = (RadioGroup) findViewById(R.id.radioGroup1);
		final RadioGroup rg2 = (RadioGroup) findViewById(R.id.radioGroup2);

		rg1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected

				if (checkedId == R.id.rbCook) {
					calEntry = "I'll cook!";
					rg2.clearCheck();
				} else if (checkedId == R.id.rbJoin) {
					calEntry = "I'll join!";
					rg2.clearCheck();
				} else if (checkedId == R.id.rbCookJoin) {
					calEntry = "I'll cook/join!";
					rg2.clearCheck();

				}
			}
		});

		rg2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected
				if (checkedId == R.id.rbNot) {
					calEntry = "Not attending!";
					rg1.clearCheck();
				} else if (checkedId == R.id.rbLater) {
					calEntry = "I'll eat later!";
					rg1.clearCheck();

				}
			}
		});

		Button btSend = (Button) findViewById(R.id.btSend);
		btSend.setOnClickListener(new OnClickListener() {
			CheckBox cb16 = (CheckBox) findViewById(R.id.cb16);
			CheckBox cb17 = (CheckBox) findViewById(R.id.cb17);
			CheckBox cb18 = (CheckBox) findViewById(R.id.cb18);
			CheckBox cb19 = (CheckBox) findViewById(R.id.cb19);
			CheckBox cb20 = (CheckBox) findViewById(R.id.cb20);
			CheckBox cb21 = (CheckBox) findViewById(R.id.cb21);
			CheckBox cb22 = (CheckBox) findViewById(R.id.cb22);
			CheckBox cbLeave15 = (CheckBox) findViewById(R.id.cbLeave15);
			CheckBox cbLeave30 = (CheckBox) findViewById(R.id.cbLeave30);
			CheckBox cbArrive15 = (CheckBox) findViewById(R.id.cbArrive15);
			CheckBox cbArrive30 = (CheckBox) findViewById(R.id.cbArrive30);
			CheckBox cbPlusOne = (CheckBox) findViewById(R.id.cbPlusOne);


			@Override
			public void onClick(View view) {
				startSet = false;

				if (cb16.isChecked() && !startSet) {
					startHour = 16;
					startSet = true;
				} else if (cb16.isChecked() && startSet) {
					endHour = 16;
				}
				if (cb17.isChecked() && !startSet) {
					startHour = 17;
					startSet = true;
				} else if (cb17.isChecked() && startSet) {
					endHour = 17;
				}
				if (cb18.isChecked() && !startSet) {
					startHour = 18;
					startSet = true;
				} else if (cb18.isChecked() && startSet) {
					endHour = 17;
				}
				if (cb19.isChecked() && !startSet) {
					startHour = 19;
					startSet = true;
				} else if (cb19.isChecked() && startSet) {
					endHour = 19;
				}
				if (cb20.isChecked() && !startSet) {
					startHour = 20;
					startSet = true;
				} else if (cb20.isChecked() && startSet) {
					endHour = 20;
				}
				if (cb21.isChecked() && !startSet) {
					startHour = 21;
					startSet = true;
				} else if (cb21.isChecked() && startSet) {
					endHour = 21;
				}
				if (cb22.isChecked() && !startSet) {
					startHour = 22;
					startSet = true;
				} else if (cb22.isChecked() && startSet) {
					endHour = 22;
				}
				if (cbArrive15.isChecked() && cbArrive30.isChecked()) {
					startMin = 45;
				} else if (cbArrive15.isChecked()) {
					startMin = 15;
				} else if (cbArrive30.isChecked()) {
					startMin = 30;
				} else if (!cbArrive30.isChecked() && !cbArrive15.isChecked()) {
					startMin = 00;
				}

				if (cbLeave15.isChecked() && cbLeave30.isChecked()) {
					endMin = 45;
					if (endHour == 23) {
						endHour = startHour;
					}
				} else if (cbLeave15.isChecked()) {
					endMin = 15;
					if (endHour == 23) {
						endHour = startHour;
					}
				} else if (cbLeave30.isChecked()) {
					endMin = 30;
					if (endHour == 23) {
						endHour = startHour;
					}
				} else if (!cbLeave30.isChecked() && !cbLeave15.isChecked()) {
					endMin = 00;
				}
				if (cbPlusOne.isChecked()) {
					calPlusOne = "+1 ";
				}
				readProp();
				getStartEndTime();
				getPreferences();
				createEvent();

			}
		});
	}

	/** Reads the menu proposal field */
	private void readProp() {

		EditText text = (EditText) findViewById(R.id.tfProp);
		tfProp = text.getText().toString();
		Log.i(MainActivity.DEBUG_TAG, "Proposal:" + tfProp);
	}

	/** Calculates start and endtime and shows a toast */
	private void getStartEndTime() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int date = c.get(Calendar.DATE);

		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, date, startHour, startMin);
		startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, date, endHour, endMin);
		endMillis = endTime.getTimeInMillis();

		// Change format for display
		Calendar calBegin = Calendar.getInstance();
		calBegin.setTimeInMillis(startMillis);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(endMillis);

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
		String eventStart = formatter.format(calBegin.getTime());
		String eventEnd = formatter.format(calEnd.getTime());

		Log.i(MainActivity.DEBUG_TAG, "Time: " + year + "," + month + "," + date + ";" + ": " + eventStart + " - " + eventEnd);

		// Show a Toast
		Context context = getApplicationContext();
		CharSequence text = eventStart + " - " + eventEnd + ": " + calPlusOne + calEntry + " " + tfProp;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public void createEvent() {

		long calID = 0;
		// Run query
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" + Calendars.ACCOUNT_TYPE + " = ?) AND (" + Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] { calName, calType, calOwner };
		Log.i(DEBUG_TAG, "uri: " + uri);
		// Submit the query and get a Cursor object back.
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		Log.i(DEBUG_TAG, "Cur : " + cur);
		// Use the cursor to step through the returned records
		while (cur.moveToNext()) {
			calID = cur.getLong(PROJECTION_ID_INDEX);
			Log.i(DEBUG_TAG, "CalID 123: " + calID);

		}

		// ContentResolver cr = getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, startMillis);
		values.put(Events.DTEND, endMillis);
		values.put(Events.TITLE, calTitle + ": " + calPlusOne + calEntry + " " + tfProp);
		values.put(Events.CALENDAR_ID, calID);
		values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		Log.i(DEBUG_TAG, "Timezone retrieved=>" + TimeZone.getDefault().getID());
		// values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
		uri = cr.insert(Events.CONTENT_URI, values);

		Log.w(DEBUG_TAG, "CalID: " + calID);

		// get the event ID that is the last element in the Uri
		long eventID = Long.parseLong(uri.getLastPathSegment());

		// Add reminder
		// Calculate reminder time, CurrentTime-EventTime-1minute
		long time = System.currentTimeMillis();
		int selectedReminderValue = ((int) ((startMillis - time) / 60000)) - 1;
		Log.w(MainActivity.DEBUG_TAG, "Time until Alarm: " + selectedReminderValue + " min");

		if (alertPop) {
			ContentValues reminder1 = new ContentValues();
			reminder1.put(Reminders.EVENT_ID, eventID);
			reminder1.put(Reminders.METHOD, Reminders.METHOD_ALERT);
			reminder1.put(Reminders.MINUTES, selectedReminderValue);
			Uri uri2 = cr.insert(Reminders.CONTENT_URI, reminder1);
		}
		if (alertEmail) {
			ContentValues reminder2 = new ContentValues();
			reminder2.put(Reminders.EVENT_ID, eventID);
			reminder2.put(Reminders.METHOD, Reminders.METHOD_EMAIL);
			reminder2.put(Reminders.MINUTES, selectedReminderValue);
			Uri uri3 = cr.insert(Reminders.CONTENT_URI, reminder2);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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

}
