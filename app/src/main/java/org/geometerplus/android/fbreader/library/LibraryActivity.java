/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.view.*;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

import org.accessibility.VoiceableDialog;
import org.geometerplus.android.fbreader.benetech.Analytics;
import org.geometerplus.android.fbreader.benetech.LabelsListAdapter;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.benetech.android.R;

import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, Library.ChangeListener {
	static volatile boolean ourToBeKilled = false;

	public static final String SELECTED_BOOK_PATH_KEY = "SelectedBookPath";

	private BooksDatabase myDatabase;
	private Library myLibrary;

	private Book mySelectedBook;
    private Dialog dialog;
    private static final ZLResource resource = Library.resource();
    ListView list;
    Activity myActivity;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		myDatabase = SQLiteBooksDatabase.Instance();
		if (myDatabase == null) {
			myDatabase = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (myLibrary == null) {
			myLibrary = Library.Instance();
			myLibrary.addChangeListener(this);
			myLibrary.startBuild();
		}

		final String selectedBookPath = getIntent().getStringExtra(SELECTED_BOOK_PATH_KEY);
		mySelectedBook = null;
		if (selectedBookPath != null) {
			final ZLFile file = ZLFile.createFileByPath(selectedBookPath);
			if (file != null) {
				mySelectedBook = Book.getByFile(file);
			}
		}

		new LibraryTreeAdapter(this);
        myActivity = this;

		init(getIntent());

        //commenting out to prevent soft keyboard when using Eyes-Free keyboard
		//getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.accessible_long_press_dialog);
        list = (ListView) dialog.findViewById(R.id.accessible_list);

	}

	@Override
	protected FBTree getTreeByKey(FBTree.Key key) {
		return key != null ? myLibrary.getLibraryTree(key) : myLibrary.getRootTree();
	}

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

	@Override
	public void onPause() {
		super.onPause();
		ourToBeKilled = true;
	}

	@Override
	protected void onDestroy() {
		myLibrary.removeChangeListener(this);
		myLibrary = null;
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
		final Book book = tree.getBook();
        mySelectedBook = book;
		if (book != null) {
            if (!accessibilityManager.isEnabled()) {
			    showBookInfo(book);
            } else {

                ArrayList<Object> listItems = new ArrayList<Object>();
                listItems.add(resource.getResource("openBook").getValue());
                if (myLibrary.isBookInFavorites(book)) {
                    listItems.add(resource.getResource("removeFromFavorites").getValue());
                } else {
                    listItems.add(resource.getResource("addToFavorites").getValue());
                }
                if ((myLibrary.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
                    listItems.add(resource.getResource("deleteBook").getValue());
                }

                LabelsListAdapter adapter = new LabelsListAdapter(listItems, this);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new MenuClickListener(book));

                dialog.show();
            }
		} else {
            String id = tree.getUniqueKey().Id;
            if (id.equals(Library.ROOT_SEARCH))  {
                onSearchRequested();
            } else {
			    openTree(tree);
            }
		}
	}

	//
	// show BookInfoActivity
	//
	private static final int BOOK_INFO_REQUEST = 1;

	protected void showBookInfo(Book book) {
		startActivityForResult(
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY, book.File.getPath()),
			BOOK_INFO_REQUEST
		);
	}

	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == BOOK_INFO_REQUEST && intent != null) {
			final String path = intent.getStringExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY);
			final Book book = Book.getByFile(ZLFile.createFileByPath(path));
			myLibrary.refreshBookInfo(book);
			getListView().invalidateViews();
		} else {
			super.onActivityResult(requestCode, returnCode, intent);
		}
	} 

	//
	// Search
	//
	static final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	private void openSearchResults() {
		final FBTree tree = myLibrary.getRootTree().getSubTree(Library.ROOT_FOUND);
		if (tree != null) {
			openTree(tree);
		}
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	//
	// Context menu
	//
	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 2;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 3;
	private static final int DELETE_BOOK_ITEM_ID = 4;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book); 
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = Library.resource();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, resource.getResource("showBookInfo").getValue());
		if (myLibrary.isBookInFavorites(book)) {
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, resource.getResource("addToFavorites").getValue());
		}
		if ((myLibrary.getRemoveBookMode(book) & Library.REMOVE_FROM_DISK) != 0) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	private boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case OPEN_BOOK_ITEM_ID:
				openBook(book);
				return true;
			case SHOW_BOOK_INFO_ITEM_ID:
				showBookInfo(book);
				return true;
			case ADD_TO_FAVORITES_ITEM_ID:
				myLibrary.addBookToFavorites(book);
				return true;
			case REMOVE_FROM_FAVORITES_ITEM_ID:
				myLibrary.removeBookFromFavorites(book);
				getListView().invalidateViews();
				return true;
			case DELETE_BOOK_ITEM_ID:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}
    
    private void notifyFavoritesAction(int messageId) {
        final VoiceableDialog finishedDialog = new VoiceableDialog(myActivity);
        String message = getResources().getString(messageId);
        finishedDialog.popup(message, 3200);
    }

	private void openBook(Book book) {
		startActivity(
			new Intent(getApplicationContext(), FBReader.class)
				.setAction(Intent.ACTION_VIEW)
				.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		);
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			default:
				return true;
		}
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;
		private final int myMode;

		BookDeleter(Book book, int removeMode) {
			myBook = book;
			myMode = removeMode;
		}

		public void onClick(DialogInterface dialog, int which) {
			deleteBook(myBook, myMode);
		}
	}

	private void tryToDeleteBook(Book book) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
			.setTitle(book.getTitle())
			.setMessage(boxResource.getResource("message").getValue())
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book, Library.REMOVE_FROM_DISK))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void deleteBook(Book book, int mode) {
		myLibrary.removeBook(book, mode);

		if (getCurrentTree() instanceof FileTree) {
			getListAdapter().remove(new FileTree((FileTree)getCurrentTree(), book.File));
		} else {
			getListAdapter().replaceAll(getCurrentTree().subTrees());
		}
		getListView().invalidateViews();
	}

	public void onLibraryChanged(final Code code) {
		runOnUiThread(new Runnable() {
			public void run() {
				switch (code) {
					default:
                        getListAdapter().replaceAll(getCurrentTree().subTrees());
						break;
					case StatusChanged:
						setProgressBarIndeterminateVisibility(!myLibrary.isUpToDate());
						break;
					case Found:
						openSearchResults();
						break;
					case NotFound:
						UIUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
						break;
				}
			}
		});
    }


    private class MenuClickListener implements AdapterView.OnItemClickListener {
        private Book book;

        private MenuClickListener(Book book) {
            this.book = book;
        }

        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            dialog.hide();
            switch (position) {
                case OPEN_BOOK_ITEM_ID:
                    EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BOOK_MENU,
                        Analytics.EVENT_LABEL_OPEN_BOOK, null);
                    openBook(book);
                    break;
                case 1:
                    if (myLibrary.isBookInFavorites(book)) {
                        myLibrary.removeBookFromFavorites(book);
                        notifyFavoritesAction(R.string.library_removed_from_favorites);
                        getListView().invalidateViews();
                        EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BOOK_MENU,
                            Analytics.EVENT_LABEL_FAVORITES_REMOVE, null);
                    } else {
                        myLibrary.addBookToFavorites(book);
                        notifyFavoritesAction(R.string.library_added_to_favorites);
                        EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BOOK_MENU,
                            Analytics.EVENT_LABEL_FAVORITES_ADD, null);
                    }
                    break;
                case 2:
                    tryToAccessiblyDeleteBook(book);
                    EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BOOK_MENU,
                        Analytics.EVENT_LABEL_DELETE_BOOK, null);
                    break;
            }
        }
    }

    private void tryToAccessiblyDeleteBook(final Book book) {

        final ZLResource dialogResource = ZLResource.resource("dialog");
        final ZLResource boxResource = dialogResource.getResource("deleteBookBox");

        final Dialog confirmDialog = new Dialog(myActivity);
        confirmDialog.setTitle(getResources().getString(R.string.accessible_alert_title));
        confirmDialog.setContentView(R.layout.accessible_alert_dialog);
        TextView confirmation = (TextView)confirmDialog.findViewById(R.id.bookshare_confirmation_message);
        confirmation.setText(boxResource.getResource("message").getValue());
        Button yesButton = (Button)confirmDialog.findViewById(R.id.bookshare_dialog_btn_yes);
        Button noButton = (Button) confirmDialog.findViewById(R.id.bookshare_dialog_btn_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                deleteBook(book, Library.REMOVE_FROM_DISK);
                confirmDialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
            }
        });

        confirmDialog.show();
    }
}
