package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * End and start paragraph.
 * @author meghan larson
 *
 */
public class Daisy3XMLTagRestartParagraphAction extends Daisy3XMLTagAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		reader.getModelReader().endParagraph();
		reader.getModelReader().beginParagraph();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
	}

}
