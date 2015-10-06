
package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

class Daisy3XMLTagParagraphAction extends Daisy3XMLTagAction {
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		reader.getModelReader().beginParagraph();
	}

	protected void doAtEnd(Daisy3XMLReader reader) {
		reader.getModelReader().endParagraph();
	}
}
