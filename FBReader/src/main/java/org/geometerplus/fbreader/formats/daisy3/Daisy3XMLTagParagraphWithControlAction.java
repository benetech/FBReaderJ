
package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

class Daisy3XMLTagParagraphWithControlAction extends Daisy3XMLTagAction {
	final byte myControl;

	Daisy3XMLTagParagraphWithControlAction(byte control) {
		myControl = control;
	}

	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		if ((myControl == FBTextKind.TITLE) &&
				(modelReader.Model.BookTextModel.getParagraphsNumber() > 1)) {
			modelReader.insertEndOfSectionParagraph();
		}
		modelReader.pushKind(myControl);
		modelReader.beginParagraph();
	}

	protected void doAtEnd(Daisy3XMLReader reader) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.endParagraph();
		modelReader.popKind();
	}
}
