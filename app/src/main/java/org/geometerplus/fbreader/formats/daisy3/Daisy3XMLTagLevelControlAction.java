
package org.geometerplus.fbreader.formats.daisy3;

import java.util.LinkedHashMap;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * Control Action class when a level tag is encountered in the
 * XML file. Primarily used for construction of the
 * mapping from TOC entry to paragraph number.
 */
class Daisy3XMLTagLevelControlAction extends Daisy3XMLTagAction {

	private static Daisy3XMLTagLevelControlAction instance = null;
	private final static LinkedHashMap<String,Integer> toc_paragraph_map = new LinkedHashMap<String, Integer>();
		
	/**
	 * Obtain a singleton instance of the class.
	 * @return Daisy3XMLTagLevelControlAction Singleton instance of this class.
	 */
	public static Daisy3XMLTagLevelControlAction getInstance(){
		if(instance == null){
			instance = new Daisy3XMLTagLevelControlAction();
		}
		return instance;
	}

	/**
	 * Add an entry in the HashMap for TOC<=>paragraph mapping.
	 * @param id String Indicates an entry in the table of contents.
	 * @param para Paragraph number in the XML file.
	 */
	void storeParagraphNumforLevel(String id, int para){
		toc_paragraph_map.put(id, para);
	}

	/**
	 * Get the HashMap containing TOC<=>paragraph mappings.
	 * @return HashMap Containing TOC<=>paragraph mappings.
	 */
	public static LinkedHashMap<String,Integer> getToc_paragraph_map(){
		return toc_paragraph_map;
	}
	
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		// Do nothing
	}

	protected void doAtEnd(Daisy3XMLReader reader) {
		// Do nothing
	}
}
