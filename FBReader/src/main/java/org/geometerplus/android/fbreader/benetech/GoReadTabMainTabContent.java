package org.geometerplus.android.fbreader.benetech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by animal@martus.org on 4/26/16.
 */
public class GoReadTabMainTabContent extends ListFragment {

    private static final String EXTENSION_OPF = "opf";
    private static final String EXTENSION_EPUB = "epub";
    private static final String[] BOOK_FILE_EXTENSIONS_TO_FILTER_BY = {EXTENSION_EPUB, EXTENSION_OPF};

    private ArrayList<AbstractTitleListRowItem> downloadedBooksList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadedBooksList = new ArrayList<>();
        try {
            fillListAdapter();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void fillListAdapter() throws Exception {
        String value = Paths.BooksDirectoryOption().getValue();
        File downloadDir = new File(value);
        if (!downloadDir.exists())
            throw new Exception("Download directory does not exist");

        Collection<File> bookFilesFound = FileUtils.listFiles(downloadDir, BOOK_FILE_EXTENSIONS_TO_FILTER_BY, true);
        for (File bookFile : bookFilesFound) {
            ZLFile zlFile = ZLFile.createFileByPath(bookFile.getAbsolutePath());
            final Book book = Book.getByFile(zlFile);
            if (book != null)
                downloadedBooksList.add(new DownloadedTitleListRowItem(book));
            else
                Log.e(this.getClass().getSimpleName(), "Book file exists but could not create Book object from it");
        }

        sortListItems();
        setListAdapter(new BookListAdapter(getActivity(), downloadedBooksList));
    }

    private void sortListItems() {
        Collections.sort(downloadedBooksList);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AbstractTitleListRowItem clickedRowItem = downloadedBooksList.get(position);
        Intent intent = new Intent(getActivity().getApplicationContext(), BookInfoActivity.class);
        intent.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, clickedRowItem.getBookFilePath());
        startActivity(intent);
    }
}
