package org.geometerplus.android.fbreader.benetech;

import java.util.ArrayList;

import org.benetech.android.R;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter for an ArrayList of Strings that can be used as accessible option for long press menu
 * @author roms
 */
public class LabelsListAdapter extends ArrayAdapter<Object> {
    ArrayList<Object> labels;
    Activity myActivity;
            
    public LabelsListAdapter(ArrayList<Object> items, Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, items);
        labels = items;
        myActivity = activity;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = myActivity.getLayoutInflater();
        convertView = inflater.inflate(R.layout.dialog_items, null);

        holder = new ViewHolder();
        holder.text = (TextView) convertView.findViewById(R.id.text);		

        convertView.setTag(holder);

        holder.text.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        holder.text.setText(labels.get(position).toString());
        return convertView;
    }

    private class ViewHolder {
        TextView text;
    }
}
