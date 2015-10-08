package org.geometerplus.android.fbreader.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 10/7/15.
 */
public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private static final int DEFAULT_PROGRESS_VALUE = 50;
    private int initialProgress;

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle, int initialProgress) {
        super(context, attrs, defStyle);

        this.initialProgress = initialProgress;
    }

    @Override
    public View onCreateView(ViewGroup parent){
        super.onCreateView(parent);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.seek_bar_preference, null);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBarPreference);
        seekBar.setProgress(initialProgress);
        seekBar.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
        callChangeListener(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index){
        return typedArray.getInt(index, DEFAULT_PROGRESS_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int temp = restoreValue ? getPersistedInt(DEFAULT_PROGRESS_VALUE) : (Integer)defaultValue;
        if(!restoreValue)
            persistInt(temp);
    }
}
