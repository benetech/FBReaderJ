package org.geometerplus.android.fbreader;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.LabelsListAdapter;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by animal@martus.org on 11/5/15.
 */
public class BookNavigationBookmarkTab extends Fragment implements MenuItem.OnMenuItemClickListener {

    private static final int OPEN_ITEM_ID = 0;
    private static final int DELETE_ITEM_ID = 2;

    private List<Bookmark> allBooksBookmarks;
    private final List<Bookmark> myThisBookBookmarks = new LinkedList<>();

    private ListView currentBookBookmarkListView;

    private final ZLResource myResource = ZLResource.resource("bookmarksView");
    private final ZLStringOption myBookmarkSearchPatternOption = new ZLStringOption("BookmarkSearch", "Pattern", "");

    private AccessibilityManager accessibilityManager;
    private Dialog contextMenuDialog;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        accessibilityManager = (AccessibilityManager) getActivity().getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

        View view = inflater.inflate(R.layout.book_navigation_tab_bookmarks_layout, container, false);
        allBooksBookmarks = Bookmark.bookmarks();
        Collections.sort(allBooksBookmarks, new Bookmark.ByTimeComparator());
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();

        if (fbreader.Model != null) {
            final long bookId = fbreader.Model.Book.getId();
            for (Bookmark bookmark : allBooksBookmarks) {
                if (bookmark.getBookId() == bookId) {
                    myThisBookBookmarks.add(bookmark);
                }
            }

            final Book currentBook = Library.getRecentBook();
            currentBookBookmarkListView = (ListView) view.findViewById(R.id.current_book_bookmarks_list);
            new BookmarksAdapter(currentBookBookmarkListView, myThisBookBookmarks, true);
        } else {
            view.findViewById(R.id.current_book_bookmarks_list).setVisibility(View.GONE);
        }

        contextMenuDialog = new Dialog(getActivity());
        contextMenuDialog.setContentView(R.layout.accessible_long_press_dialog);
        listView = (ListView) contextMenuDialog.findViewById(R.id.accessible_list);

        return view;
    }

    private void addBookmark() {
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        final Bookmark bookmark = fbreader.addBookmark(20, true);
        if (bookmark != null) {
            myThisBookBookmarks.add(0, bookmark);
            allBooksBookmarks.add(0, bookmark);
            invalidateAllViews();

            final VoiceableDialog finishedDialog = new VoiceableDialog(getActivity());
            String msg = getResources().getString(R.string.bookmark_added, bookmark.getPageNumber());
            finishedDialog.popup(msg, 3000);
        }
    }

    private void invalidateAllViews() {
        currentBookBookmarkListView.invalidateViews();
        currentBookBookmarkListView.requestLayout();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                return onSearchRequested();
            default:
                return true;
        }
    }

    public boolean onSearchRequested() {
        getActivity().startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
        return true;
    }

    private void gotoBookmark(Bookmark bookmark) {
        bookmark.onOpen();
        final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
        final long bookId = bookmark.getBookId();
        if ((fbreader.Model == null) || (fbreader.Model.Book.getId() != bookId)) {
            final Book book = Book.getById(bookId);
            if (book != null) {

                Library.addBookToRecentList(book);
                if (accessibilityManager.isEnabled()) {
                    fbreader.openBook(book, bookmark, getActivity());
                } else {
                    getActivity().finish();
                    fbreader.openBook(book, bookmark);
                }
            } else {
                UIUtil.showErrorMessage(getActivity(), "cannotOpenBook");
            }
        } else {
            getActivity().finish();
            fbreader.gotoBookmark(bookmark);
            if (accessibilityManager.isEnabled()) {
                ZLApplication.Instance().doAction(ActionCode.SPEAK);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
        BookmarksAdapter adapter = (BookmarksAdapter) currentBookBookmarkListView.getAdapter();
        final Bookmark bookmark = adapter.getItem(position);
        switch (item.getItemId()) {
            case OPEN_ITEM_ID:
                gotoBookmark(bookmark);
                return true;
            case DELETE_ITEM_ID:
                bookmark.delete();
                myThisBookBookmarks.remove(bookmark);
                allBooksBookmarks.remove(bookmark);
                invalidateAllViews();
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
        private final List<Bookmark> myBookmarks;
        private final boolean myCurrentBook;

        public BookmarksAdapter(ListView listView, List<Bookmark> bookmarks, boolean currentBook) {
            myBookmarks = bookmarks;
            myCurrentBook = currentBook;
            listView.setAdapter(this);
            listView.setOnItemClickListener(this);
            listView.setOnCreateContextMenuListener(this);
        }

        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
            final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
            if (getItem(position) != null) {
                menu.setHeaderTitle(getItem(position).getText());
                final ZLResource resource = ZLResource.resource("bookmarksView");
                menu.add(0, OPEN_ITEM_ID, 0, resource.getResource("open").getValue());
                menu.add(0, DELETE_ITEM_ID, 0, resource.getResource("delete").getValue());
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = (convertView != null) ? convertView : LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
            final ImageView imageView = (ImageView)view.findViewById(R.id.bookmark_item_icon);
            final TextView textView = (TextView)view.findViewById(R.id.bookmark_item_text);
            final TextView bookTitleView = (TextView)view.findViewById(R.id.bookmark_item_booktitle);

            final Bookmark bookmark = getItem(position);
            if (bookmark == null) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_list_plus);
                textView.setText(ZLResource.resource("bookmarksView").getResource("new").getValue());
                bookTitleView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.GONE);
                textView.setText(bookmark.getText());
                if (myCurrentBook) {
                    bookTitleView.setVisibility(View.GONE);
                } else {
                    bookTitleView.setVisibility(View.VISIBLE);
                    bookTitleView.setText(bookmark.getBookTitle());
                }
            }
            return view;
        }

        public final boolean areAllItemsEnabled() {
            return true;
        }

        public final boolean isEnabled(int position) {
            return true;
        }

        public final long getItemId(int position) {
            return position;
        }

        public final Bookmark getItem(int position) {
            if (myCurrentBook) {
                --position;
            }
            return (position >= 0) ? myBookmarks.get(position) : null;
        }

        public final int getCount() {
            return myCurrentBook ? myBookmarks.size() + 1 : myBookmarks.size();
        }

        public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Bookmark bookmark = getItem(position);
            if (bookmark != null) {
                if (!accessibilityManager.isEnabled()) {
                    gotoBookmark(bookmark);
                } else {
                    // show 'long press' context menu to open or remove a bookmark
                    ArrayList<Object> listItems = new ArrayList<Object>();
                    final ZLResource resource = ZLResource.resource("bookmarksView");
                    listItems.add(resource.getResource("open").getValue());
                    listItems.add(resource.getResource("delete").getValue());
                    LabelsListAdapter adapter = new LabelsListAdapter(listItems, getActivity());
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new MenuClickListener(bookmark));
                    contextMenuDialog.show();
                }
            } else {
                addBookmark();
            }
        }

        private class MenuClickListener implements AdapterView.OnItemClickListener {
            private Bookmark bookmark;

            private MenuClickListener(Bookmark bookmark) {
                this.bookmark = bookmark;
            }

            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                contextMenuDialog.hide();

                switch (position) {
                    case 0:
                        gotoBookmark(bookmark);
                        break;
                    case 1:
                        bookmark.delete();
                        myThisBookBookmarks.remove(bookmark);
                        allBooksBookmarks.remove(bookmark);
                        invalidateAllViews();
                        break;
                }
            }
        }
    }
}
