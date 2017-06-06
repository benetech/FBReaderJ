package org.geometerplus.android.fbreader.network.bookshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.widget.Button;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.AddToReadingListDialogActivity;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

/**
 * Shows the details of a selected book. Will also show a download option if applicable.
 */
public class BookDetailActivity extends Activity {

    protected Bookshare_Metadata_Bean metadata_bean;
    protected Button btnReadingList;

    protected final int START_READINGLIST_DIALOG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
            if(ZLApplication.Instance() != null){
                ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.SILENT_STARTUP);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((ZLAndroidApplication) getApplication()).startTracker(this);
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
