package org.geometerplus.android.fbreader.benetech;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.benetech.android.R;

/**
 * Created by animal@martus.org on 4/25/16.
 */
public class GoReadTabContainer extends AbstractBaseTabContainer {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reading_list_tab_container, null);
    }

    @Override
    protected void initView() {
        replaceFragment(new GoReadTabMainTabContent(), false);
    }
}
