package org.geometerplus.android.fbreader.benetech;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.library.DownloadedBookInfoActivity;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

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

        DownloadedBookInfoActivity.setCover(getActivity().getWindowManager(), viewHolder.bookCover, bookCover);
        viewHolder.bookTitle.setText(item.getBookTitle());
        viewHolder.bookAuthors.setText(item.getAuthors());

        int userValue = ZLTextStyleCollection.Instance().getBaseStyle().FontSizeOption.getValue();
        userValue = Math.max(userValue, 18); //these values come from ZLFontSizeListPreference
        userValue = Math.min(userValue, 30);
        viewHolder.bookTitle.setTextSize(userValue);
        double lowerValue = userValue / 1.5;
        lowerValue = Math.max(lowerValue, 12d);
        viewHolder.bookAuthors.setTextSize(Math.round(lowerValue));

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
