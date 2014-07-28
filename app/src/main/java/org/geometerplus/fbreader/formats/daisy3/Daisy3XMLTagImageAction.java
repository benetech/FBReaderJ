package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.formats.util.MiscUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class Daisy3XMLTagImageAction extends Daisy3XMLTagAction {

    private static final String ALT_TAG = "alt";
    private static final String IMG_ID = "image.";

	private final String myNamespace;
	private final String myNameAttribute;
	
	Daisy3XMLTagImageAction(String namespace, String nameAttribute) {
		myNamespace = namespace;
		myNameAttribute = nameAttribute;
	}

	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {

		String fileName = reader.getAttributeValue(xmlattributes, myNamespace, myNameAttribute);
        final BookReader modelReader = reader.getModelReader();
        boolean wasParagraphOpen = modelReader.paragraphIsOpen() &&
                !modelReader.paragraphIsNonEmpty();
		if (fileName != null) {
			fileName = MiscUtil.decodeHtmlReference(fileName);
			final ZLFile imageFile = ZLFile.createFileByPath(reader.myPathPrefix + fileName);
			if (imageFile != null) {
				if (wasParagraphOpen) {
					modelReader.endParagraph();
				}
				final String imageName = imageFile.getLongName();
				modelReader.addImageReference(imageName, (short)0, false);
				modelReader.addImage(imageName, new ZLFileImage(MimeType.IMAGE_AUTO, imageFile));

				if (wasParagraphOpen) {
					modelReader.beginParagraph();
				}
			}
		}

        final String altText = reader.getAttributeValue(xmlattributes, myNamespace, ALT_TAG);
        if (altText != null && !altText.trim().equals("")) {
            if (!wasParagraphOpen) {
                modelReader.beginParagraph();
            }

            // TODO: Alt text shouldn't be displayed on the screen
            modelReader.addData((altText + ". " + IMG_ID).toCharArray());

            if (!wasParagraphOpen) {
                modelReader.endParagraph();
            }
        }
	}

	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
		// TODO Auto-generated method stub
		
	}


}
