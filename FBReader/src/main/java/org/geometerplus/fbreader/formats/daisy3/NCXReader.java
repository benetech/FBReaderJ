
package org.geometerplus.fbreader.formats.daisy3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.formats.util.MiscUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;


/**
 * NCX file reader for the daisy3 book
 * All the "navPoint" elements are stored in form
 * of a navigation map. This map is used or displaying 
 * table of contents.
 */
class NCXReader extends ZLXMLReaderAdapter {
	static class NavPoint {
		final int Order;
		final int Level;
		String Text = "";
		String id = "";
		int para;

		NavPoint(int order, int level) {
			Order = order;
			Level = level;			    
		}
	}
    
    static class Page {
        String text = "";
        String id = "";

        Page(String pageId) {
            id = pageId;
        }                                     
    }

	private final TreeMap<Integer,NavPoint> myNavigationMap = new TreeMap<Integer,NavPoint>();
    private LinkedHashMap<String, String> pageIdToPageText = new LinkedHashMap<String, String>();
    private Page currentPage;
	private final ArrayList<NavPoint> myPointStack = new ArrayList<NavPoint>();
	private BookReader myModelReader;
	private static final int READ_NONE = 0;
	private static final int READ_MAP = 1;
	private static final int READ_POINT = 2;
	private static final int READ_LABEL = 3;
	private static final int READ_TEXT = 4;
    private static final int READ_PAGE_LIST = 5;
    private static final int READ_PAGE_TARGET = 6;
    private static final int READ_PAGE_LABEL = 7;
    private static final int READ_PAGE_TEXT = 8;

	int myReadState = READ_NONE;
	int myPlayIndex = -65535;
	private String myLocalPathPrefix;


	NCXReader(BookReader modelReader) {
		myModelReader = modelReader;
	}

	boolean readFile(String filePath) {
		final ZLFile file = ZLFile.createFileByPath(filePath);
		myLocalPathPrefix = MiscUtil.archiveEntryName(MiscUtil.htmlDirectoryPrefix(file));
		return read(file);
	}

	Map<Integer,NavPoint> navigationMap() {
		return myNavigationMap;
	}
    
    LinkedHashMap<String,String> pageMap() {
        return pageIdToPageText;
    }

	private static final String TAG_NAVMAP = "navmap";
	private static final String TAG_NAVPOINT = "navpoint";
	private static final String TAG_NAVLABEL = "navlabel";
	private static final String TAG_CONTENT = "content";
	private static final String TAG_TEXT = "text";

	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_PLAYORDER = "playOrder";
    
    private static final String TAG_PAGE_LIST = "pagelist";
    private static final String TAG_PAGE_TARGET = "pagetarget";


	private int atoi(String number) {
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		switch (myReadState) {
			case READ_NONE:
				if (tag.equals(TAG_NAVMAP)) {
					myReadState = READ_MAP;
				} else if (tag.equals(TAG_PAGE_LIST)) {
                    myReadState = READ_PAGE_LIST;
                }
				break;
			case READ_MAP:
				if (tag.equals(TAG_NAVPOINT)) {
					final String order = attributes.getValue(ATTRIBUTE_PLAYORDER);
					final String id = attributes.getValue(ATTRIBUTE_ID);
					final int index = (order != null) ? atoi(order) : myPlayIndex++;
					NavPoint point = new NavPoint(index, myPointStack.size());
					// Adding id resolved the bug for first level TOC entries
					point.id = id;
					myPointStack.add(point);
					myReadState = READ_POINT;
				}
				break;
            case READ_PAGE_LIST:
                if (tag.equals(TAG_PAGE_TARGET)) {
                    final String id = attributes.getValue(ATTRIBUTE_ID);
                    currentPage = new Page(id);
                    myReadState = READ_PAGE_TARGET;
                }
                break;
			case READ_POINT:
				if (tag.equals(TAG_NAVPOINT)) {
					final String order = attributes.getValue(ATTRIBUTE_PLAYORDER);
					final String id = attributes.getValue(ATTRIBUTE_ID);
					final int index = (order != null) ? atoi(order) : myPlayIndex++;
					NavPoint navpoint = new NavPoint(index, myPointStack.size());
					navpoint.id = id;
					myPointStack.add(navpoint);
				} else if (tag.equals(TAG_NAVLABEL)) {
					myReadState = READ_LABEL;
				} else if (tag.equals(TAG_CONTENT)) {
					final int size = myPointStack.size();
					if (size > 0) {
					//	myPointStack.get(size - 1).id = myLocalPathPrefix + attributes.getValue("id");
					}
				}
				break;
            case READ_PAGE_TARGET:
                if (tag.equals(TAG_NAVLABEL)) {
                    myReadState = READ_PAGE_LABEL;
                }
                break;
			case READ_LABEL:
				if (TAG_TEXT.equals(tag)) {
					myReadState = READ_TEXT;
				}
				break;
            case READ_PAGE_LABEL:
                if (tag.equals(TAG_TEXT)) {
                    myReadState = READ_PAGE_TEXT;
                }
                break;
			case READ_TEXT:
				break;
		}
		return false;
	}
	
	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase().intern();
		switch (myReadState) {
			case READ_NONE:
				break;
			case READ_MAP:
				if (TAG_NAVMAP.equals(tag)) {
					myReadState = READ_NONE;
				} 
				break;
            case READ_PAGE_LIST:
                if (tag.equals(TAG_PAGE_LIST)) {
                    myReadState = READ_NONE;
                }
                break;
            case READ_PAGE_TARGET:
                if (tag.equals(TAG_PAGE_TARGET)) {
                    pageIdToPageText.put(currentPage.id, currentPage.text);
                    myReadState = READ_PAGE_LIST;
                }
                break;
			case READ_POINT:
				if (TAG_NAVPOINT.equals(tag)) {
					NavPoint last = myPointStack.get(myPointStack.size() - 1);
					if (last.Text.length() == 0) {
						last.Text = "...";
					}
					myNavigationMap.put(last.Order, last);
					myPointStack.remove(myPointStack.size() - 1);
					myReadState = (myPointStack.isEmpty()) ? READ_MAP : READ_POINT;
				}
			case READ_LABEL:
				if (TAG_NAVLABEL.equals(tag)) {
					myReadState = READ_POINT;
				}
				break;
            case READ_PAGE_LABEL:
                if (tag.equals(TAG_NAVLABEL)) {
                    myReadState = READ_PAGE_TARGET;
                }
                break;
			case READ_TEXT:
				if (TAG_TEXT.equals(tag)) {
					myReadState = READ_LABEL;
				} 
				break;
            case READ_PAGE_TEXT:
                if (tag.equals(TAG_TEXT)) {
                    myReadState = READ_PAGE_LABEL;
                }
                break;
		}
		return false;
	}
	
	@Override
	public void characterDataHandler(char[] ch, int start, int length) {
		if (myReadState == READ_TEXT) {
			final ArrayList<NavPoint> stack = myPointStack;
			final NavPoint last = stack.get(stack.size() - 1);
			last.Text += new String(ch, start, length);
		} else if (myReadState == READ_PAGE_TEXT) {
            currentPage.text = new String(ch, start, length);
        }
	}

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}
}
