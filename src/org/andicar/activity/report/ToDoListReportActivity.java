/*
 *  AndiCar - a car management software for Android powered devices.
 *
 *  Copyright (C) 2010 - 2011 Miklos Keresztes (miklos.keresztes@gmail.com)
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

package org.andicar.activity.report;

import java.util.Calendar;

import org.andicar2.activity.R;
import org.andicar.activity.TaskEditActivity;
import org.andicar.activity.dialog.AndiCarDialogBuilder;
import org.andicar.persistence.MainDbAdapter;
import org.andicar.persistence.ReportDbAdapter;
import org.andicar.persistence.ToDoListDataBinder;
import org.andicar.service.ToDoManagementService;
import org.andicar.utils.StaticValues;
import org.andicar.utils.Utils;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 *
 * @author miki
 */
public class ToDoListReportActivity extends ReportListActivityBase{
    private View searchView;
    private EditText etUserCommentSearch;
    private TextView tvDateFromSearch;
    private TextView tvDateToSearch;
    private Spinner spnCarSearch;
    private Spinner spnTask;
    private Spinner spnIsDone;

    @Override
    public void onCreate( Bundle icicle )
    {
        reportSelectName = "todoListViewSelect";
//        mCarId = getSharedPreferences( StaticValues.GLOBAL_PREFERENCE_NAME, 0 ).getLong("CurrentCar_ID", 0);
        if(icicle == null){
            whereConditions = new Bundle();
            whereConditions.putString(
                    ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO, MainDbAdapter.COL_NAME_TODO__ISDONE) + "=", "N");
        }
        else
            whereConditions = (Bundle)getLastNonConfigurationInstance();

        initStyle();

        super.onCreate( icicle, null, TaskEditActivity.class, null,
                MainDbAdapter.TABLE_NAME_TODO, ReportDbAdapter.genericReportListViewSelectCols, null,
                null, threeLineListReportActivity,
                new String[]{ReportDbAdapter.FIRST_LINE_LIST_NAME, ReportDbAdapter.SECOND_LINE_LIST_NAME, ReportDbAdapter.THIRD_LINE_LIST_NAME},
                new int[]{R.id.tvThreeLineListReportText1, R.id.tvThreeLineListReportText2, R.id.tvThreeLineListReportText3},
                reportSelectName,  whereConditions, new ToDoListDataBinder());

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        //save existing data whwn the activity restart (for example on screen orientation change)
        return whereConditions;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == StaticValues.OPTION_MENU_SEARCH_ID){
            showDialog(StaticValues.DIALOG_LOCAL_SEARCH);
        }
        else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id != StaticValues.DIALOG_LOCAL_SEARCH)
            return super.onCreateDialog(id);

        LayoutInflater liLayoutFactory = LayoutInflater.from(this);
        searchView = liLayoutFactory.inflate(R.layout.search_dialog_todo, null);
        AndiCarDialogBuilder searchDialog = new AndiCarDialogBuilder(ToDoListReportActivity.this, 
        		AndiCarDialogBuilder.DIALOGTYPE_SEARCH, mRes.getString(R.string.DIALOGSearch_DialogTitle));
        searchDialog.setView(searchView);
        searchDialog.setPositiveButton(R.string.GEN_OK, searchDialogButtonlistener);
        searchDialog.setNegativeButton(R.string.GEN_CANCEL, searchDialogButtonlistener);
        etUserCommentSearch = (EditText) searchView.findViewById(R.id.etUserCommentSearch);
        etUserCommentSearch.setText("%");
        tvDateFromSearch = (TextView) searchView.findViewById(R.id.tvDateFromSearch);
        tvDateToSearch = (TextView) searchView.findViewById(R.id.tvDateToSearch);
        spnCarSearch = (Spinner) searchView.findViewById(R.id.spnCarSearch);
        initSpinner(spnCarSearch, MainDbAdapter.TABLE_NAME_CAR, null, null, 0);
        spnTask = (Spinner) searchView.findViewById(R.id.spnTask);
        initSpinner(spnTask, MainDbAdapter.TABLE_NAME_TASK, null, null, 0);
        spnIsDone = (Spinner) searchView.findViewById(R.id.spnIsActive);
        spnIsDone.setSelection(2); //yes

        ImageButton btnPickDateFrom = (ImageButton) searchView.findViewById(R.id.btnPickDateFrom);
        if(btnPickDateFrom != null)
            btnPickDateFrom.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.DIALOG_DATE_FROM_PICKER);
                }
            });
        
        ImageButton btnPickDateTo = (ImageButton) searchView.findViewById(R.id.btnPickDateTo);
        if(btnPickDateTo != null)
            btnPickDateTo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    showDialog(StaticValues.DIALOG_DATE_TO_PICKER);
                }
            });
        return searchDialog.create();
    }
    
    private DialogInterface.OnClickListener searchDialogButtonlistener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
            	if(whereConditions == null)
            		whereConditions = new Bundle();
                try {
                    whereConditions.clear();
                    if (etUserCommentSearch.getText().toString().length() > 0) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO,
                                		MainDbAdapter.COL_NAME_GEN_USER_COMMENT) + " LIKE ",
                                etUserCommentSearch.getText().toString());
                    }
                    if (tvDateFromSearch.getText().toString().length() > 0) {
//                        whereConditions.putString(
//                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME,
//                                		MainDbAdapter.TODO_COL_DUEDATE_NAME) + " >= ",
//                                Long.toString(Utils.decodeDateStr(etDateFromSearch.getText().toString(),
//                                StaticValues.DATE_DECODE_TO_ZERO) / 1000));
                    	Calendar now = Calendar.getInstance();
                    	long estDueDay =
                    			(
                    			Utils.decodeDateStr(tvDateFromSearch.getText().toString(), StaticValues.DATE_DECODE_TO_ZERO)
                				- 
                				now.getTimeInMillis()
                				) 
                				/ 
                				StaticValues.ONE_DAY_IN_MILISECONDS;
                        whereConditions.putString("EstDueDays >= ", Long.toString(estDueDay));
                    }
                    if (tvDateToSearch.getText().toString().length() > 0) {
//                        whereConditions.putString(
//                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TODO_TABLE_NAME,
//                                		MainDbAdapter.TODO_COL_DUEDATE_NAME) + " <= ",
//                                Long.toString(Utils.decodeDateStr(etDateToSearch.getText().toString(),
//                                StaticValues.DATE_DECODE_TO_24) / 1000));
                    	Calendar now = Calendar.getInstance();
                    	long estDueDay =
                			(
                			Utils.decodeDateStr(tvDateToSearch.getText().toString(), StaticValues.DATE_DECODE_TO_ZERO)
            				- 
            				now.getTimeInMillis()
            				) 
            				/ 
            				StaticValues.ONE_DAY_IN_MILISECONDS;
                        whereConditions.putString("EstDueDays <= ", Long.toString(estDueDay));
                    }
                    if (spnCarSearch.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO,
                                		MainDbAdapter.COL_NAME_TODO__CAR_ID) + "=",
                                String.valueOf(spnCarSearch.getSelectedItemId()));
                    }
                    if (spnTask.getSelectedItemId() != -1) {
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO,
                                		MainDbAdapter.COL_NAME_TODO__TASK_ID) + "=",
                                String.valueOf(spnTask.getSelectedItemId()));
                    }
                    if (spnIsDone.getSelectedItemId() == 1) { //is done
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO,
                                		MainDbAdapter.COL_NAME_TODO__ISDONE) + "=", "Y");
                    }
                    else if (spnIsDone.getSelectedItemId() == 2) { //is not done
                        whereConditions.putString(
                                ReportDbAdapter.sqlConcatTableColumn(MainDbAdapter.TABLE_NAME_TODO,
                                		MainDbAdapter.COL_NAME_TODO__ISDONE) + "=", "N");
                    }
                    mListDbHelper.setReportSql(reportSelectName, whereConditions);
                    fillData();
                } catch (IndexOutOfBoundsException e) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.ERR_008));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                } catch (NumberFormatException e) {
                    errorAlertBuilder.setMessage(mRes.getString(R.string.ERR_008));
                    errorAlert = errorAlertBuilder.create();
                    errorAlert.show();
                }
            }
        };
    };

	/* (non-Javadoc)
	 * @see org.andicar.activity.report.ReportListActivityBase#updateDate(int)
	 */
	@Override
	protected void updateDate(int what) {
		if(what == 1){ //date from
			tvDateFromSearch.setText(mYearFrom + "-" + Utils.pad((mMonthFrom + 1), 2) + "-" + Utils.pad(mDayFrom, 2));
		}
		else{ //date to
			tvDateToSearch.setText(mYearTo + "-" + Utils.pad((mMonthTo + 1), 2) + "-" + Utils.pad(mDayTo, 2));
		}
	}

	/* (non-Javadoc)
	 * @see org.andicar.activity.ListActivityBase#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getItemId() != StaticValues.CONTEXT_MENU_TODO_DONE_ID)
			return super.onContextItemSelected(item);
		else{
			//check if task is one time => if yes delete the to-do and the task
			Cursor c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_TODO, 
					MainDbAdapter.COL_LIST_TODO_TABLE, mLongClickId);
			long taskID = 0;
			boolean isRecurrentTask = false;
			if(c != null){
				taskID = c.getLong(MainDbAdapter.COL_POS_TODO__TASK_ID);
				try{c.close();} catch(Exception e){};
			}
			if(taskID > 0){
				c = mDbAdapter.fetchRecord(MainDbAdapter.TABLE_NAME_TASK, 
						MainDbAdapter.COL_LIST_TASK_TABLE, taskID);
				isRecurrentTask = c.getString(MainDbAdapter.COL_POS_TASK__ISRECURRENT).equals("Y");
				try{c.close();} catch(Exception e){};
			}
			if(!isRecurrentTask){ //if not recurrent => delete the to-do & task
				mDbAdapter.deleteRecord(MainDbAdapter.TABLE_NAME_TODO, mLongClickId);
				mDbAdapter.deleteRecord(MainDbAdapter.TABLE_NAME_TASK, taskID);
				fillData();
				return true;
			}
			
			ContentValues data = new ContentValues();
			data.put(MainDbAdapter.COL_NAME_TODO__ISDONE, "Y");
			int updResult = mDbAdapter.updateRecord(MainDbAdapter.TABLE_NAME_TODO, mLongClickId, data);
			if (updResult != -1) {
				String errMsg = "";
				errMsg = mRes.getString(updResult);
				if (updResult == R.string.ERR_000)
					errMsg = errMsg + "\n" + mDbAdapter.lastErrorMessage;
				errorAlertBuilder.setMessage(errMsg);
				errorAlert = errorAlertBuilder.create();
				errorAlert.show();
			}

			Intent intent = new Intent(this, ToDoManagementService.class);
			intent.putExtra("TaskID", taskID);
			intent.putExtra("setJustNextRun", false);
			startService(intent);
			finish();
			return true;
		}
	}

	
}
