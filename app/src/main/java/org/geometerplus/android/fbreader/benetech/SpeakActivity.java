/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.benetech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import com.google.analytics.tracking.android.EasyTracker;
import com.hyperionics.fbreader.plugin.tts_plus.TtsSentenceExtractor;

import org.accessibility.SimpleGestureFilter;
import org.accessibility.VoiceableDialog;
import org.benetech.android.R;
import org.geometerplus.android.fbreader.TOCActivity;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class SpeakActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, SimpleGestureFilter.SimpleGestureListener  {
    private ApiServerImplementation myApi;

	private TextToSpeech myTTS;

	private int myParagraphIndex = -1;
	private int myParagraphsNumber;

	private boolean myIsActive = false;

    private static final int PLAY_AFTER_TOC = 1;
    private static final int CHECK_TTS_INSTALLED = 0;
    public static final int SPEAK_BACK_PRESSED = 77;

    private SimpleGestureFilter detector;
    private Vibrator myVib;
    private int lastSentence = 0;
    private boolean justPaused = false;
    private boolean resumePlaying = false;
    private boolean returnFromOtherScreen = false;
	private boolean screenLockEventOccurred = false;
	private BroadcastReceiver mReceiver;

    //Added for the detecting whether the talkback is on
    private AccessibilityManager accessibilityManager;

    private static final long[] VIBE_PATTERN = {
        0, 10, 70, 80
    };
    public static final String CONTENTS_EARCON = "[CONTENTS]";
    public static final String MENU_EARCON = "[MENU]";
    public static final String FORWARD_EARCON = "[FORWARD]";
    public static final String BACK_EARCON = "[BACK]";
    public static final String START_READING_EARCON = "[START]";

    private static Method MotionEvent_getX;
    private static Method MotionEvent_getY;
    private static Method AccessibilityManager_isTouchExplorationEnabled;
    static SharedPreferences myPreferences;
    final FBReaderApp fbReader = (FBReaderApp) FBReaderApp.Instance();

    private TtsSentenceExtractor.SentenceIndex mySentences[] = new TtsSentenceExtractor.SentenceIndex[0];
    static private int myCurrentSentence = 0;
    static private final String UTTERANCE_ID = "GoReadTTS";
    static private HashMap<String, String> myCallbackMap;

    static {
        initCompatibility();
    }

    private static void initCompatibility() {
        try {
            MotionEvent_getX = MotionEvent.class.getMethod("getX", new Class[] { Integer.TYPE });
            MotionEvent_getY = MotionEvent.class.getMethod("getY", new Class[] { Integer.TYPE });
            AccessibilityManager_isTouchExplorationEnabled = AccessibilityManager.class.getMethod(
                    "isTouchExplorationEnabled");
            /* success, this is a newer device */
        } catch (NoSuchMethodException nsme) {
            /* failure, must be older device */
        }
    }

    private static boolean isTouchExplorationEnabled(AccessibilityManager am) {
        try {
            if (AccessibilityManager_isTouchExplorationEnabled != null) {
                Object retobj = AccessibilityManager_isTouchExplorationEnabled.invoke(am);
                return (Boolean) retobj;
            }
        } catch (IllegalAccessException ie) {
            System.err.println("unexpected " + ie);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private void setListener(int id, View.OnClickListener listener) {
		findViewById(id).setOnClickListener(listener);
	}

    private void setTouchFocusEnabled(int id) {
        findViewById(id).setFocusableInTouchMode(true);
    }

    private class MyHoverListener implements View.OnHoverListener {

        @Override
        public boolean onHover(View view, MotionEvent motionEvent) {
            stopTalking();
            justPaused = true;
            return false;
        }
    }

    void savePosition() {
        if (myCurrentSentence < mySentences.length) {
            String bookHash = "BP:" + fbReader.Model.Book.getId();;
            SharedPreferences.Editor myEditor = myPreferences.edit();
            Time time = new Time();
            time.setToNow();
            String lang = "";
            //lang = " l:" + selectedLanguage;
            myEditor.putString(bookHash, lang +
                    "p:" + myParagraphIndex + " s:" + myCurrentSentence + " e:" + mySentences[myCurrentSentence].i +
                    " d:" + time.format2445()
            );

            myEditor.commit();
        }
    }

    void restorePosition() {
        String bookHash = "BP:" + fbReader.Model.Book.getId();;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        WindowManager.LayoutParams params =
        getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(params);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        detector = new SimpleGestureFilter(this,this);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        accessibilityManager =
            (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        setContentView(R.layout.view_spokentext);

        if (isTouchExplorationEnabled(accessibilityManager)) {
            findViewById(R.id.speak_menu_back).setOnHoverListener(new MyHoverListener());
            findViewById(R.id.speak_menu_forward).setOnHoverListener(new MyHoverListener());
            findViewById(R.id.speak_menu_pause).setOnHoverListener(new MyHoverListener());
            findViewById(R.id.speak_menu_contents).setOnHoverListener(new MyHoverListener());
        }

		setListener(R.id.speak_menu_back, new View.OnClickListener() {
			public void onClick(View v) {
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON,
                    Analytics.EVENT_LABEL_PREV, null);
                goBackward();
			}
		});
        findViewById(R.id.speak_menu_back).setOnFocusChangeListener(
            new View.OnFocusChangeListener() {
                public void onFocusChange(android.view.View view, boolean b) {
                    if (b) {
                        stopTalking();
                        justPaused = true;
                    }
                }
            }
        );
		setListener(R.id.speak_menu_forward, new View.OnClickListener() {
			public void onClick(View v) {
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON,
                    Analytics.EVENT_LABEL_NEXT, null);
                goForward();
			}
		});
        findViewById(R.id.speak_menu_forward).setOnFocusChangeListener(
            new View.OnFocusChangeListener() {
                public void onFocusChange(android.view.View view, boolean b) {
                    if (b) {
                        stopTalking();
                        justPaused = true;
                    }
                }
            }
        );
/*		setListener(R.id.button_close, new View.OnClickListener() {
			public void onClick(View v) {
				stopTalking();
				finish();
			}
		});*/
		setListener(R.id.speak_menu_pause, new View.OnClickListener() {
			public void onClick(View v) {
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON,
                    Analytics.EVENT_LABEL_PLAY_PAUSE, null);
                playOrPause();
            }
		});
        setListener(R.id.speak_menu_contents, new View.OnClickListener() {
            public void onClick(View v) {
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_BUTTON,
                    Analytics.EVENT_LABEL_TOC, null);
                showContents();
            }
        });
        findViewById(R.id.speak_menu_contents).setOnFocusChangeListener(
            new View.OnFocusChangeListener() {
                public void onFocusChange(android.view.View view, boolean b) {
                    if (b) {
                        stopTalking();
                        justPaused = true;
                    }
                }
            }
        );

		((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(
			new PhoneStateListener() {
				public void onCallStateChanged(int state, String incomingNumber) {
					if (state == TelephonyManager.CALL_STATE_RINGING) {
						stopTalking();
					}
				}
			},
			PhoneStateListener.LISTEN_CALL_STATE
		);

		setActive(false);
		setActionsEnabled(false);

        if (myCallbackMap == null) {
            myCallbackMap = new HashMap<String, String>();
            myCallbackMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        }
		myApi = new ApiServerImplementation();
		try {
			startActivityForResult(
				new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA), CHECK_TTS_INSTALLED
			);
		} catch (ActivityNotFoundException e) {
			showErrorMessage(getText(R.string.no_tts_installed), true);
		}

        if (!accessibilityManager.isEnabled()) {
		    setTitle(R.string.initializing);
        }

        myPreferences = getSharedPreferences("GoReadTTS", MODE_PRIVATE);

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		mReceiver = new ScreenUnlockReceiver();
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_TTS_INSTALLED) {
            myTTS = new TextToSpeech(this, this);
        } else {
            if (resultCode == TOCActivity.BACK_PRESSED) {
                returnFromOtherScreen = true;
            } else {
                justPaused = false;
                resumePlaying = true;
            }
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
        try {
            findViewById(R.id.speak_menu_pause).requestFocus();

            if (!returnFromOtherScreen) {
                setCurrentLocation();
            }
            returnFromOtherScreen = false;

            if ((resumePlaying || justPaused) && !screenLockEventOccurred) {
                resumePlaying = false;
                myTTS.playEarcon(START_READING_EARCON, TextToSpeech.QUEUE_ADD, null);
                speakParagraph(getNextParagraph());
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

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		stopTalking();
        myApi.clearHighlighting();
        //LastReadPageOfCurrentBook.saveLocationOfLastReadPage(this);
        savePosition();

		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			//do nothing
		}
		super.onStop();
        EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onDestroy() {
		if (myTTS != null) {
			myTTS.shutdown();
		}
		super.onDestroy();
	}

	private volatile int myInitializationStatus;
	private final static int TTS_INITIALIZED = 2;
	private final static int FULLY_INITIALIZED =  TTS_INITIALIZED;

	// implements TextToSpeech.OnInitListener
	public void onInit(int status) {
		if (myInitializationStatus != FULLY_INITIALIZED) {
			myInitializationStatus |= TTS_INITIALIZED;
			if (myInitializationStatus == FULLY_INITIALIZED) {
				doFinalInitialization();
			}
		}
	}

	private void setActionsEnabled(final boolean enabled) {
		runOnUiThread(new Runnable() {
			public void run() {
				findViewById(R.id.speak_menu_back).setEnabled(enabled);
				findViewById(R.id.speak_menu_forward).setEnabled(enabled);
				findViewById(R.id.speak_menu_pause).setEnabled(enabled);
				findViewById(R.id.speak_menu_contents).setEnabled(enabled);
			}
		});
	}

	private void doFinalInitialization() {

        if (null == myTTS.getLanguage()) {
            setActionsEnabled(false);
            showErrorMessage(getText(R.string.no_tts_language), true);
            return;
        }

		myTTS.setOnUtteranceCompletedListener(this);

        myTTS.addEarcon(CONTENTS_EARCON, "org.benetech.android", R.raw.sound_toc);
        myTTS.addEarcon(MENU_EARCON, "org.benetech.android", R.raw.sound_main_menu);
        myTTS.addEarcon(FORWARD_EARCON, "org.benetech.android", R.raw.sound_forward);
        myTTS.addEarcon(BACK_EARCON, "org.benetech.android", R.raw.sound_back);
        myTTS.addEarcon(START_READING_EARCON, "org.benetech.android", R.raw.sound_start_reading);

        //setCurrentLocation();
        restorePosition();

        myTTS.playEarcon(START_READING_EARCON, TextToSpeech.QUEUE_ADD, null);

        if (accessibilityManager.isEnabled()) {
            speakString(myApi.getBookTitle(), 0);
        } else {
            setTitle(myApi.getBookTitle());
        }

        setActionsEnabled(true);
        speakParagraph(getNextParagraph());
	}

	@Override
	public void onUtteranceCompleted(String uttId) {
        String lastSentenceID = Integer.toString(lastSentence);
		if (myIsActive && uttId.equals(lastSentenceID)) {
            ++myParagraphIndex;
            speakParagraph(getNextParagraph());
            if (myParagraphIndex >= myParagraphsNumber) {
                stopTalking();
            }
		} else {
			myCurrentSentence = Integer.parseInt(uttId);
            if (myIsActive) {
                int listSize = mySentences.length;
                if (listSize > 1 && myCurrentSentence < listSize) {
                    highlightSentence(myCurrentSentence);
                }
            }
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
		setActive(false);
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

	private volatile PowerManager.WakeLock myWakeLock;

	private synchronized void setActive(final boolean active) {


		runOnUiThread(new Runnable() {
			public void run() {
				if(!accessibilityManager.isEnabled()){
	                if (myIsActive != active) {
	                    ((Button)findViewById(R.id.speak_menu_pause)).setText(active ? R.string.on_press_pause : R.string.on_press_play);
	                    if(myIsActive){
		                    WindowManager.LayoutParams params =
		                            getWindow().getAttributes();
		                            params.alpha=1;
		                            getWindow().setAttributes(params);
		                    } else {
		                        WindowManager.LayoutParams params =
		                                getWindow().getAttributes();
		                                params.alpha=0.2f;
		                                getWindow().setAttributes(params);
		                    }
	                }
				}
			}
		});

        myIsActive = active;

		if (active) {
			if (myWakeLock == null) {
				myWakeLock =
					((PowerManager)getSystemService(POWER_SERVICE))
						.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FBReader TTS plugin");
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
        for (int i = myParagraphIndex - 1; i >= 0; --i) {
            if (myApi.getParagraphText(i).length() > 0) {
                myParagraphIndex = i;
                break;
            }
        }
        if (myApi.getPageStart().ParagraphIndex >= myParagraphIndex) {
            myApi.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
        }
        highlightParagraph();
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.speak_menu_forward).setEnabled(true);
                findViewById(R.id.speak_menu_pause).setEnabled(true);
            }
        });

	}

	private String getNextParagraph() {
        String text = "";
        List<String> wl = null;
        ArrayList<Integer> il = null;
        for (; myParagraphIndex < myParagraphsNumber; ++myParagraphIndex) {
            final String s = myApi.getParagraphText(myParagraphIndex);
            wl = myApi.getParagraphWords(myParagraphIndex);
            if (s.length() > 0) {
                text = s;
                il = myApi.getParagraphIndices(myParagraphIndex);
                break;
            }
        }
        if (!"".equals(text) && !myApi.isPageEndOfText()) {
            myApi.setPageStart(new TextPosition(myParagraphIndex, 0, 0));
        }

        if (null != wl) {
            mySentences = TtsSentenceExtractor.build(wl, il, myTTS.getLanguage());
            highlightParagraph();
        }

        //Disable next section button if this is the last paragraph
        if (myParagraphIndex >= (myParagraphsNumber - 1)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    findViewById(R.id.speak_menu_forward).setEnabled(false);
                }
            });
        }
        return text;

	}

    // Bookshare custom methods

    private void highlightSentence(int myCurrentSentence) {
        if (myCurrentSentence >= mySentences.length) {
            return;
        }
       // try {
            int endEI = myCurrentSentence < mySentences.length-1 ?
                            mySentences[myCurrentSentence+1].i-1: Integer.MAX_VALUE;
            TextPosition stPos;
            if (myCurrentSentence == 0)
                stPos = new TextPosition(myParagraphIndex, 0, 0);
            else
                stPos = new TextPosition(myParagraphIndex, mySentences[myCurrentSentence].i, 0);
            TextPosition edPos = new TextPosition(myParagraphIndex, endEI, 0);
            if (stPos.compareTo(myApi.getPageStart()) < 0 || edPos.compareTo(myApi.getPageEnd()) > 0)
                myApi.setPageStart(stPos);
            myApi.highlightArea(stPos, edPos);

/*        } catch (ApiException e) {
            switchOff();
            TtsApp.ExitApp();
        }*/
    }

    private void speakParagraph(String text) {
        if (text.length() < 1) {
            return;
        }
        setActive(true);
        //createSentenceIterator();
        ArrayList<String> sentenceList = new ArrayList<String>();
        for (TtsSentenceExtractor.SentenceIndex mySentence : mySentences) {
            sentenceList.add(mySentence.s);
        }
        final Iterator<String> sentenceIterator = sentenceList.iterator();
        //sentenceListIterator = sentences.iterator();

        String currentSentence;
        int sentenceNumber = 0;
        int numWordIndices = sentenceList.size();

        if (justPaused) {                    // on returning from pause, iterate to the last sentence spoken
            justPaused = false;
            for (int i=1; i< myCurrentSentence; i++) {
                if (sentenceIterator.hasNext()) {
                    sentenceIterator.next();
                }
            }
            if (myCurrentSentence > 1 && numWordIndices > myCurrentSentence) {
                sentenceNumber = myCurrentSentence - 1;
                highlightSentence(myCurrentSentence + 1);
            }

        } else { //should only highlight first sentence of paragraph if we haven't just paused
            if (numWordIndices > 0) {
                highlightSentence(0);
            }
        }

        while (sentenceIterator.hasNext())  { 	// if there are sentences in the sentence queue
            sentenceNumber++;
            currentSentence = sentenceIterator.next();
            speakString(currentSentence, sentenceNumber);
        }

        lastSentence = sentenceNumber;

        // Disable play button if this is last paragraph
        if (myParagraphIndex >= (myParagraphsNumber - 1)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    findViewById(R.id.speak_menu_pause).setEnabled(false);
                }
            });
        }
    }


    private void playOrPause() {
            if (!myIsActive) {
                final String nextParagraph = getNextParagraph();
                if (null == nextParagraph || nextParagraph.length() < 1) {
                    //setCurrentLocation();
                    restorePosition();
                    justPaused = false;
                }
                speakParagraph(nextParagraph);
            } else {
                stopTalking();
                justPaused = true;
            }
        }

    private void goForward() {
        stopTalking();
        myTTS.playEarcon(FORWARD_EARCON, TextToSpeech.QUEUE_ADD, null);
	    setButtonOpacity(0.2f);
        if (myParagraphIndex < myParagraphsNumber) {
            ++myParagraphIndex;
            speakParagraph(getNextParagraph());
        }
    }

    private void goBackward() {
        stopTalking();
        myTTS.playEarcon(BACK_EARCON, TextToSpeech.QUEUE_ADD, null);
	    setButtonOpacity(0.2f);
        gotoPreviousParagraph();
        speakParagraph(getNextParagraph());
    }

    private void showContents() {
        justPaused = true;
        stopTalking();
        myTTS.playEarcon(CONTENTS_EARCON, TextToSpeech.QUEUE_FLUSH, null);
	    setButtonOpacity(0.2f);
        Intent tocIntent = new Intent(this, TOCActivity.class);
        startActivityForResult(tocIntent, PLAY_AFTER_TOC);
    }

    private void showMainMenu() {
        stopTalking();
        justPaused = true;
        myTTS.playEarcon(MENU_EARCON, TextToSpeech.QUEUE_ADD, null);
	    setButtonOpacity(0.2f);
        Intent intent = new Intent(this, AccessibleMainMenuActivity.class);
        startActivityForResult(intent, PLAY_AFTER_TOC);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){

	    setButtonOpacity(1);
	    this.detector.onTouchEvent(me);
          return super.dispatchTouchEvent(me);
    }

	private void setButtonOpacity(final float value)
	{
		runOnUiThread(new Runnable() {
	        public void run() {
	            WindowManager.LayoutParams params =
	                    getWindow().getAttributes();
	                    params.alpha=value;
	                    getWindow().setAttributes(params);
	        }
	    });
	}

	@Override
    public void onSwipe(int direction) {
        myVib.vibrate(VIBE_PATTERN, -1);
        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT :
                goForward();
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE,
                    Analytics.EVENT_LABEL_NEXT, null);
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                goBackward();
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE,
                    Analytics.EVENT_LABEL_PREV, null);
                break;
            case SimpleGestureFilter.SWIPE_DOWN :
                showMainMenu();
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE,
                    Analytics.EVENT_LABEL_MENU, null);
                break;
            case SimpleGestureFilter.SWIPE_UP :
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE,
                    Analytics.EVENT_LABEL_TOC, null);
                showContents();
                break;
          }
    }

    @Override
    public void onDoubleTap() {
        myVib.vibrate(VIBE_PATTERN, -1);
        EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_GESTURE,
            Analytics.EVENT_LABEL_PLAY_PAUSE, null);
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
}
