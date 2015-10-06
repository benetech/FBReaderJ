package org.geometerplus.fbreader.formats.daisy3;

import java.util.List;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

/**
 * This class acts as a plugin for ebooks in daisy3 format
 *
 */
public class Daisy3Plugin extends FormatPlugin {
	public boolean acceptsFile(ZLFile file) {
		boolean flagncx = false;
		boolean flagxml = false;
		final String extension = file.getExtension().intern();
		if(extension == "opf"){
			ZLFile parentDirectory = file.getParent();
			List<ZLFile> children =  parentDirectory.children();
			for(ZLFile daisy3content : children){
				if(daisy3content.getExtension() == "ncx"){
					flagncx = true;
				}
				if(daisy3content.getExtension() == "xml"){
					flagxml = true;
				}
				if(flagncx && flagxml) return true;
			}
		}
		return false;
	}

	private ZLFile getOpfFile(ZLFile oebFile) {
		if (oebFile.getExtension().equals("opf")) {
			return oebFile;
		}

		for (ZLFile child : oebFile.children()) {
			if (child.getExtension().equals("opf")) {
				return child;
			}
		}
		return null;
	}

	@Override
	public boolean readMetaInfo(Book book) {
		
		// Read the opf file that contains the meta info for the book
		final ZLFile opfFile = getOpfFile(book.File);
		return (opfFile != null) ? new Daisy3MetaInfoReader(book).readMetaInfo(opfFile) : false;
	}
	
	@Override
	public boolean readModel(BookModel model) {
		model.Book.File.setCached(true);
		final ZLFile opfFile = getOpfFile(model.Book.File);
		return (opfFile != null) ? new Daisy3OPFReader(model).readBook(opfFile) : false;
	}

    @Override
    public ZLImage readCover(ZLFile file) {
        //todo: Rom
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String readAnnotation(ZLFile file) {
        //todo: Rom
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	public ZLImage readCover(Book book) {
		final ZLFile opfFile = getOpfFile(book.File);
//		return (opfFile != null) ? new OEBCoverReader().readCover(opfFile) : null;
		return null;
	}
}
