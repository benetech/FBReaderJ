package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * Parse tags with controls.
 * @author meghan larson
 *
 */
public class Daisy3XMLTagControlAction extends Daisy3XMLTagAction {

	final byte myControl;
	
	public Daisy3XMLTagControlAction(final byte control) {
		myControl = control;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.pushKind(myControl);
		modelReader.addControl(myControl, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.addControl(myControl, false);
		modelReader.popKind();
	}
	
}
