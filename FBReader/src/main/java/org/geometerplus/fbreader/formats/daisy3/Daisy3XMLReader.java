
package org.geometerplus.fbreader.formats.daisy3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.formats.util.MiscUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLArchiveEntryFile;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

/**
 * Reader class for a Daisy3 XML file
 *
 */
public class Daisy3XMLReader extends ZLXMLReaderAdapter {

	private static final HashMap<String,Daisy3XMLTagAction> ourTagActions = new HashMap<String,Daisy3XMLTagAction>();
	private final BookReader myModelReader;
	String myPathPrefix;
	String myLocalPathPrefix;
	String myReferencePrefix;
	boolean myPreformatted;
	boolean myInsideBody;
	private final Map<String,Integer> myFileNumbers;
	
	private static final String[] BLOCK_ELEMENTS = {"blockquote", "bodymatter", "book", 
		"byline", "cite", "covertitle", "div", "docauthor", "doctitle", "dateline", 
		"epigraph", "frontmatter", "head", 
		"line", "rearmatter", "sidebar", "th", "td"
    };
	
	private static final String[] NO_FORMAT_ELEMENTS = {"abbr", "acronym", "address", "annoref",
		"annotation", "author", "bdo", "bridgehead", "code", "col", "colgroup", "dd",
		"dfn", "dl", "dt", "dtbook", "imggroup", "kbd", "lic", 
		"linegroup", "linenum", "link", "meta", "note", "noteref", "q",
		"samp", "sent", "span", "sub", "sup", "title", "w", "thead", "tbody", "tr", "table"
	};

	/**
	 * Add action class corresponding to the tag into the HashMap.
	 * @param tag Tag in the XML file.
	 * @param action Daisy3XMLTagAction class to be activated when the tag is encountered.
	 * @return Daisy3XMLTagAction Earlier Value which was replaced.
	 */
	public static Daisy3XMLTagAction addAction(String tag, Daisy3XMLTagAction action) {
		Daisy3XMLTagAction old = (Daisy3XMLTagAction)ourTagActions.get(tag);
		ourTagActions.put(tag, action);
		return old;
	}

	/**
	 * Fill the HashMap with tag<=>Daisy3XMLTagAction mappings.
	 */
	public static void fillTagTable() {
		if (!ourTagActions.isEmpty()) {
			return;
		}

		addAction("p", new Daisy3XMLTagParagraphAction());
		addAction("h1", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H1));
		addAction("h2", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H2));
		addAction("h3", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H3));
		addAction("h4", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H4));
		addAction("h5", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H5));
		addAction("h6", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.H6));
		addAction("hd", new Daisy3XMLTagParagraphAction());
		
		addAction("poem", new Daisy3XMLTagParagraphWithControlAction(FBTextKind.VERSE));
		
		addAction("level", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level1", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level2", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level3", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level4", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level5", Daisy3XMLTagLevelControlAction.getInstance());
		addAction("level6", Daisy3XMLTagLevelControlAction.getInstance());
		
		addAction("br", new Daisy3XMLTagRestartParagraphAction());
		
		//Image descriptions.
		addAction("prodnote",  new Daisy3XMLTagAnnotatedWithControlAction(FBTextKind.PRODNOTE,
				"Image Description.", "End Image Description."));
		addAction("caption",  new Daisy3XMLTagAnnotatedWithControlAction(FBTextKind.PRODNOTE,
				"Caption.", "End Caption."));
		
		//Special formatting.
		addAction("strong", new Daisy3XMLTagControlAction(FBTextKind.STRONG));
		addAction("em", new Daisy3XMLTagControlAction(FBTextKind.ITALIC));
		
		addAction("li", Daisy3XMLTagListAction.getInstance());
		addAction("a", new Daisy3XMLTagHyperlinkAction());
		addAction("img", new Daisy3XMLTagImageAction(null, "src"));
        addAction("pagenum", Daisy3XMLTagPageControlAction.getInstance());
		
		for (final String tag : BLOCK_ELEMENTS) {
			addAction(tag, new Daisy3XMLTagParagraphAction());
		}
		
		for (final String tag : NO_FORMAT_ELEMENTS) {
			addAction(tag, new Daisy3XMLTagControlAction(FBTextKind.REGULAR));
		}
	}

	/**
	 * Constructor
	 * @param modelReader BookReader.
	 * @param fileNumbers Map<String, Integer>.
	 */
	public Daisy3XMLReader(BookReader modelReader, Map<String,Integer> fileNumbers) {
		myModelReader = modelReader;
		myFileNumbers = fileNumbers;
	}

	/**
	 * Get the BookReader instance.
	 * @return Bookreader 
	 */
	final BookReader getModelReader() {
		return myModelReader;
	}

	
	/**
	 * Read this Daisy3 XML file.
	 * @param file ZLFile Represents the XML file.
	 * @param referencePrefix String used in accordance with other ZLXMLReader(s).
     * @return
	 */
	public boolean readFile(ZLFile file, String referencePrefix) {
		fillTagTable();

		myReferencePrefix = referencePrefix;

		myPathPrefix = MiscUtil.htmlDirectoryPrefix(file);
		myLocalPathPrefix = MiscUtil.archiveEntryName(myPathPrefix);

		myPreformatted = false;
		myInsideBody = false;

		return read(file);
	}

	/**
	 * Tag handler method when a tag parsing starts.
	 * @param tag String representing the XML tag.
	 * @param attributes ZLStringMap.
	 * @return boolean Indicates status of operation.
	 */
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		String id = attributes.getValue("id");
		if (id != null) {
			myModelReader.addHyperlinkLabel(myReferencePrefix + id);
		}

		tag = tag.toLowerCase();
		
		if (tag.equals("list")) {
			Daisy3XMLTagListAction.getInstance().startList(this, attributes);
        } else if (tag.equals("pagenum")) {
            Daisy3XMLTagAction action = ourTagActions.get(tag);
            if (action != null) {
                Daisy3XMLTagPageControlAction pageAction =  (Daisy3XMLTagPageControlAction)action;
                pageAction.storeParagraphNumforPage(id, myModelReader.Model.BookTextModel.getParagraphsNumber());
                action.doAtStart(this, attributes);
            }
		} else {
			Daisy3XMLTagAction action = ourTagActions.get(tag);
			if (action != null) {
				if(tag=="level1" || tag=="level2" || tag=="level3"){
					Daisy3XMLTagLevelControlAction level_action = (Daisy3XMLTagLevelControlAction)action;
					level_action.storeParagraphNumforLevel(id, myModelReader.Model.BookTextModel.getParagraphsNumber());
				}
				action.doAtStart(this, attributes);
			}
		}
		return false;
	}

	/**
	 * Tag handler when a tag parsing ends.
	 * @param tag String indicating tag whose parsing has ended.
	 * @return Indicates status of operation.
	 */
	public boolean endElementHandler(String tag) {
		if (tag.toLowerCase().equals("list")) {
			Daisy3XMLTagListAction.getInstance().endList(this);
		} else {
			Daisy3XMLTagAction action = ourTagActions.get(tag.toLowerCase());
			if (action != null) {
				action.doAtEnd(this);
			}
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int len) {
		if (myPreformatted) {
			final char first = data[start]; 
			if ((first == '\r') || (first == '\n')) {
				myModelReader.addControl(FBTextKind.CODE, false);
				myModelReader.endParagraph();
				myModelReader.beginParagraph();
				myModelReader.addControl(FBTextKind.CODE, true);
			}
			int spaceCounter = 0;
cycle:
			while (spaceCounter < len) {
				switch (data[start + spaceCounter]) {
					case 0x08:
					case 0x09:
					case 0x0A:
					case 0x0B:
					case 0x0C:
					case 0x0D:
					case ' ':
						break;
					default:
						break cycle;
				}
				++spaceCounter;
			}
			myModelReader.addFixedHSpace((short)spaceCounter);
			start += spaceCounter;
			len -= spaceCounter;
		}
		if (len > 0) {
			if (myInsideBody && !myModelReader.paragraphIsOpen()) {
				myModelReader.beginParagraph();
			}
			myModelReader.addData(data, start, len, false);
		}
	}

	private static ArrayList<String> ourExternalDTDs = new ArrayList<String>();

	
	public static List<String> xhtmlDTDs() {
		if (ourExternalDTDs.isEmpty()) {
			ourExternalDTDs.add("formats/xhtml/xhtml-lat1.ent");
			ourExternalDTDs.add("formats/xhtml/xhtml-special.ent");
			ourExternalDTDs.add("formats/xhtml/xhtml-symbol.ent");
		}
		return ourExternalDTDs;
	}

	@Override
	public List<String> externalDTDs() {
		return xhtmlDTDs();
	}

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}
	
	@Override
	public boolean processNamespaces() {
		return true;
	}
	
	public void namespaceMapChangedHandler(Map<String,String> namespaceMap) {
	}
	
	public final String getFileAlias(String fileName) {
		fileName = MiscUtil.decodeHtmlReference(fileName);
		fileName = ZLArchiveEntryFile.normalizeEntryName(fileName);
		Integer num = myFileNumbers.get(fileName);
		if (num == null) {
			num = myFileNumbers.size();
			myFileNumbers.put(fileName, num);
		}
		return num.toString();
	}
}
