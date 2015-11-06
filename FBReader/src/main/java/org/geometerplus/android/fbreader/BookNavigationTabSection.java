package org.geometerplus.android.fbreader;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.benetech.LabelsListAdapter;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 11/5/15.
 */
public class BookNavigationTabSection extends Fragment {

    private static final int PROCESS_TREE_ITEM_ID = 0;
    private static final int READ_BOOK_ITEM_ID = 1;

    private TOCAdapter myAdapter;
    private ZLTree<?> mySelectedItem;

    private Resources resources;
    private Dialog dialog;
    private ListView listView;
    private ListView tocListView;
    private Activity myActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resources = getResources();
        View view = inflater.inflate(R.layout.book_tab_toc_layout, container, false);
        final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
        final TOCTree root = fbreader.Model.TOCTree;
        tocListView = (ListView) view.findViewById(R.id.current_book_toc_list);
        myAdapter = new TOCAdapter(root);
        final ZLTextWordCursor cursor = fbreader.BookTextView.getStartCursor();
        int index = cursor.getParagraphIndex();
        if (cursor.isEndOfParagraph()) {
            ++index;
        }
        TOCTree treeToSelect = fbreader.getCurrentTOCElement();
        myAdapter.selectItem(treeToSelect);
        mySelectedItem = treeToSelect;

        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.accessible_long_press_dialog);
        listView = (ListView) dialog.findViewById(R.id.accessible_list);
        myActivity = getActivity();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ZLAndroidApplication) getActivity().getApplication()).startTracker(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ZLAndroidApplication) getActivity().getApplication()).stopTracker(getActivity());
    }

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

    private final class TOCAdapter extends ZLTreeAdapter {

        public TOCAdapter(TOCTree root) {
            super(tocListView, root);
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
            final View view = (convertView != null) ? convertView : LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
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

        private void openBookText(TOCTree tree) {
            final TOCTree.Reference reference = tree.getReference();
            if (reference != null) {
                getActivity().finish();
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
                ArrayList<Object> listItems = new ArrayList<Object>();
                listItems.add(getResources().getString(R.string.toc_goto_heading));
                if (myAdapter.isOpen(tree))  {
                    listItems.add(getResources().getString(R.string.toc_close_subheadings));
                } else {
                    listItems.add(getResources().getString(R.string.toc_view_subheadings));
                }
                LabelsListAdapter adapter = new LabelsListAdapter(listItems, myActivity);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new MenuClickListener(tree));
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
