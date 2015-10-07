package org.geometerplus.android.fbreader.benetech;

import java.util.LinkedHashMap;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author roms
 */
public class AccessibleNavigateActivity extends Activity {
    
    private EditText searchTermEditText;
    private Activity parentActivity;
    private PageHandler pageHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = this;

        setContentView(R.layout.bookshare_dialog);

        setTitle(getResources().getString(R.string.navigate_dialog_title));
        searchTermEditText = (EditText)findViewById(R.id.bookshare_dialog_search_edit_txt);
        
        
        
        TextView dialog_search_title = (TextView) findViewById(R.id.bookshare_dialog_search_txt);

        Button dialog_ok = (Button)findViewById(R.id.bookshare_dialog_btn_ok);
        Button dialog_cancel = (Button)findViewById(R.id.bookshare_dialog_btn_cancel);
        
        dialog_search_title.setText(getResources().getString(R.string.navigate_dialog_label));
        
        searchTermEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String page =  searchTermEditText.getText().toString().trim();
                    gotoPage(page);
                    return true;
                }
                return false;
            }
        });
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String page =  searchTermEditText.getText().toString().trim();
                gotoPage(page);
            }
        });
        dialog_cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                parentActivity.finish();
            }
        });
        searchTermEditText.requestFocus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        LinkedHashMap<String, Integer> pageMap = fbreader.getDaisyPageMap();
        String examplePrefix = "";
        if (null != pageMap && pageMap.size() > 1) {
            pageHandler = new DaisyPageHandler(fbreader, pageMap);
            examplePrefix = " DAISY ";
        } else {
            pageHandler = new DefaultPageHandler(fbreader);
        }
        
        String nonNumeric = "";
        if (pageHandler.isNumeric()) {
            searchTermEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            searchTermEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            nonNumeric = getResources().getString(R.string.navigate_dialog_example_non_numeric);
        }
        String currentPage = pageHandler.getCurrentPage();
        String lastPage = pageHandler.getLastPage();
        

        TextView dialog_example_text = (TextView) findViewById(R.id.bookshare_dialog_search_example);
        dialog_example_text.setText(getResources().getString(R.string.navigate_dialog_example, examplePrefix, currentPage, lastPage, nonNumeric));
        searchTermEditText.setContentDescription(getResources().getString(R.string.navigate_dialog_label) + " " +
            getResources().getString(R.string.navigate_dialog_example, examplePrefix, currentPage, lastPage, nonNumeric));
    }
    
    private void gotoPage(String page) {
        String message;
        if (pageHandler.gotoPage(page)) {
            message =  getResources().getString(R.string.page_navigated, page);
        } else {
            message = "page not found!";
        }
        confirmAndClose(message);
    }
    
    /*
     * Display logged out confirmation and close the bookshare menu screen
     */
    private void confirmAndClose(String msg) {
        final VoiceableDialog dialog = new VoiceableDialog(this);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.popup(msg, 2000);
    }
    
}
