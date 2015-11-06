package org.geometerplus.android.fbreader;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.DaisyPageHandler;
import org.geometerplus.android.fbreader.benetech.DefaultPageHandler;
import org.geometerplus.android.fbreader.benetech.PageHandler;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import java.util.LinkedHashMap;

/**
 * Created by animal@martus.org on 11/5/15.
 */
public class BookNavigationTabPage extends Fragment {

    private EditText searchTermEditText;
    private Activity parentActivity;
    private PageHandler pageHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookshare_dialog, container, false);
        parentActivity = getActivity();
        searchTermEditText = (EditText)view.findViewById(R.id.bookshare_dialog_search_edit_txt);

        TextView dialog_search_title = (TextView) view.findViewById(R.id.bookshare_dialog_search_txt);

        Button dialog_ok = (Button)view.findViewById(R.id.bookshare_dialog_btn_ok);
        Button dialog_cancel = (Button)view.findViewById(R.id.bookshare_dialog_btn_cancel);

        dialog_search_title.setText(getResources().getString(R.string.navigate_dialog_label));

        searchTermEditText.setOnKeyListener(new OnKeyHandler());
        dialog_ok.setOnClickListener(new DialogOkKeyHandler());
        dialog_cancel.setOnClickListener(new DialogCancelKeyHandler());
        searchTermEditText.requestFocus();

        return view;
    }

    @Override
    public void onStart() {
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

        TextView dialog_example_text = (TextView) getView().findViewById(R.id.bookshare_dialog_search_example);
        dialog_example_text.setText(getResources().getString(R.string.navigate_dialog_example, examplePrefix, currentPage, lastPage, nonNumeric));
        searchTermEditText.setContentDescription(getResources().getString(R.string.navigate_dialog_label) + " " + getResources().getString(R.string.navigate_dialog_example, examplePrefix, currentPage, lastPage, nonNumeric));
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

    private void confirmAndClose(String msg) {
        final VoiceableDialog dialog = new VoiceableDialog(getActivity());
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                getActivity().finish();
            }
        });
        dialog.popup(msg, 2000);
    }

    private class DialogOkKeyHandler implements View.OnClickListener {
        public void onClick(View v) {
            String page =  searchTermEditText.getText().toString().trim();
            gotoPage(page);
        }
    }

    private class DialogCancelKeyHandler implements View.OnClickListener {
        public void onClick(View v){
            parentActivity.finish();
        }
    }

    private class OnKeyHandler implements View.OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String page = searchTermEditText.getText().toString().trim();
                gotoPage(page);

                return true;
            }

            return false;
        }
    }
}
