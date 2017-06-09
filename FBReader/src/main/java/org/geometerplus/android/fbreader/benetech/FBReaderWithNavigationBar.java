package org.geometerplus.android.fbreader.benetech;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hyperionics.fbreader.plugin.tts_plus.TtsSentenceExtractor;

import org.accessibility.SimpleGestureFilter;
import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.TOCActivity;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.android.fbreader.library.DownloadedBookInfoActivity;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.SyncReadingListsWithBookshareAction;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class FBReaderWithNavigationBar extends FBReaderWithPinchZoom implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, SimpleGestureFilter.SimpleGestureListener, AsyncResponse<Boolean>  {

    private static final String LOG_TAG ="FBRsWithNavigationBar";
    private static final String ACTIVITY_RESUMING_STATE ="ACTIVITY_RESUMING_STATE";
    private ApiServerImplementation myApi;
    private TextToSpeech myTTS;
    private int myParagraphIndex = -1;
    private int myParagraphsNumber;
    private boolean isActive = false;
    private static final int PLAY_AFTER_TOC = 1;
    private static final int CHECK_TTS_INSTALLED = 0;
    public static final int SPEAK_BACK_PRESSED = 77;

    private SimpleGestureFilter detector;
    private Vibrator myVib;
    private int lastSentence = 0;
    private boolean isPaused = false;
    private boolean returnFromOtherScreen = false;
    private boolean screenLockEventOccurred = false;
    private BroadcastReceiver mReceiver;
    private PowerManager pm;
    private AccessibilityManager accessibilityManager;

    private static final long[] VIBE_PATTERN = {0, 10, 70, 80,};

    public static final String CONTENTS_EARCON = "[CONTENTS]";
    public static final String MENU_EARCON = "[MENU]";
    public static final String FORWARD_EARCON = "[FORWARD]";
    public static final String BACK_EARCON = "[BACK]";
    public static final String START_READING_EARCON = "[START]";

    private static Method AccessibilityManager_isTouchExplorationEnabled;
    private static SharedPreferences myPreferences;
    private FBReaderApp fbReader;

    private TtsSentenceExtractor.SentenceIndex mySentences[] = new TtsSentenceExtractor.SentenceIndex[0];
    private static int myCurrentSentence = 0;
    private static final String UTTERANCE_ID = "GoReadTTS";
    private static HashMap<String, String> myCallbackMap;
    private volatile int myInitializationStatus;
    private final static int TTS_INITIALIZED = 2;
    private final static int FULLY_INITIALIZED =  TTS_INITIALIZED;
    private volatile PowerManager.WakeLock myWakeLock;
    private static final String IS_FIRST_TIME_RUNNING_PREFERENCE_TAG = "first_time_running";

    private boolean activityResuming = false; // this means that the activity is resuming from being in background or orientation change and not created fresh
    private boolean isFirstTimeRunningApp;
    static {
        initCompatibility();
    }

    private static void initCompatibility() {
        try {
            AccessibilityManager_isTouchExplorationEnabled = AccessibilityManager.class.getMethod("isTouchExplorationEnabled");
            /* success, this is a newer device */
        } catch (NoSuchMethodException nsme) {
            /* failure, must be older device */
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        accessibilityManager = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        super.onCreate(savedInstanceState);
        fbReader = (FBReaderApp)FBReaderApp.Instance();
        if(savedInstanceState != null){
            activityResuming = savedInstanceState.getBoolean(ACTIVITY_RESUMING_STATE, false);
        }

        detector = new SimpleGestureFilter(this,this);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        if (isTouchExplorationEnabled(accessibilityManager)) {
            findViewById(R.id.navigation_bar_skip_previous).setOnHoverListener(new MyHoverListener());
            findViewById(R.id.navigation_bar_skip_next).setOnHoverListener(new MyHoverListener());
            findViewById(R.id.navigation_bar_play).setOnHoverListener(new MyHoverListener());
        }

        setListener(R.id.navigation_bar_play, new View.OnClickListener() {
            public void onClick(View v) {
                ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON, Analytics.EVENT_LABEL_PLAY_PAUSE);
                playOrPause();
            }
        });

        setListener(R.id.navigation_bar_skip_previous, new View.OnClickListener() {
            public void onClick(View v) {
                ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON, Analytics.EVENT_LABEL_PREV);
                goBackward();
            }
        });

        findViewById(R.id.navigation_bar_skip_previous).setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    public void onFocusChange(android.view.View view, boolean b) {
                        if (b) {
                            stopTalking();
                        }
                    }
                });

        setListener(R.id.navigation_bar_skip_next, new View.OnClickListener() {
            public void onClick(View v) {
                ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON, Analytics.EVENT_LABEL_NEXT);
                goForward();
            }
        });

        findViewById(R.id.navigation_bar_skip_next).setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    public void onFocusChange(android.view.View view, boolean b) {
                        if (b) {
                            stopTalking();
                        }
                    }
                });

        setActive(false);

        if (myCallbackMap == null) {
            myCallbackMap = new HashMap<String, String>();
            myCallbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        }
        myApi = new ApiServerImplementation();
        try {
            startActivityForResult(new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), CHECK_TTS_INSTALLED);
        } catch (ActivityNotFoundException e) {
            showErrorMessage(getText(R.string.no_tts_installed), true);
        }

        if (!accessibilityManager.isEnabled()) {
            setTitle(R.string.initializing);
        }

        myPreferences = getSharedPreferences("GoReadTTS", MODE_PRIVATE);

        isFirstTimeRunningApp = myPreferences.getBoolean(IS_FIRST_TIME_RUNNING_PREFERENCE_TAG, true);
        if (isFirstTimeRunningApp) {
            Log.i(LOG_TAG, "First time Go Read is running after it has been installed");
            myPreferences.edit().putBoolean(IS_FIRST_TIME_RUNNING_PREFERENCE_TAG, false).commit();
        }

        if(! activityResuming){
            //only sync for fresh creation
            syncBookshareReadingLists();
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenUnlockReceiver();
        registerReceiver(mReceiver, filter);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        setIsPaused();
        activityResuming = true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(ACTIVITY_RESUMING_STATE, activityResuming);
    }

    private void syncBookshareReadingLists() {
        if (isLoggedintoBookshare()) {
            CheckInternetConnectionTask task = new CheckInternetConnectionTask(getBaseContext(), this);
            task.execute();
        }
        else {
            if(isFirstTimeRunningApp){
                ZLApplication.Instance().doAction(ActionCode.BOOKSHARE);
            }
            else {
                Toast.makeText(this,   "Must log into bookshare to auto sync reading lists", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void processFinish(Boolean hasInternetConnection) {
        if (hasInternetConnection.booleanValue())
            if(isFirstTimeRunningThisVersion){//Already calculated during onCreate() on parent class
                ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.FIRST_STARTUP);
            }
            else {
                ZLApplication.Instance().doAction(ActionCode.SYNC_WITH_BOOKSHARE, SyncReadingListsWithBookshareAction.SyncType.SILENT_STARTUP);
            }
        else
            Toast.makeText(getBaseContext(),   "Could not sync reading lists, no internet connection!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHECK_TTS_INSTALLED) {
            myTTS = new TextToSpeech(this, this);
        } else if (requestCode == FBReader.LOGIN_CODE) {
            if(resultCode == Activity.RESULT_OK){
                syncBookshareReadingLists();
            }
        }
        else if(requestCode == REPAINT_CODE){
            if(resultCode == DownloadedBookInfoActivity.RESULT_BOOK_DELETED){
                postDeleteBook();
            }
        }
        else {
            if (resultCode == TOCActivity.BACK_PRESSED) {
                returnFromOtherScreen = true;
            } else {
                setIsPlaying();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Locale bookLocale = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // only for gingerbread and newer versions
                bookLocale = Locale.forLanguageTag(myApi.getBookLanguage());
            }
            if (myTTS != null){
                try{
                    if(myTTS.isLanguageAvailable(bookLocale) == TextToSpeech.LANG_AVAILABLE) {
                        myTTS.setLanguage(bookLocale);
                    }
                } catch (Exception e){
                    Log.e("FBReaderWNavBar", "failed fetching locale", e);
                }
            }

            findViewById(R.id.navigation_bar_play).requestFocus();
            if(accessibilityManager.isEnabled()){
                enablePlayButton();
            }
            if (!returnFromOtherScreen) {
                setCurrentLocation();
            }
            returnFromOtherScreen = false;

            if (isPaused() && !screenLockEventOccurred) {
                playEarcon(START_READING_EARCON);
                //speakParagraph(getNextParagraph());
            } else {
                screenLockEventOccurred = false;
            }
        } catch (Exception e) {
            Log.e("GoRead", "Error on resuming of speak activity", e);
        }
    }

    private void setCurrentLocation() {
        myParagraphIndex = myApi.getPageStart().ParagraphIndex;
        myParagraphsNumber = myApi.getParagraphsNumber();
    }

    public static boolean isFirstTimeAppRunning() {
        return myPreferences.getBoolean(IS_FIRST_TIME_RUNNING_PREFERENCE_TAG, true);
    }

    @Override
    public void selectSentenceFromView() {
        if (isPlaying()) {
            pause();
        }
        getNextParagraph();
        highlightSentence(myCurrentSentence);
        play();
    }


    @Override
    public void onStart() {
        super.onStart();
        ((ZLAndroidApplication) getApplication()).startTracker(this);
    }

    @Override
    public void onStop() {
        if (pm.isScreenOn()) {
            stopTalking();
            myApi.clearHighlighting();
            savePosition();

            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                //do nothing
            }
        }
        super.onStop();
        ((ZLAndroidApplication) getApplication()).stopTracker(this);
    }

    @Override
    protected void onDestroy() {
        if (myTTS != null) {
            myTTS.shutdown();
        }
        super.onDestroy();
    }

    private static boolean isTouchExplorationEnabled(AccessibilityManager am) {
        try {
            if (AccessibilityManager_isTouchExplorationEnabled != null) {
                Object retobj = AccessibilityManager_isTouchExplorationEnabled.invoke(am);
                return (Boolean) retobj;
            }
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return false;
    }

    private void setListener(int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    private void savePosition() {
        if (myCurrentSentence < mySentences.length) {
            String bookHash = "";
            if (fbReader != null && fbReader.Model != null)
                bookHash = "BP:" + fbReader.Model.Book.getId();

            SharedPreferences.Editor myEditor = myPreferences.edit();
            Time time = new Time();
            time.setToNow();
            String lang = "";
            //lang = " l:" + selectedLanguage;
            myEditor.putString(bookHash, lang + "p:" + myParagraphIndex + " s:" + myCurrentSentence + " e:" + mySentences[myCurrentSentence].i + " d:" + time.format2445());

            myEditor.commit();
        }
    }

    private void restorePosition() {
        String bookHash = "";
        if (fbReader != null && fbReader.Model != null)
            bookHash = "BP:" + fbReader.Model.Book.getId();

        String s = myPreferences.getString(bookHash, "");
        //int il = s.indexOf("l:");
        int para = s.indexOf("p:");
        int sent = s.indexOf("s:");
        int idx = s.indexOf("e:");
        int dt = s.indexOf("d:");
        if (para > -1 && sent > -1 && idx > -1 && dt > -1) {
/*                if (il > -1) {
                selectedLanguage = s.substring(il + 2, para);
            }*/
            para = Integer.parseInt(s.substring(para + 2, sent-1));
            sent = Integer.parseInt(s.substring(sent + 2, idx - 1));
            idx = Integer.parseInt(s.substring(idx + 2, dt - 1));
            TextPosition tp = new TextPosition(para, idx, 0);
            if (tp.compareTo(myApi.getPageStart()) >= 0 && tp.compareTo(myApi.getPageEnd()) < 0) {
                myParagraphIndex = para;
                myCurrentSentence = sent;
            }
        } else {
            myParagraphIndex = myApi.getPageStart().ParagraphIndex;
            myParagraphsNumber = myApi.getParagraphsNumber();
        }
    }

    public void onInit(int status) {
        if (myInitializationStatus != FULLY_INITIALIZED) {
            myInitializationStatus |= TTS_INITIALIZED;
            if (myInitializationStatus == FULLY_INITIALIZED) {
                doFinalInitialization();
            }
        }
    }

    private void doFinalInitialization() {

        if (null == myTTS.getLanguage()) {
            showErrorMessage(getText(R.string.no_tts_language), true);
            return;
        }

        myTTS.setOnUtteranceCompletedListener(this);

        myTTS.addEarcon(CONTENTS_EARCON, "org.benetech.android", R.raw.sound_toc);
        myTTS.addEarcon(MENU_EARCON, "org.benetech.android", R.raw.sound_main_menu);
        myTTS.addEarcon(FORWARD_EARCON, "org.benetech.android", R.raw.sound_forward);
        myTTS.addEarcon(BACK_EARCON, "org.benetech.android", R.raw.sound_back);
        myTTS.addEarcon(START_READING_EARCON, "org.benetech.android", R.raw.sound_start_reading);

        restorePosition();

        playEarcon(START_READING_EARCON);

        if (accessibilityManager.isEnabled()) {
            speakString(myApi.getBookTitle(), 0);
        } else {
            setTitle(myApi.getBookTitle());
        }
    }

    private void highlightParagraph()  {
        if (0 <= myParagraphIndex && myParagraphIndex < myParagraphsNumber) {
            myApi.highlightArea(
                    new TextPosition(myParagraphIndex, 0, 0),
                    new TextPosition(myParagraphIndex, Integer.MAX_VALUE, 0)
            );
        } else {
            myApi.clearHighlighting();
        }
    }

    private void stopTalking() {
        setIsPaused();
        setActive(false);
        enablePlayButton();
        if (myTTS != null) {
            myTTS.stop();
        }
    }

    private void showErrorMessage(final CharSequence text, final boolean fatal) {
        final VoiceableDialog finishedDialog = new VoiceableDialog(this);
        if (fatal) {
            setTitle(R.string.failure);
        }
        finishedDialog.popup(text.toString(), 5000);
    }

    private synchronized void setActive(final boolean isActiveToUse) {
        isActive = isActiveToUse;
        if (isActive()) {
            if (myWakeLock == null) {
                myWakeLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FBReader TTS plugin");
                myWakeLock.acquire();
            }
        } else {
            if (myWakeLock != null) {
                myWakeLock.release();
                myWakeLock = null;
            }
        }
    }

    private void speakString(String text, final int sentenceNumber) {
        HashMap<String, String> callbackMap = new HashMap<String, String>();
        callbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(sentenceNumber));
        myTTS.speak(text, TextToSpeech.QUEUE_ADD, callbackMap);
    }

    private void gotoPreviousParagraph() {
        for (int index = myParagraphIndex - 1; index >= 0; --index) {
            if (myApi.getParagraphText(index).length() > 0) {
                myParagraphIndex = index;
                break;
            }
        }
        if (myApi.getPageStart().ParagraphIndex >= myParagraphIndex) {
            myApi.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
        }
        highlightParagraph();
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.navigation_bar_skip_next).setEnabled(true);
                findViewById(R.id.navigation_bar_play).setEnabled(true);
            }
        });
    }

    private String getNextParagraph() {
        String text = "";
        final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
        ZLTextRegion region = fbReader.getTextView().getDoubleTapSelectedRegion();

        boolean shouldHighlightSentence = false;
        if(region == null && fbReader.getTextView().didScroll()){
            region = fbReader.getTextView().getTopOfPageRegion();
            shouldHighlightSentence = true;
            fbReader.getTextView().resetDidScroll();
        }
        int wordOffset = 0;
        if(region != null){
            myParagraphIndex = region.getSoul().getParagraphIndex();
            fbReader.getTextView().resetLatestLongPressSelectedRegion();
            if(region.getSoul() instanceof ZLTextWordRegionSoul){
                wordOffset = (region.getSoul().getStartElementIndex() /2) +1;
            }
        }
        List<String> wordsList = null;
        ArrayList<Integer> paragraphIndexesList = new ArrayList<Integer>();
        for (; myParagraphIndex < myParagraphsNumber; ++myParagraphIndex) {
            final String paragraphText = myApi.getParagraphText(myParagraphIndex);
            wordsList = myApi.getParagraphWords(myParagraphIndex);
            if (paragraphText.length() > 0) {
                text = paragraphText;
                paragraphIndexesList = myApi.getParagraphIndices(myParagraphIndex);
                break;
            }
        }

        if (null != wordsList) {
            mySentences = TtsSentenceExtractor.build(wordsList, paragraphIndexesList, myTTS.getLanguage());

            if(region != null) {
                int currentSentence = 0;
                for (int i = 0; i < mySentences.length; i++) {
                    TtsSentenceExtractor.SentenceIndex sentence = mySentences[i];
                    int sentenceWordCount = new StringTokenizer(sentence.s).countTokens();
                    if (sentenceWordCount >= wordOffset) {
                        currentSentence = i;
                        break;
                    } else {
                        wordOffset -= sentenceWordCount;
                    }
                }
                myCurrentSentence = currentSentence;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(shouldHighlightSentence){
                highlightSentence(myCurrentSentence);
            }
            else {
                highlightParagraph();
            }
        }

        //Disable next section button if this is the last paragraph
        if (myParagraphIndex >= (myParagraphsNumber - 1)) {
            disableNextButton();
        }
        return text;
    }

    private void speakParagraph(String text) {
        if (text.length() < 1) {
            return;
        }

        setActive(true);
        ArrayList<String> sentenceList = new ArrayList<>();
        for (TtsSentenceExtractor.SentenceIndex mySentence : mySentences) {
            sentenceList.add(mySentence.s);
        }

        final Iterator<String> sentenceIterator = sentenceList.iterator();

        String currentSentence;
        int sentenceNumber = 0;
        int numWordIndices = sentenceList.size();

        if (isPaused()) {
            enablePauseButton();
            setIsPlaying();
            if (myCurrentSentence > 0 && numWordIndices > myCurrentSentence) {
                sentenceNumber = myCurrentSentence ;
                highlightSentence(sentenceNumber);
            }

        } else { //should only highlight first sentence of paragraph if we haven't just paused
            if (numWordIndices > 0) {
                highlightSentence(0);
            }
        }
        for(int c = sentenceNumber; c > 0 && sentenceIterator.hasNext(); c--){
            sentenceIterator.next();
        }
        while (sentenceIterator.hasNext())  {   // if there are sentences in the sentence queue
            sentenceNumber++;
            currentSentence = sentenceIterator.next();
            speakString(currentSentence, sentenceNumber);
        }

        lastSentence = sentenceNumber;
    }

    @Override
    public void onUtteranceCompleted(String uttId) {
        String lastSentenceID = Integer.toString(lastSentence);
        if (isActive() && uttId.equals(lastSentenceID)) {
            ++myParagraphIndex;
            speakParagraph(getNextParagraph());
            if (myParagraphIndex >= myParagraphsNumber) {
                stopTalking();
            }
        } else {
            myCurrentSentence = Integer.parseInt(uttId);
            if (isActive()) {
                int listSize = mySentences.length;
                if (listSize > 1 && myCurrentSentence < listSize) {
                    highlightSentence(myCurrentSentence);
                }
            }
        }
    }

    private void disableNextButton() {
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.navigation_bar_skip_next).setEnabled(false);
            }
        });
    }

    @Override
    public void playOrPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void toggleDisplayBars(){
        if(getSupportActionBar() != null){
            View playBar = findViewById(R.id.navigation_bar_id);
            if(playBar != null){
                playBar.setVisibility(getSupportActionBar().isShowing()?View.GONE:View.VISIBLE);
            }
            if(getSupportActionBar().isShowing()){
                getSupportActionBar().hide();
            }
            else {
                getSupportActionBar().show();
            }
        }
    }

    private void play() {
        final String nextParagraph = getNextParagraph();
        if (null == nextParagraph || nextParagraph.length() < 1) {
            restorePosition();
            setIsPlaying();
        }
        hideSelectionPanel();
        changePlayPauseButtonState(false);
        speakParagraph(nextParagraph);
    }

    private void pause() {
        stopTalking();
        changePlayPauseButtonState(true);
        setIsPaused();
    }

    private void highlightSentence(int myCurrentSentence) {
        if (myCurrentSentence >= mySentences.length) {
            return;
        }
        int endEI = myCurrentSentence < mySentences.length-1 ? mySentences[myCurrentSentence+1].i-1: Integer.MAX_VALUE;

        TextPosition stPos;
        if (myCurrentSentence == 0)
            stPos = new TextPosition(myParagraphIndex, 0, 0);
        else
            stPos = new TextPosition(myParagraphIndex, mySentences[myCurrentSentence].i, 0);

        TextPosition edPos = new TextPosition(myParagraphIndex, endEI, 0);
        if (stPos.compareTo(myApi.getPageStart()) < 0 || edPos.compareTo(myApi.getPageEnd()) > 0)
            myApi.setPageStart(stPos);

        myApi.highlightArea(stPos, edPos);
    }

    private void enablePlayButton() {
        changePlayPauseButtonState(true);
    }

    private void enablePauseButton() {
        changePlayPauseButtonState(false);
    }

    private void changePlayPauseButtonState(final boolean isPlayButton) {
        runOnUiThread(new Runnable() {
            public void run() {
                ImageButton playButton = (ImageButton) findViewById(R.id.navigation_bar_play);
                playButton.setImageResource(getPlayButtonImageResource(isPlayButton));
                playButton.setContentDescription(getString(getPlayButtonContentDescription(isPlayButton)));
            }
        });
    }

    private int getPlayButtonContentDescription(boolean isPlayButton) {
        if (isPlayButton)
            return R.string.content_description_play;

        return R.string.content_description_pause;
    }

    private int getPlayButtonImageResource(boolean isPlayButton) {
        if (isPlayButton)
            return R.drawable.ic_play_arrow_white_24dp;

        return R.drawable.ic_pause_white_24dp;
    }

    private void goForward() {
        boolean wasPlaying = isPlaying();
        stopTalking();
        playEarcon(FORWARD_EARCON);
        if (myParagraphIndex < myParagraphsNumber) {
            myParagraphIndex++;
            final String nextParagraph = getNextParagraph();
            if (wasPlaying) {
                speakParagraph(nextParagraph);
            }
        }
    }

    private void goBackward() {
        boolean wasPlaying = isPlaying();
        stopTalking();
        playEarcon(BACK_EARCON);
        gotoPreviousParagraph();
        final String nextParagraph = getNextParagraph();
        if (wasPlaying) {
            speakParagraph(nextParagraph);
        }
    }

    private void showMainMenu() {
        stopTalking();
        playEarcon(MENU_EARCON);
        Intent intent = new Intent(this, AccessibleMainMenuActivity.class);
        startActivityForResult(intent, PLAY_AFTER_TOC);
    }

    private void playEarcon(String backEarcon) {
        if (accessibilityManager.isEnabled())
            myTTS.playEarcon(backEarcon, TextToSpeech.QUEUE_ADD, null);
    }

    private void setIsPaused() {
        isPaused = true;
    }

    private void setIsPlaying() {
        isPaused = false;
    }

    private boolean isPaused() {
        return isPaused;
    }

    private boolean isPlaying() {
        return !isPaused();
    }

    private boolean isActive() {
        return isActive;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        if(accessibilityManager.isEnabled()){
            findViewById(R.id.navigation_bar_play).requestFocus();
        }
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction) {
        myVib.vibrate(VIBE_PATTERN, -1);
        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT :
                goForward();
                ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE, Analytics.EVENT_LABEL_NEXT);
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                goBackward();
                ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE, Analytics.EVENT_LABEL_PREV);
                break;
        }
    }

    @Override
    public void onTwoFingerDoubleTap() {
        myVib.vibrate(VIBE_PATTERN, -1);
        ((ZLAndroidApplication) getApplication()).trackGoogleAnalyticsEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE, Analytics.EVENT_LABEL_PLAY_PAUSE);
        playOrPause();
    }

    /*
     * show accessible full screen menu when accessibility is turned on
     *
    */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU){
            showMainMenu();
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopTalking();
            if (accessibilityManager.isEnabled()) {
                this.setResult(SPEAK_BACK_PRESSED);
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void preDeleteBookWork() {
        super.preDeleteBookWork();
        stopTalking();
        myApi.clearHighlighting();
    }

    private class ScreenUnlockReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                screenLockEventOccurred = true;
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                screenLockEventOccurred = true;
            }
        }
    }

    private class MyHoverListener implements View.OnHoverListener {
        @Override
        public boolean onHover(View view, MotionEvent motionEvent) {
            stopTalking();
            return false;
        }
    }
}
