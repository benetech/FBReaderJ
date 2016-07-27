package org.geometerplus.fbreader.fbreader;

import android.app.Activity;

/**
 * Created by avanticatechnologies on 7/26/16.
 */

public class SyncReadingListsWithBookshareActionObserver {

    private static SyncReadingListsWithBookshareActionObserver instance;

    private boolean syncing = false;
    private SyncReadingListsWithBookshareAction runningAction;

    public static SyncReadingListsWithBookshareActionObserver getInstance(){
        if(instance == null){
            instance = new SyncReadingListsWithBookshareActionObserver();
        }
        return instance;
    }

    public boolean isSyncing() {
        return syncing;
    }

    public void setSyncing(boolean syncing) {
        this.syncing = syncing;
    }

    public SyncReadingListsWithBookshareAction getRunningAction() {
        return runningAction;
    }

    public void setRunningAction(SyncReadingListsWithBookshareAction runningAction) {
        this.runningAction = runningAction;
        if(runningAction != null){
            setSyncing(true);
        }
        else {
            setSyncing(false);
        }
    }

    public void notifyRelevantBooklistOpened(Activity parent){
        if(getRunningAction() != null){
            getRunningAction()
                    .displayProgressDialog(SyncReadingListsWithBookshareAction.SyncType.SILENT_INTERRUPTED, parent);
        }
    }

}
