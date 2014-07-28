package org.geometerplus.android.fbreader.benetech;

import java.util.ArrayList;
import java.util.List;

import org.benetech.android.R;
import org.geometerplus.android.fbreader.TOCActivity;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

public class AccessibleMainMenuActivity extends Activity {

    private List<Object> listItems = new ArrayList<Object>();
    private ListView list;
    private static Resources resources;
    private static Activity me;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);
        resources = getApplicationContext().getResources();
        me = this;

        int menuItemLimit = MenuControl.values().length;

        for ( int i = 0; i < menuItemLimit; i++ ) {
			Object object = new Object();
			listItems.add(object);
        }        
		list = (ListView) findViewById(R.id.list);
        ListItemsAdapter adapter = new ListItemsAdapter(listItems);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new MainMenuClickListener(this));
    }

	private class ListItemsAdapter extends ArrayAdapter<Object> {
		public ListItemsAdapter(List<Object> items) {
			super(AccessibleMainMenuActivity.this, android.R.layout.simple_list_item_1, items);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			LayoutInflater inflater = getLayoutInflater();
			convertView = inflater.inflate(R.layout.dialog_items, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.text);		

			convertView.setTag(holder);

			// Bind the data efficiently with the holder.
			holder.text.setText( "Item: " + (position+1) );
			//holder.text.setTextSize( 20 );
			holder.text.setTypeface(Typeface.defaultFromStyle (Typeface.NORMAL));
			holder.text.setText(MenuControl.values()[position].getLabel());
			return convertView;
		}

		private class ViewHolder {
			TextView text;
		}
	}

	private class MainMenuClickListener implements OnItemClickListener {
	    private final Activity activity;

        private MainMenuClickListener(final Activity activity) {
            this.activity = activity;
        }

        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            MenuControl.values()[position].click(activity);
        }
	}

	@Override
	    public void onBackPressed() {
	        setResult(TOCActivity.BACK_PRESSED);
	        super.onBackPressed();
	    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			return true;
        } else if (keyCode == KeyEvent.KEYCODE_CALL) {
        	return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(TOCActivity.BACK_PRESSED);
			finish();
			return true;
		}
		return false;
	}

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    /**
     * Order of FBReader's main menu options. Each enum entry has an associated label and onClick operation.
     */
    private enum MenuControl {
	    
	    speak(resources.getString(R.string.menu_speak), new MenuOperation() {
	        public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SPEAK);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_READ, null);
                activity.finish();
	        }
	    }),
        tableOfContents(resources.getString(R.string.menu_toc), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_TOC);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_TOC, null);
                activity.finish();
            }
        }),
        navigate(resources.getString(R.string.menu_navigate), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.ACCESSIBLE_NAVIGATION);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_NAVIGATE, null);
                activity.finish();
            }
        }),
        bookmarks(resources.getString(R.string.menu_bookmarks), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_BOOKMARKS);
                activity.finish();
            }
        }),
        library(resources.getString(R.string.menu_library), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_LIBRARY);
                activity.finish();
            }
        }),
        bookshare(resources.getString(R.string.menu_bookshare), new MenuOperation() {
            public void click(Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.BOOKSHARE);
                activity.finish();
            }
        }),
        networkLibrary(resources.getString(R.string.menu_network_library), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_NETWORK_LIBRARY);
                activity.finish();
            }
        }),
	    settings(resources.getString(R.string.menu_settings), new MenuOperation() {
	        public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_PREFERENCES);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_SETTINGS, null);
                activity.finish();
	        }
	    }),
        accessibilitySettings(resources.getString(R.string.menu_accessibility_settings), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_ACCESSIBILITY_SETTINGS);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_ACCESSIBILITY_SETTINGS, null);
                activity.finish();
            }
        }),
        ttsSettings(resources.getString(R.string.menu_tts_settings), new MenuOperation() {
            public void click(final Activity activity) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                me.startActivity(intent);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_TTS_SETTINGS, null);
                activity.finish();
            }
        }),
        help(resources.getString(R.string.menu_help), new MenuOperation() {
            public void click(final Activity activity) {
                ZLApplication.Instance().doAction(ActionCode.SHOW_HELP);
                EasyTracker.getTracker().trackEvent(Analytics.EVENT_CATEGORY_UI, Analytics.EVENT_ACTION_MENU,
                    Analytics.EVENT_LABEL_HELP, null);
                activity.finish();
            }
        })
        ;

	    private final HasLabel hasLabel;
	    private final MenuOperation menuOperation;

	    private MenuControl(final String label, final MenuOperation menuOperation) {
            this.hasLabel = new HasLabel() {
                public String getLabel() {
                    return label;
                }
            };
            this.menuOperation = menuOperation;
        }

        public String getLabel() {
            return this.hasLabel.getLabel();
        }
        public void click(final Activity menuActivity) {
            this.menuOperation.click(menuActivity);
        }
	}
	
	private interface MenuOperation {
	    public void click(Activity activity);
	}

    private interface HasLabel {
        public String getLabel();
    }

}