package org.geometerplus.android.fbreader.benetech;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.image.ZLImage;

import java.util.List;

/**
 * Created by animal@martus.org on 4/28/16.
 */
public class BookListAdapter  extends ArrayAdapter<AbstractTitleListRowItem> {

    private Activity activity;

    public BookListAdapter(Activity activityToUse, List<AbstractTitleListRowItem> items) {
        super(activityToUse, R.layout.book_list_item, items);

        activity = activityToUse;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.book_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.bookCover = (ImageView) convertView.findViewById(R.id.bookCover);
            viewHolder.bookTitle = (TextView) convertView.findViewById(R.id.bookTitle);
            viewHolder.bookAuthors = (TextView) convertView.findViewById(R.id.bookAuthorsLabel);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AbstractTitleListRowItem item = getItem(position);
        ZLImage bookCover = Library.getCover(item.getBookZlFile());

        BookInfoActivity.setCover(getActivity().getWindowManager(), viewHolder.bookCover, bookCover);
        viewHolder.bookTitle.setText(item.getBookTitle());
        viewHolder.bookAuthors.setText(item.getAuthors());

        return convertView;
    }

    private Activity getActivity() {
        return activity;
    }

    private static class ViewHolder {
        public ImageView bookCover;
        public TextView bookTitle;
        public TextView bookAuthors;
    }
}
