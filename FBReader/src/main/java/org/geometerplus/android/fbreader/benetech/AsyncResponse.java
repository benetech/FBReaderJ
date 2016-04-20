package org.geometerplus.android.fbreader.benetech;

/**
 * Created by animal@martus.org on 4/19/16.
 */
public interface AsyncResponse<E> {
    public void processFinish(E type);
}
