package org.geometerplus.android.fbreader.network.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.AddToReadingListDialogActivity;
import org.geometerplus.android.fbreader.library.AbstractSQLiteBooksDatabase;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.fbreader.library.ReadingList;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import java.util.ArrayList;

/**
 * Shows the details of a selected book. Will also show a download option if applicable.
 */
public class BookDetailActivity extends Activity {

    protected Bookshare_Metadata_Bean metadata_bean;
    protected Button btnReadingList;
    boolean hasAvailableReadingLists = false;

    protected final int START_READINGLIST_DIALOG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private void determineAvailableReadingLists() {
        if(hasAvailableReadingLists) return; //if we already determined user has available RLs no need to recalculate. If he didnt last time it's worth to check if he has now
        SQLiteBooksDatabase database = (SQLiteBooksDatabase) SQLiteBooksDatabase.Instance();
        try {
            ArrayList<ReadingList> readingLists = database.getAllReadingLists();
            for (int index = 0; index < readingLists.size(); ++index) {
                ReadingList readingList = readingLists.get(index);
                if(readingList.allowsAdditions()){
                    hasAvailableReadingLists = true;
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == START_READINGLIST_DIALOG) {
            if(resultCode == AddToReadingListDialogActivity.RESULT_CODE_SUCCESS){
                String listName = "";
                if(data != null){
                    listName = data.getStringExtra(AddToReadingListDialogActivity.EXTRA_READINGLIST_NAME);
                }
                btnReadingList.setText(
                        String.format("%s %s", getString(R.string.added_to_readinglist_success), listName));

            }
            else if(resultCode == AddToReadingListDialogActivity.RESULT_CODE_FAIL){
                new AlertDialog.Builder(this)
                        .setTitle(R.string.added_to_readinglist_fail_title)
                        .setMessage(R.string.added_to_readinglist_fail_message)
                        .setPositiveButton(R.string.accept, null)
                        .show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((ZLAndroidApplication) getApplication()).startTracker(this);
        determineAvailableReadingLists();
        if(btnReadingList != null){
            btnReadingList.setVisibility(hasAvailableReadingLists? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ZLAndroidApplication) getApplication()).stopTracker(this);
    }

    protected void showReadingListsDialog(String bookshareId){
        Intent intent = new Intent(this, AddToReadingListDialogActivity.class);
        intent.putExtra(AddToReadingListDialogActivity.EXTRA_BOOK_ID, bookshareId);
        startActivityForResult(intent, START_READINGLIST_DIALOG);
    }
}
