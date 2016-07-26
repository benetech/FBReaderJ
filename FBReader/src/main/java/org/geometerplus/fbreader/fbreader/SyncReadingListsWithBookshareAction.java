package org.geometerplus.fbreader.fbreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.FBAndroidAction;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.benetech.AsyncResponse;
import org.geometerplus.android.fbreader.benetech.DownLoadReadingListsTask;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Webservice_Login;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction.SyncType.FIRST_STARTUP;

/**
 * Created by animal@martus.org on 3/22/16.
 */
public class SyncReadingListsWithBookshareAction extends FBAndroidAction implements AsyncResponse<JSONArray> {

    private static final String LOG_TAG = "SyncWithBookshareAction";
    private static final int SECONDS_TO_PAUSE = 15;

    public enum SyncType {
        FIRST_STARTUP,
        SILENT_STARTUP,
        USER_ACTIVATED
    }

    private CustomProgressDialog progressDialog;

    public SyncReadingListsWithBookshareAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object ... params) {
        SyncType type = FIRST_STARTUP;

        SyncReadingListsWithBookshareActionObserver.getInstance().setRunningAction(this);
        if(params.length > 0 && params[0] instanceof SyncType){
            type = (SyncType)params[0];
        }
        displayProgressDialog(type, getBaseActivity());

        SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getBaseActivity());
        String username = login_preference.getString(Bookshare_Webservice_Login.USER, "");
        String password = login_preference.getString(Bookshare_Webservice_Login.PASSWORD, "");
        DownLoadReadingListsTask task = new DownLoadReadingListsTask(this, username, password);
        task.execute();

        displayCompleteDialogWithDelay();
    }

    @Override
    public void processFinish(JSONArray readingLists) {
        try {
            insertReadingListsIntoDatabase(readingLists);
        } catch (Exception e) {
            Toast.makeText(getBaseActivity().getBaseContext(), "Error trying to sync reading lists", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private void insertReadingListsIntoDatabase(JSONArray readingLists) throws Exception{
        SQLiteBooksDatabase database = (SQLiteBooksDatabase) SQLiteBooksDatabase.Instance();
        database.clearReadingLists();
        for (int index = 0; index < readingLists.length(); ++index) {
            JSONObject readingListJson = readingLists.getJSONObject(index);
            database.insertReadingList(readingListJson);
        }
    }

    private void displayCompleteDialogWithDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SyncReadingListsWithBookshareActionObserver.getInstance().setRunningAction(null);
                if(progressDialog != null) {
                    showGreenCheckMarkImageView();
                    syncCompleted();
                }
            }
        }, SECONDS_TO_PAUSE * 1000);
    }

    private void syncCompleted() {
        if(progressDialog != null) {

            progressDialog.setTitle(getBaseActivity().getString(R.string.title_sync_bookshare_complete_progress_dialog));
            TextView textView = (TextView) progressDialog.findViewById(R.id.progress_dialog_sync_message);
            textView.setText(getBaseActivity().getString(R.string.message_sync_bookshare_reading_lists_complete_progress_dialog));

            Button doneButton = (Button) progressDialog.findViewById(R.id.done_button);
            doneButton.setEnabled(true);
        }
    }

    private void hideGreenCheckMarkImageView() {
        if(progressDialog != null) {
            getGreenCheckMarkImageView().setVisibility(View.GONE);
            getSyncProgressBar().setVisibility(View.VISIBLE);
        }
    }

    private void showGreenCheckMarkImageView() {
        if(progressDialog != null) {
            getGreenCheckMarkImageView().setVisibility(View.VISIBLE);
            getSyncProgressBar().setVisibility(View.GONE);
        }
    }

    private ImageView getGreenCheckMarkImageView() {
        return (ImageView) progressDialog.findViewById(R.id.green_check_mark);
    }

    private ProgressBar getSyncProgressBar() {
        return (ProgressBar) progressDialog.findViewById(R.id.sync_progress_bar);
    }

    private class CustomProgressDialog extends AlertDialog {
        public CustomProgressDialog(Context context) {
            super(context);

            setCancelable(false);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.sync_bookshare_progress_dialog);
            Button doneButton = (Button) findViewById(R.id.done_button);
            doneButton.setOnClickListener(new DoneHandler());
            doneButton.setEnabled(false);

            hideGreenCheckMarkImageView();
        }
    }

    protected class DoneHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            progressDialog.dismiss();
        }
    }

    public void displayProgressDialog(SyncType type, Activity parent){
        if(progressDialog == null
                || !progressDialog.isShowing()) {
            switch (type) {
                case FIRST_STARTUP:
                    progressDialog = new CustomProgressDialog(parent);
                    progressDialog.setTitle(getBaseActivity().getString(R.string.title_sync_bookshare_progress_dialog));
                    progressDialog.show();
                    break;
                case SILENT_STARTUP:
                    progressDialog = null;
                    break;
                case USER_ACTIVATED:
                    progressDialog = new CustomProgressDialog(parent);
                    progressDialog.setTitle("");
                    progressDialog.show();
                    break;
            }
        }
    }
}
