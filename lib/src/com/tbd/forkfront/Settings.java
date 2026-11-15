package com.tbd.forkfront;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.tbd.forkfront.TilesetPreference;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private TilesetPreference mTilesetPref;
	private ExportPreferences mExportPreferences;
	private ImportPreferences mImportPreferences;
	public static final int RESULT_SAVE_AND_EXIT = 100;
	public static final int RESULT_SAVE_GAME = 101;

	// ____________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// turn off the window's title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		View btnBack = findViewById(R.id.btn_back);
		if(btnBack != null)
		{
			btnBack.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					finish();
				}
			});
		}

		View btnSaveExit = findViewById(R.id.btn_save_exit);
		if(btnSaveExit != null)
		{
			btnSaveExit.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					setResult(RESULT_SAVE_GAME);
					finish();
				}
			});
		}

		ListView mainList = (ListView) findViewById(android.R.id.list);
		View pagerBar = findViewById(R.id.pager_bar);
		Button btnPrev = (Button) findViewById(R.id.btn_prev_page);
		TextView txtPage = (TextView) findViewById(R.id.txt_page_num);
		Button btnNext = (Button) findViewById(R.id.btn_next_page);
		View pagerDivider = findViewById(R.id.pager_divider);
		if(mainList != null && pagerBar != null)
		{
			EInkPager.setupListViewPager(mainList, pagerBar, btnPrev, txtPage, btnNext, pagerDivider);
		}

		addPreferencesFromResource(R.xml.preferences);
	}

	// ____________________________________________________________________________________
	@Override
	protected void onResume()
	{
		super.onResume();

		setupDialogPreferences(getPreferenceScreen());

		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

		for(int i = 0; i < 10; i++)
		{
			char idx = (char)('0' + i);
			Preference screen = findPreference("panel" + idx);

			if(screen == null)
				break;

			String name = sharedPreferences.getString("pName" + idx, "");

			screen.setTitle(name);
		}

		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mTilesetPref = (TilesetPreference)findPreference("tilesetPreference");
		mTilesetPref.setActivity(this);
		mExportPreferences = (ExportPreferences)findPreference("exportPreferences");
		mExportPreferences.setActivity(this);
		mImportPreferences = (ImportPreferences)findPreference("importPreferences");
		mImportPreferences.setActivity(this);

		if(!getApplicationContext().getResources().getBoolean(R.bool.hearseAvailable))
		{
			PreferenceCategory hearseParent = (PreferenceCategory)findPreference("advanced");
			Preference hearsePref = findPreference("hearse");
			if(hearsePref != null)
				hearseParent.removePreference(hearsePref);
		}

		// Immersive mode only available on API 11 and up
		PreferenceCategory settingsCategory = (PreferenceCategory)findPreference("settings");
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			Preference fullscreenPref = findPreference("immersive");
			if(fullscreenPref != null)
				settingsCategory.removePreference(fullscreenPref);
		}
	}

	// ____________________________________________________________________________________
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (mTilesetPref.onActivityResult(requestCode, resultCode, data)) return;
		if (mExportPreferences.onActivityResult(requestCode, resultCode, data)) return;
		if (mImportPreferences.onActivityResult(requestCode, resultCode, data)) return;
	}

	// ____________________________________________________________________________________
	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	// ____________________________________________________________________________________
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if(key.startsWith("pName"))
		{
			char idx = key.charAt(key.length() - 1);
			Preference screen = findPreference("panel" + idx);
			String name = sharedPreferences.getString("pName" + idx, "");
			screen.setTitle(name);
		}
	}

	// ____________________________________________________________________________________
	private void setupDialogPreferences(PreferenceGroup group)
	{
		if(group == null)
			return;
		for(int i = 0; i < group.getPreferenceCount(); i++)
		{
			Preference pref = group.getPreference(i);
			if(pref instanceof DialogPreference)
			{
				final DialogPreference dp = (DialogPreference)pref;
				if(dp.getNegativeButtonText() == null)
				{
					dp.setNegativeButtonText(android.R.string.cancel);
				}
				final Preference.OnPreferenceClickListener existing = dp.getOnPreferenceClickListener();
				dp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
				{
					@Override
					public boolean onPreferenceClick(Preference p)
					{
						if(existing != null)
							existing.onPreferenceClick(p);
						getWindow().getDecorView().post(new Runnable()
						{
							@Override
							public void run()
							{
								Dialog d = dp.getDialog();
								if(d != null)
									d.setCanceledOnTouchOutside(true);
							}
						});
						return false;
					}
				});
			}
			if(pref instanceof PreferenceGroup)
			{
				setupDialogPreferences((PreferenceGroup)pref);
			}
		}
	}

	// ____________________________________________________________________________________
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
	{
		super.onPreferenceTreeClick(preferenceScreen, preference);

		if(preference instanceof PreferenceScreen)
		{
			final PreferenceScreen subScreen = (PreferenceScreen)preference;
			final Dialog dialog = subScreen.getDialog();
			if(dialog != null)
			{
				dialog.setCanceledOnTouchOutside(true);

				View content = dialog.findViewById(android.R.id.content);
				if(content instanceof ViewGroup)
				{
					ViewGroup group = (ViewGroup)content;
					if(group.findViewById(R.id.btn_back) == null && group.getChildCount() > 0)
					{
						View listView = group.getChildAt(0);
						group.removeView(listView);

						final float density = getResources().getDisplayMetrics().density;

						LinearLayout container = new LinearLayout(this);
						container.setOrientation(LinearLayout.VERTICAL);
						container.setBackgroundColor(0xFFFFFFFF);
						int p5 = (int)(5 * density);
						container.setPadding(p5, p5, p5, p5);

						LinearLayout header = new LinearLayout(this);
						header.setOrientation(LinearLayout.HORIZONTAL);
						header.setGravity(Gravity.CENTER_VERTICAL);
						int p8 = (int)(8 * density);
						int p12 = (int)(12 * density);
						header.setPadding(p8, p8, p8, p8);
						header.setBackgroundColor(0xFFFFFFFF);

						Button btnBack = new Button(this);
						btnBack.setId(R.id.btn_back);
						btnBack.setText("Back");
						btnBack.setTextAppearance(this, R.style.ButtonText);
						btnBack.setBackgroundResource(R.drawable.btn_eink_selector);
						btnBack.setTextColor(getResources().getColorStateList(R.color.btn_eink_text_selector));
						btnBack.setMinimumWidth((int)(90 * density));
						btnBack.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								dialog.dismiss();
							}
						});
						header.addView(btnBack, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

						TextView title = new TextView(this);
						title.setText(subScreen.getTitle());
						title.setTextAppearance(this, R.style.DialogTitle);
						title.setPadding(p12, 0, 0, 0);
						title.setSingleLine(true);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
						header.addView(title, lp);

						container.addView(header);

						View divider = new View(this);
						divider.setBackgroundColor(0xFF000000);
						container.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(2 * density)));

						container.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

						View subPagerDivider = new View(this);
						subPagerDivider.setBackgroundColor(0xFF000000);
						subPagerDivider.setVisibility(View.GONE);
						container.addView(subPagerDivider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(1 * density)));

						LinearLayout subPager = new LinearLayout(this);
						subPager.setOrientation(LinearLayout.HORIZONTAL);
						subPager.setGravity(Gravity.CENTER);
						int p6 = (int)(6 * density);
						int p16 = (int)(16 * density);
						subPager.setPadding(0, p6, 0, p6);
						subPager.setVisibility(View.GONE);

						Button subBtnPrev = new Button(this);
						subBtnPrev.setText("\u25c4 Prev");
						subBtnPrev.setTextAppearance(this, R.style.ButtonText);
						subBtnPrev.setBackgroundResource(R.drawable.btn_eink_selector);
						subBtnPrev.setTextColor(getResources().getColorStateList(R.color.btn_eink_text_selector));
						subBtnPrev.setMinimumWidth((int)(90 * density));
						subPager.addView(subBtnPrev, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

						TextView subTxtPage = new TextView(this);
						subTxtPage.setText("Page 1/1");
						subTxtPage.setTextAppearance(this, R.style.DialogText);
						subTxtPage.setPadding(p16, 0, p16, 0);
						subPager.addView(subTxtPage, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

						Button subBtnNext = new Button(this);
						subBtnNext.setText("Next \u25ba");
						subBtnNext.setTextAppearance(this, R.style.ButtonText);
						subBtnNext.setBackgroundResource(R.drawable.btn_eink_selector);
						subBtnNext.setTextColor(getResources().getColorStateList(R.color.btn_eink_text_selector));
						subBtnNext.setMinimumWidth((int)(90 * density));
						subPager.addView(subBtnNext, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

						container.addView(subPager);

						if(listView instanceof ListView)
						{
							EInkPager.setupListViewPager((ListView)listView, subPager, subBtnPrev, subTxtPage, subBtnNext, subPagerDivider);
						}

						group.addView(container);
					}
				}
			}
		}
		else if(preference instanceof DialogPreference)
		{
			final DialogPreference dp = (DialogPreference)preference;
			Dialog dialog = dp.getDialog();
			if(dialog != null)
			{
				dialog.setCanceledOnTouchOutside(true);
			}
		}

		return false;
	}
}
