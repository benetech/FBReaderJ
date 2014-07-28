package org.geometerplus.fbreader.formats.daisy3;

import java.util.LinkedHashMap;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * Control Action class when a level tag is encountered in the
 * XML file. Primarily used for construction of the
 * mapping from TOC entry to paragraph number.
 */
class Daisy3XMLTagPageControlAction extends Daisy3XMLTagAction {

	private static Daisy3XMLTagPageControlAction instance = null;
	private final static LinkedHashMap<String,Integer> pageNumToParagraph = new LinkedHashMap<String, Integer>();
		
	/**
	 * Obtain a singleton instance of the class.
	 * @return Daisy3XMLTagLevelControlAction Singleton instance of this class.
	 */
	public static Daisy3XMLTagPageControlAction getInstance(){
		if(instance == null){
			instance = new Daisy3XMLTagPageControlAction();
		}
		return instance;
	}

	/**
	 * Add an entry in the HashMap for TOC<=>paragraph mapping.
	 * @param id String Indicates an entry in the table of contents.
	 * @param para Paragraph number in the XML file.
	 */
	void storeParagraphNumforPage(String id, int para){
        pageNumToParagraph.put(id, para);
	}

	/**
	 * Get the HashMap containing TOC<=>paragraph mappings.
	 * @return HashMap Containing TOC<=>paragraph mappings.
	 */
	public static LinkedHashMap<String,Integer> getPageNumToParagraphMap(){
		return pageNumToParagraph;
	}
	
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		// Do nothing
	}

	protected void doAtEnd(Daisy3XMLReader reader) {
		// Do nothing
	}
}
