/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.app.ListActivity;
import com.google.analytics.tracking.android.EasyTracker;

import org.geometerplus.android.fbreader.benetech.LabelsListAdapter;
import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;

import org.benetech.android.R;

import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class TOCActivity extends ListActivity {
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;

    public static final int BACK_PRESSED = 10;
    private Resources resources;
    private Dialog dialog;
    ListView list;
    Activity myActivity;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        resources = getApplicationContext().getResources();

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		final TOCTree root = fbreader.Model.TOCTree;
		myAdapter = new TOCAdapter(root);
		final ZLTextWordCursor cursor = fbreader.BookTextView.getStartCursor();
		int index = cursor.getParagraphIndex();	
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();
		myAdapter.selectItem(treeToSelect);
		mySelectedItem = treeToSelect;

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.accessible_long_press_dialog);
        list = (ListView) dialog.findViewById(R.id.accessible_list);
        myActivity = this;
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

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final TOCTree tree = (TOCTree)myAdapter.getItem(position);
		switch (item.getItemId()) {
			case PROCESS_TREE_ITEM_ID:
				myAdapter.runTreeItem(tree);
				return true;
			case READ_BOOK_ITEM_ID:
				myAdapter.openBookText(tree);
				return true;
		}
		return super.onContextItemSelected(item);
	}

    @Override
    public void onBackPressed() {
        setResult(BACK_PRESSED);
        super.onBackPressed();
    }

    private final class TOCAdapter extends ZLTreeAdapter {

		TOCAdapter(TOCTree root) {
			super(getListView(), root);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final TOCTree tree = (TOCTree)getItem(position);
			if (tree.hasChildren()) {
				menu.setHeaderTitle(tree.getText());
				final ZLResource resource = ZLResource.resource("tocView");
				menu.add(0, PROCESS_TREE_ITEM_ID, 0, resource.getResource(isOpen(tree) ? "collapseTree" : "expandTree").getValue());
				menu.add(0, READ_BOOK_ITEM_ID, 0, resource.getResource("readText").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree)getItem(position);
			view.setBackgroundColor((tree == mySelectedItem) ? 0xff808080 : 0);
            StringBuilder subHeadings = new StringBuilder("");
            if (tree.hasChildren()) {
                subHeadings = subHeadings.append(resources.getString(R.string.subheading, tree.subTrees().size()));
            }
			setIcon((ImageView)view.findViewById(R.id.toc_tree_item_icon), tree);
			((TextView)view.findViewById(R.id.toc_tree_item_text)).setText(tree.getText() + subHeadings);
			return view;
		}

		void openBookText(TOCTree tree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				finish();
				final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree)tree);
			return true;
		}

        public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ZLTree<?> tree = getItem(position);
            if (!tree.hasChildren()) {
                runTreeItem(getItem(position));
            } else {
                // popup window to ask for subheadings or heading
                ArrayList<Object> listItems = new ArrayList<Object>();
                listItems.add(getResources().getString(R.string.toc_goto_heading));
                if (myAdapter.isOpen(tree))  {
                    listItems.add(getResources().getString(R.string.toc_close_subheadings));
                } else {
                    listItems.add(getResources().getString(R.string.toc_view_subheadings));
                }
                LabelsListAdapter adapter = new LabelsListAdapter(listItems, myActivity);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new MenuClickListener(tree));
                TextView header = (TextView)dialog.findViewById(R.id.accessible_list_heading);
                header.requestFocus();
                dialog.show();
            }

        }

        /*
         * Performs action based on item clicked in view sub heading or go to heading popup
         */
        private class MenuClickListener implements AdapterView.OnItemClickListener {
            private ZLTree<?> tree;

            private MenuClickListener(ZLTree<?> tree) {
                this.tree = tree;
            }

            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                dialog.hide();

                switch (position) {
                    case 0:
                        openBookText((TOCTree)tree);
                        break;
                    case 1:
                        runTreeItem(tree);
                        break;
                }
            }
        }
	}
}
