package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.formats.util.MiscUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class Daisy3XMLTagImageAction extends Daisy3XMLTagAction {
	
	private final String myNamespace;
	private final String myNameAttribute;
	
	Daisy3XMLTagImageAction(String namespace, String nameAttribute) {
		myNamespace = namespace;
		myNameAttribute = nameAttribute;
	}

	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		String fileName = reader.getAttributeValue(xmlattributes, myNamespace, myNameAttribute);
		if (fileName != null) {
			fileName = MiscUtil.decodeHtmlReference(fileName);
			final ZLFile imageFile = ZLFile.createFileByPath(reader.myPathPrefix + fileName);
			if (imageFile != null) {
				final BookReader modelReader = reader.getModelReader();
				boolean flag = modelReader.paragraphIsOpen() && !modelReader.paragraphIsNonEmpty();
				if (flag) {
					modelReader.endParagraph();
				}
				final String imageName = imageFile.getLongName();
				modelReader.addImageReference(imageName, (short)0, false);
				modelReader.addImage(imageName, new ZLFileImage(MimeType.IMAGE_AUTO, imageFile));
				if (flag) {
					modelReader.beginParagraph();
				}
			}
		}
	}

	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
		// TODO Auto-generated method stub
		
	}


}
