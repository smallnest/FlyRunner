package com.colobu.flyrunner;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.colobu.flyrunner.utils.SharedPreferencesUtils;

public class SettingActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SharedPreferencesUtils.PREFS_NAME);
		addPreferencesFromResource(R.xml.flyrunner_preference);    
		this.getListView().setBackgroundResource(R.drawable.backgroup1);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		
		return false;
	}
}
