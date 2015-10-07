
package org.geometerplus.fbreader.formats.daisy3;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public abstract class Daisy3XMLTagAction {
	protected abstract void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes);
	protected abstract void doAtEnd(Daisy3XMLReader reader);
};
