package org.geometerplus.fbreader.fbreader;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.benetech.android.R;
import org.bookshare.net.BookshareHttpOauth2Client;
import org.geometerplus.android.fbreader.FBAndroidAction;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.library.ReadingList;

import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by animal@martus.org on 3/22/16.
 */
public class SyncWithBookshareAction extends FBAndroidAction {

    private static final String LOG_TAG = "SyncWithBookshareAction";
    private static final String FIRST_READING_LIST_NAME = "Bookshare Reading List";

    private CustomProgressDialog progressDialog;

    public SyncWithBookshareAction(FBReader baseActivity, FBReaderApp fbreader) {
        super(baseActivity, fbreader);
    }

    @Override
    protected void run(Object ... params) {
        SQLiteBooksDatabase database = (SQLiteBooksDatabase) SQLiteBooksDatabase.Instance();
        ReadingList emptyReadingList = database.insertEmptyReadingList(FIRST_READING_LIST_NAME);
        emptyReadingList.setReadingListName(FIRST_READING_LIST_NAME);
        try {
            emptyReadingList.save();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        progressDialog = new CustomProgressDialog(getBaseActivity());
        progressDialog.setTitle(getBaseActivity().getString(R.string.title_sync_bookshare_progress_dialog));
        progressDialog.show();

        SynchReadingLists syncTask = new SynchReadingLists();
        syncTask.execute();
    }

    private void onCompleteSyncBookshareReadingLists() {
        TextView textView = (TextView) progressDialog.findViewById(R.id.progress_dialog_sync_message);
        textView.setText(getBaseActivity().getString(R.string.message_sync_bookshare_reading_lists_complete_progress_dialog));

        Button doneButton = (Button) progressDialog.findViewById(R.id.done_button);
        doneButton.setEnabled(true);
    }

    private void hideGreenCheckMarkImageView() {
        getGreenCheckMarkImageView().setVisibility(View.GONE);
        getSyncProgressBar().setVisibility(View.VISIBLE);
    }

    private void showGreenCheckMarkImageView() {
        getGreenCheckMarkImageView().setVisibility(View.VISIBLE);
        getSyncProgressBar().setVisibility(View.GONE);
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

    private class SynchReadingLists extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
//                BookshareHttpOauth2Client client = new BookshareHttpOauth2Client();
//                ArrayList<ReadingList> readingLists = client.getReadingLists();
                //FIXME urgent - under construction.  Need to store the reading lists in DB.
                //Committing now since its a good stopping point
                System.out.println("-=---------------------------------------ReadingList count s= "  /*+ readingLists.size()*/);
            }  catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            showGreenCheckMarkImageView();
            onCompleteSyncBookshareReadingLists();

        }
    }

    protected class DoneHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            progressDialog.dismiss();
        }
    }
}
