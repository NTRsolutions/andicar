/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2010 Miklos Keresztes (miklos.keresztes@gmail.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.andicar.activity.preference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import java.util.HashMap;
import java.util.Map;
import org.andicar.activity.BPartnerListActivity;
import org.andicar.activity.CarListActivity;
import org.andicar.activity.CurrencyListActivity;
import org.andicar.activity.CurrencyRateListActivity;
import org.andicar.activity.DriverListActivity;
import org.andicar.activity.ExpenseCategoryListActivity;
import org.andicar.activity.ExpenseTypeListActivity;
import org.andicar.activity.R;
import org.andicar.activity.UOMConversionListActivity;
import org.andicar.activity.UOMListActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.utils.StaticValues;
import org.andicar.utils.AndiCarExceptionHandler;
import org.andicar.utils.AndiCarStatistics;

import com.andicar.activity.preference.AddOnServices;

/**
 *
 * @author miki
 */
public class AndiCarPreferencesActivity extends PreferenceActivity {

    private Resources mRes = null;
    protected SharedPreferences mPreferences;
    private boolean isSendStatistics;
    private boolean isSendCrashReport;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);
        isSendStatistics = mPreferences.getBoolean("SendUsageStatistics", true);
        isSendCrashReport = mPreferences.getBoolean("SendCrashReport", true);
        if(isSendCrashReport)
            Thread.setDefaultUncaughtExceptionHandler(
                    new AndiCarExceptionHandler(Thread.getDefaultUncaughtExceptionHandler(), this));
        mRes = getResources();

        setPreferenceScreen(createPreferenceHierarchy());

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(isSendStatistics)
            AndiCarStatistics.sendFlurryStartSession(this);
    }
    @Override
    protected void onStop()
    {
        super.onStop();

        if(isSendStatistics != mPreferences.getBoolean("SendUsageStatistics", true)){
            if(!isSendStatistics)
                AndiCarStatistics.sendFlurryStartSession(this);
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("SendStatisticsChanged", "From " + isSendStatistics + " to " + mPreferences.getBoolean("SendUsageStatistics", true));
            AndiCarStatistics.sendFlurryEvent("SendStatistics", parameters);
            if(!isSendStatistics)
                AndiCarStatistics.sendFlurryEndSession(this);
        }

        if(isSendStatistics){
            AndiCarStatistics.sendFlurryEndSession(this);
        }
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(StaticValues.GLOBAL_PREFERENCE_NAME);
        PreferenceScreen prefScreenRoot = prefMgr.createPreferenceScreen(this);

        //cars and drivers
        PreferenceCategory carDriverCategory = new PreferenceCategory(this);
        carDriverCategory.setTitle(mRes.getString(R.string.PREF_CarDriverCategoryTitle));
        prefScreenRoot.addPreference(carDriverCategory);
        //cars
        PreferenceScreen carPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        carPrefScreen.setIntent(new Intent(this, CarListActivity.class));
        carPrefScreen.setTitle(mRes.getString(R.string.PREF_CarTitle));
        carPrefScreen.setSummary(mRes.getString(R.string.PREF_CarSummary));
        carDriverCategory.addPreference(carPrefScreen);
        //drivers
        PreferenceScreen driverPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        driverPrefScreen.setIntent(new Intent(this, DriverListActivity.class));
        driverPrefScreen.setTitle(mRes.getString(R.string.PREF_DriverTitle));
        driverPrefScreen.setSummary(mRes.getString(R.string.PREF_DriverSummary));
        carDriverCategory.addPreference(driverPrefScreen);

        //business partners
        PreferenceCategory bPartnerCategory = new PreferenceCategory(this);
        bPartnerCategory.setTitle(mRes.getString(R.string.PREF_BPartnersCategoryTitle));
        prefScreenRoot.addPreference(bPartnerCategory);
        //partners
        PreferenceScreen partnersPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        partnersPrefScreen.setIntent(new Intent(this, BPartnerListActivity.class));
        partnersPrefScreen.setTitle(mRes.getString(R.string.PREF_PartnerTitle));
        partnersPrefScreen.setSummary(mRes.getString(R.string.PREF_PartnerSummary));
        bPartnerCategory.addPreference(partnersPrefScreen);

        //AddOn Services
        PreferenceCategory addOnCategory = new PreferenceCategory(this);
        addOnCategory.setTitle(mRes.getString(R.string.PREF_AddOnCategoryTitle));
        prefScreenRoot.addPreference(addOnCategory);
        //AddOn screen
        PreferenceScreen addOnScreen = getPreferenceManager().createPreferenceScreen(this);
        addOnScreen.setIntent(new Intent(this, AddOnServices.class));
        addOnScreen.setTitle(mRes.getString(R.string.PREF_AddOnServicesTitle));
        addOnScreen.setSummary(mRes.getString(R.string.PREF_AddOnServicesSummary));
        addOnCategory.addPreference(addOnScreen);

        //uom's
        PreferenceCategory uomPrefCategory = new PreferenceCategory(this);
        uomPrefCategory.setTitle(mRes.getString( R.string.PREF_UOMCategoryTitle ));
        prefScreenRoot.addPreference(uomPrefCategory);
        //preference for uom
        PreferenceScreen uomLengthPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        Intent uomLengthIntent = new Intent(this, UOMListActivity.class);
        uomLengthPrefScreen.setIntent(uomLengthIntent);
        uomLengthPrefScreen.setTitle(mRes.getString(R.string.PREF_UOMTitle));
        uomLengthPrefScreen.setSummary(mRes.getString(R.string.PREF_UOMSummary));
        uomPrefCategory.addPreference(uomLengthPrefScreen);
        //uom conversions
        PreferenceScreen uomConversionPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        uomConversionPrefScreen.setIntent(new Intent(this, UOMConversionListActivity.class));
        uomConversionPrefScreen.setTitle(mRes.getString(R.string.PREF_UOMConversionTitle));
        uomConversionPrefScreen.setSummary(mRes.getString(R.string.PREF_UOMConversionSummary));
        uomPrefCategory.addPreference(uomConversionPrefScreen);

        //Backup/Restore
        PreferenceCategory bkRestoreCategory = new PreferenceCategory(this);
        bkRestoreCategory.setTitle(mRes.getString(R.string.PREF_BackupRestoreCategoryTitle));
        prefScreenRoot.addPreference(bkRestoreCategory);
        PreferenceScreen bkRestorePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        bkRestorePrefScreen.setIntent(new Intent(this, BackupRestoreActivity.class));
        bkRestorePrefScreen.setTitle(mRes.getString(R.string.PREF_BackupRestoreTitle));
        bkRestorePrefScreen.setSummary(mRes.getString(R.string.PREF_BackupRestoreSummary));
        bkRestoreCategory.addPreference(bkRestorePrefScreen);

        //Expenses settings
        PreferenceCategory expenseCategory = new PreferenceCategory(this);
        expenseCategory.setTitle(mRes.getString(R.string.PREF_ExpenseCategoryTitle));
        prefScreenRoot.addPreference(expenseCategory);

        //expense categories
        PreferenceScreen expCategoryPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        expCategoryPrefScreen.setIntent(new Intent(this, ExpenseCategoryListActivity.class));
        expCategoryPrefScreen.setTitle(mRes.getString(R.string.PREF_ExpenseCategoryCategoryTitle));
        expCategoryPrefScreen.setSummary(mRes.getString(R.string.PREF_ExpenseCategoryCategorySummary));
        expenseCategory.addPreference(expCategoryPrefScreen);

        //expense types
        PreferenceScreen expTypePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        expTypePrefScreen.setIntent(new Intent(this, ExpenseTypeListActivity.class));
        expTypePrefScreen.setTitle(mRes.getString(R.string.PREF_ExpenseTypeCategoryTitle));
        expTypePrefScreen.setSummary(mRes.getString(R.string.PREF_ExpenseTypeCategorySummary));
        expenseCategory.addPreference(expTypePrefScreen);

        //currencies
        PreferenceScreen currencyPrefScreen = getPreferenceManager().createPreferenceScreen(this);
        currencyPrefScreen.setIntent(new Intent(this, CurrencyListActivity.class));
        currencyPrefScreen.setTitle(mRes.getString(R.string.PREF_CurrencyTitle));
        currencyPrefScreen.setSummary(mRes.getString(R.string.PREF_CurrencySummary));
        expenseCategory.addPreference(currencyPrefScreen);
        //currency rates
        PreferenceScreen currencyRatePrefScreen = getPreferenceManager().createPreferenceScreen(this);
        currencyRatePrefScreen.setIntent(new Intent(this, CurrencyRateListActivity.class));
        currencyRatePrefScreen.setTitle(mRes.getString(R.string.PREF_CurrencyRateCategoryTitle));
        currencyRatePrefScreen.setSummary(mRes.getString(R.string.PREF_CurrencyRateCategorySummary));
        expenseCategory.addPreference(currencyRatePrefScreen);

        //gps track
        PreferenceCategory gpsTrackCategory = new PreferenceCategory(this);
        gpsTrackCategory.setTitle(mRes.getString(R.string.PREF_GPSTrackCategoryTitle));
        prefScreenRoot.addPreference(gpsTrackCategory);
        //gps
        PreferenceScreen gpsTrackScreen = getPreferenceManager().createPreferenceScreen(this);
        gpsTrackScreen.setIntent(new Intent(this, GPSPreferencesActivity.class));
        gpsTrackScreen.setTitle(mRes.getString(R.string.PREF_GPSTrackTitle));
        gpsTrackScreen.setSummary(mRes.getString(R.string.PREF_GPSTrackSummary));
        gpsTrackCategory.addPreference(gpsTrackScreen);

        //Misc settings
        PreferenceCategory miscCategory = new PreferenceCategory(this);
        miscCategory.setTitle(mRes.getString(R.string.PREF_MiscCategoryTitle));
        prefScreenRoot.addPreference(miscCategory);

        //main screen pref
        PreferenceScreen mainScreenPref = getPreferenceManager().createPreferenceScreen(this);
        mainScreenPref.setIntent(new Intent(this, MainScreenPreferenceActivity.class));
        mainScreenPref.setTitle(mRes.getString(R.string.PREF_MainScreenCategoryTitle));
        mainScreenPref.setSummary(mRes.getString(R.string.PREF_MainScreenCategorySummary));
        miscCategory.addPreference(mainScreenPref);

        //numeric inout type
        CheckBoxPreference useNumericInput = new CheckBoxPreference(this);
        useNumericInput.setTitle(R.string.PREF_UseNumericInputTitle);
        useNumericInput.setSummary(R.string.PREF_UseNumericInputSummary);
        useNumericInput.setKey("UseNumericKeypad");
        miscCategory.addPreference(useNumericInput);

        //send crash and usage statistiscs
        CheckBoxPreference sendUsagePrefCk = new CheckBoxPreference(this);
        sendUsagePrefCk.setTitle(R.string.PREF_SendUsageTitle);
        sendUsagePrefCk.setSummary(R.string.PREF_SendUsageSummary);
        sendUsagePrefCk.setKey("SendUsageStatistics");
        miscCategory.addPreference(sendUsagePrefCk);

        CheckBoxPreference sendCrashPrefCk = new CheckBoxPreference(this);
        sendCrashPrefCk.setTitle(R.string.PREF_SendCrashReportsTitle);
        sendCrashPrefCk.setSummary(R.string.PREF_SendCrashReportsSummary);
        sendCrashPrefCk.setKey("SendCrashReport");
        miscCategory.addPreference(sendCrashPrefCk);

        return prefScreenRoot;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPreferences.getBoolean("MustClose", false)){
            finish();
        }
    }
}
