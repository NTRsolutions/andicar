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
package org.andicar.activity;

import android.content.pm.PackageManager.NameNotFoundException;
import org.andicar.activity.report.RefuelListReportActivity;
import org.andicar.activity.report.MileageListReportActivity;
import org.andicar.activity.miscellaneous.PreferencesActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import org.andicar.utils.StaticValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.database.Cursor;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import org.andicar.activity.miscellaneous.AboutActivity;
import org.andicar.activity.miscellaneous.BackupRestoreActivity;
import org.andicar.activity.report.ExpensesListReportActivity;
import org.andicar.persistence.FileUtils;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;

/**
 *
 * @author miki
 */
public class MainActivity extends Activity {

    private Resources mRes = null;
    private long currentDriverID = -1;
    private String currentDriverName = "";
    private long currentCarID = -1;
    private String currentCarName = "";
    private String infoStr = "";
    private Context mainContext;
    private int ACTIVITY_MILEAGEINSERT_REQUEST_CODE = 0;
    private int ACTIVITY_REFUELINSERT_REQUEST_CODE = 1;
    private int ACTIVITY_EXPENSEINSERT_REQUEST_CODE = 2;
    private SharedPreferences mPreferences;

    private Button mileageListBtn;
    private Button mileageInsertBtn;
    private Button refuelListBtn;
    private Button refuelInsertBtn;
    private Button expenseListBtn;
    private Button expenseInsertBtn;

    private ReportDbAdapter reportDb;
    private Cursor listCursor;
    private TextView threeLineListMileageText1;
    private TextView threeLineListMileageText2;
    private TextView threeLineListMileageText3;
    private TextView threeLineListRefuelText1;
    private TextView threeLineListRefuelText2;
    private TextView threeLineListRefuelText3;
    private TextView threeLineListExpenseText1;
    private TextView threeLineListExpenseText2;
    private TextView threeLineListExpenseText3;

    private boolean exitResume = false;
    private String appVersion;
    private boolean showMileageZone = true;
    private boolean showRefuelZone = true;
    private boolean showExpenseZone = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mRes = getResources();
        mPreferences = getSharedPreferences(StaticValues.GLOBAL_PREFERENCE_NAME, 0);

        SharedPreferences.Editor editor = mPreferences.edit();
        if(!mPreferences.contains("MainActivityShowMileage")){
            editor.putBoolean("MainActivityShowMileage", true);
            editor.commit();
        }
        else
            showMileageZone = mPreferences.getBoolean("MainActivityShowMileage", true);
        if(!mPreferences.contains("MainActivityShowRefuel")){
            editor.putBoolean("MainActivityShowRefuel", true);
            editor.commit();
        }
        else
            showRefuelZone = mPreferences.getBoolean("MainActivityShowRefuel", true);
        if(!mPreferences.contains("MainActivityShowExpense")){
            editor.putBoolean("MainActivityShowExpense", true);
            editor.commit();
        }
        else
            showExpenseZone = mPreferences.getBoolean("MainActivityShowExpense", true);
        
        setContentView(R.layout.main_activity);
        mainContext = this;
        reportDb = new ReportDbAdapter(mainContext, null, null);
        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch(NameNotFoundException ex) {
            appVersion = "N/A";
        }

        mileageListBtn = (Button) findViewById(R.id.mainActivityBtnMileageList);
        mileageListBtn.setOnClickListener(btnMileageListClickListener);
        mileageInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertMileage);
        mileageInsertBtn.setOnClickListener(btnInsertMileageClickListener);
        refuelListBtn = (Button) findViewById(R.id.mainActivityBtnRefuelList);
        refuelListBtn.setOnClickListener(btnRefuelListClickListener);
        refuelInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertRefuel);
        refuelInsertBtn.setOnClickListener(btnInsertRefuelClickListener);
        expenseListBtn = (Button) findViewById(R.id.mainActivityBtnExpenseList);
        expenseListBtn.setOnClickListener(btnExpenseListClickListener);
        expenseInsertBtn = (Button) findViewById(R.id.mainActivityBtnInsertExpense);
        expenseInsertBtn.setOnClickListener(btnInsertExpenseClickListener);

        threeLineListMileageText1 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText1);
        threeLineListMileageText2 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText2);
        threeLineListMileageText3 = (TextView) findViewById(R.id.mainActivityThreeLineListMileageText3);
        threeLineListRefuelText1 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText1);
        threeLineListRefuelText2 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText2);
        threeLineListRefuelText3 = (TextView) findViewById(R.id.mainActivityThreeLineListRefuelText3);
        threeLineListExpenseText1 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText1);
        threeLineListExpenseText2 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText2);
        threeLineListExpenseText3 = (TextView) findViewById(R.id.mainActivityThreeLineListExpenseText3);

        if (mPreferences == null || mPreferences.getAll().isEmpty()) { //fresh install
            exitResume = true;
            //test if backups exists
            if (FileUtils.getBkFileNames() != null && !FileUtils.getBkFileNames().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_BACKUPEXISTS));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_YES),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, BackupRestoreActivity.class);
                                startActivity(i);
                                exitResume = false;
                            }
                        });
                builder.setNegativeButton(mRes.getString(R.string.GEN_NO),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                exitResume = false;
                                onResume();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                exitResume = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_WELLCOME_MESSAGE) + "\n"
                        + mRes.getString(R.string.LM_MAIN_ACTIVITY_WELLCOME_MESSAGE2));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                exitResume = false;
                                onResume();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(exitResume)
            return;
        if (mPreferences.getBoolean("MustClose", false)) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean("MustClose", false);
            editor.commit();
            finish();
        }
        ((TextView)findViewById(R.id.mainActivityShortAboutLbl)).setText(Html.fromHtml(
                "<b><i>AndiCar</i></b> is a free and open source car management software for Android powered devices. " +
                "It is licensed under the terms of the GNU General Public License, version 3.<br>" +
                "For more details see the About page.<br>Copyright © 2010 Miklos Keresztes.<br> " +
                "Thank you for using <b><i>AndiCar</i></b>!<br>" +
                "Application version: " + appVersion));
        fillDriverCar();
        Bundle whereConditions = new Bundle();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.MILEAGE_TABLE_NAME, MainDbAdapter.MILEAGE_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportMileageListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListMileageText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListMileageText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListMileageText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            mileageListBtn.setEnabled(true);
        } else {
            threeLineListMileageText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOMILEAGETEXT));
            threeLineListMileageText2.setText("");
            threeLineListMileageText3.setText("");
            mileageListBtn.setEnabled(false);
        }
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.REFUEL_TABLE_NAME, MainDbAdapter.REFUEL_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportRefuelListViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListRefuelText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListRefuelText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListRefuelText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            refuelListBtn.setEnabled(true);
        } else {
            threeLineListRefuelText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOREFUELTEXT));
            threeLineListRefuelText2.setText("");
            threeLineListRefuelText3.setText("");
            refuelListBtn.setEnabled(false);
        }
        listCursor = null;
        whereConditions.clear();
        whereConditions.putString(
                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.EXPENSES_TABLE_NAME,
                    MainDbAdapter.EXPENSES_COL_CAR_ID_NAME) + "=",
                String.valueOf(currentCarID));
        reportDb.setReportSql("reportExpensesListMainViewSelect", whereConditions);
        listCursor = reportDb.fetchReport(1);
        if (listCursor.moveToFirst()) {
            threeLineListExpenseText1.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.FIRST_LINE_LIST_NAME)));
            threeLineListExpenseText2.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.SECOND_LINE_LIST_NAME)));
            threeLineListExpenseText3.setText(listCursor.getString(listCursor.getColumnIndex(ReportDbAdapter.THIRD_LINE_LIST_NAME)));
            expenseListBtn.setEnabled(true);
        } else {
            threeLineListExpenseText1.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOEXPENSETEXT));
            threeLineListExpenseText2.setText(mRes.getString(R.string.MAIN_ACTIVITY_NOEXPENSE_ADITIONAL_TEXT));
            threeLineListExpenseText3.setText("");
        }
        listCursor = null;

        if(!showMileageZone)
            findViewById(R.id.mainActivityMileageZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityMileageZone).setVisibility(View.VISIBLE);
        if(!showRefuelZone)
            findViewById(R.id.mainActivityRefuelZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityRefuelZone).setVisibility(View.VISIBLE);
        if(!showExpenseZone)
            findViewById(R.id.mainActivityExpenseZone).setVisibility(View.GONE);
        else
            findViewById(R.id.mainActivityExpenseZone).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reportDb.close();
    }

    private OnClickListener btnMileageListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, MileageListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

    private OnClickListener btnInsertMileageClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageInsertIntent = new Intent(mainContext, MileageEditActivity.class);
            mileageInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            mileageInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            mileageInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            mileageInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            mileageInsertIntent.putExtra("Operation", "N");
            startActivityForResult(mileageInsertIntent, ACTIVITY_MILEAGEINSERT_REQUEST_CODE);
        }
    };

    private OnClickListener btnRefuelListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, RefuelListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

    private OnClickListener btnInsertRefuelClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent refuelInsertIntent = new Intent(mainContext, RefuelEditActivity.class);
            refuelInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            refuelInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            refuelInsertIntent.putExtra("CurrentDriver_Name", currentDriverName);
            refuelInsertIntent.putExtra("CurrentCar_Name", currentCarName);
            refuelInsertIntent.putExtra("Operation", "N");
            startActivityForResult(refuelInsertIntent, ACTIVITY_REFUELINSERT_REQUEST_CODE);
        }
    };

    private OnClickListener btnExpenseListClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent mileageReportIntent = new Intent(mainContext, ExpensesListReportActivity.class);
            startActivity(mileageReportIntent);
        }
    };

    private OnClickListener btnInsertExpenseClickListener = new OnClickListener() {

        public void onClick(View arg0) {
            Intent refuelInsertIntent = new Intent(mainContext, ExpenseEditActivity.class);
            refuelInsertIntent.putExtra("CurrentDriver_ID", currentDriverID);
            refuelInsertIntent.putExtra("CurrentCar_ID", currentCarID);
            refuelInsertIntent.putExtra("Operation", "N");
            startActivityForResult(refuelInsertIntent, ACTIVITY_EXPENSEINSERT_REQUEST_CODE);
        }
    };

    private void fillDriverCar() {
        if (mPreferences != null) {
            infoStr = mRes.getString(R.string.GEN_DRIVER_LABEL);

            //get the current driver id and name
            if (mPreferences.getLong("CurrentDriver_ID", -1) != -1 && !mPreferences.getAll().isEmpty()) {
                currentDriverID = mPreferences.getLong("CurrentDriver_ID", -1);
            } else { //no saved driver. start driver list activity in order to select one.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_NO_CURRENT_DRIVER));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, DriverListActivity.class);
                                startActivity(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                mileageListBtn.setEnabled(false);
                mileageInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentDriver_Name", "").length() > 0) {
                currentDriverName = mPreferences.getString("CurrentDriver_Name", "");
            }
            infoStr = infoStr + " " + currentDriverName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            //get the current car id and name
            if (mPreferences.getLong("CurrentCar_ID", -1) != -1 && !mPreferences.getAll().isEmpty()) {
                currentCarID = mPreferences.getLong("CurrentCar_ID", -1);
            } else { //no saved car. start car list activity in order to select one.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(mRes.getString(R.string.MAIN_ACTIVITY_NO_CURRENT_CAR));
                builder.setCancelable(false);
                builder.setPositiveButton(mRes.getString(R.string.GEN_OK),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(MainActivity.this, CarListActivity.class);
                                startActivity(i);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                mileageListBtn.setEnabled(false);
                mileageInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
                return;
            }

            if (mPreferences.getString("CurrentCar_Name", "").length() > 0) {
                currentCarName = mPreferences.getString("CurrentCar_Name", "");
            }
            infoStr = infoStr + "; " + mRes.getString(R.string.GEN_CAR_LABEL) + " " + currentCarName;
            ((TextView) findViewById(R.id.info)).setText(infoStr);

            if (currentCarID < 0 || currentDriverID < 0) {
                mileageInsertBtn.setEnabled(false);
                mileageListBtn.setEnabled(false);
                refuelInsertBtn.setEnabled(false);
                refuelListBtn.setEnabled(false);
                expenseListBtn.setEnabled(false);
                expenseInsertBtn.setEnabled(false);
            } else {
                mileageInsertBtn.setEnabled(true);
                mileageListBtn.setEnabled(true);
                refuelInsertBtn.setEnabled(true);
                refuelListBtn.setEnabled(true);
                expenseListBtn.setEnabled(true);
                expenseInsertBtn.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, StaticValues.MENU_PREFERENCES_ID, 0,
                mRes.getText(R.string.MENU_PREFERENCES_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_preferences));
        menu.add(0, StaticValues.MENU_ABOUT_ID, 0,
                mRes.getText(R.string.MENU_ABOUT_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_info_details));
        menu.add(0, StaticValues.MENU_MILEAGE_ID, 0,
                mRes.getText(R.string.MENU_MILEAGE_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_mileage));
        menu.add(0, StaticValues.MENU_REFUEL_ID, 0,
                mRes.getText(R.string.MENU_REFUEL_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_refuel));
        menu.add(0, StaticValues.MENU_EXPENSES_ID, 0,
                mRes.getText(R.string.MENU_EXPENSES_CAPTION)).setIcon(mRes.getDrawable(R.drawable.ic_menu_expenses));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.MENU_PREFERENCES_ID) {
            startActivity(new Intent(this, PreferencesActivity.class));
//            Intent i = new Intent(this, PreferencesActivity.class);
//            startActivityForResult(i, SETTINGS_ACTIVITY_REQUEST_CODE);
//            return true;
        } else if (item.getItemId() == StaticValues.MENU_ABOUT_ID) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_MILEAGE_ID) {
            startActivity(new Intent(this, MileageListReportActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_REFUEL_ID) {
            startActivity(new Intent(this, RefuelListReportActivity.class));
        } else if (item.getItemId() == StaticValues.MENU_EXPENSES_ID) {
            startActivity(new Intent(this, ExpensesListReportActivity.class));
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//		Bundle extras = intent.getExtras();

//        switch( requestCode ) {
//            case SETTINGS_ACTIVITY_REQUEST_CODE:
//                fillDriverCar();
//                break;
//        }
    }
}
