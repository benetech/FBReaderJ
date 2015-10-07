package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * Handle DAISY Structural elements.
 * @author meghan larson
 *
 */
public class Daisy3XMLTagAnnotatedWithControlAction extends Daisy3XMLTagAction {

	final byte myControl;
	final String myPrelude;
	final String myPostlude;

	/**
	 * Default Constructor.
	 * @param control not null
	 * @param prelude not null
	 * @param postlude not null
	 */
	Daisy3XMLTagAnnotatedWithControlAction(byte control, String prelude, String postlude) {
		myControl = control;
		myPrelude = prelude;
		myPostlude = postlude;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.addControl(myControl, true);
		modelReader.beginParagraph();
		modelReader.addData(myPrelude.toCharArray());
		modelReader.endParagraph();
		modelReader.beginParagraph();
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.beginParagraph();
		modelReader.addData(myPostlude.toCharArray());
		modelReader.endParagraph();
		modelReader.addControl(myControl, false);
		
	}
}

