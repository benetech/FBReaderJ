package org.geometerplus.fbreader.formats.daisy3;

import java.util.Stack;

import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

/**
 * Handle lists.
 * @author meghan larson
 *
 */
public class Daisy3XMLTagListAction extends Daisy3XMLTagAction {
	
	private static final char[] BULLET = { '\u2022', '\240' };
	private static final char[] BASE26CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray(); 
	
	private static Daisy3XMLTagListAction instance = null;
	private Stack<ListTypeItem> listStack = new Stack<ListTypeItem>();
	
	private static class ListTypeItem {
		private final ListType listType;
		private final boolean alphabetic;
		private Integer itemNumber;
		
		
		public ListTypeItem(final ListType listType, final boolean alphabetic, final int start) {
			this.listType = listType;
			this.alphabetic = alphabetic;
			this.itemNumber = start;
		}
		
		public void incrementItemNumber() {
			this.itemNumber++;
		}
		
		public ListType getListType() {
			return listType;
		}
		
		public Integer getItemNumber() {
			return itemNumber;
		}
		
		public boolean isAlphabetic() {
			return alphabetic;
		}
	}
	
    private enum ListType {
    	UL("ul"), OL("ol"), PL("pl");
    	
    	private String name;
    	
    	/**
    	 * Default constructor.
    	 * @param name not null
    	 */
    	private ListType(final String name) {
    		this.name = name;
    	}
    	
    	/**
    	 * @return name
    	 */
    	public String getName() {
    		return name;
    	}
    }
    
	/**
	 * Default constructor.
	 */
	public static Daisy3XMLTagListAction getInstance() {
		if (instance == null) {
			instance = new Daisy3XMLTagListAction();
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtStart(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		final BookReader modelReader = reader.getModelReader();
		modelReader.beginParagraph();
		final ListTypeItem currentList = !listStack.isEmpty() 
				? listStack.peek() : new ListTypeItem(ListType.UL, false, 1);
		if (currentList.getListType().equals(ListType.UL)) {
			modelReader.addData(BULLET);
		} else if (currentList.getListType().equals(ListType.OL)){
			final StringBuilder itemDesc = new StringBuilder();
			if (!currentList.isAlphabetic()) {
			    itemDesc.append(currentList.getItemNumber());
			} else {
				itemDesc.append(getAlphabeticValue(currentList.getItemNumber()));
			}
			modelReader.addData(itemDesc.append(". ").toString().toCharArray());
			//Increment item.
		    currentList.incrementItemNumber();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doAtEnd(Daisy3XMLReader reader) {
		reader.getModelReader().endParagraph();
	}
	
	/**
	 * Add list type to stack.
	 * @param xmlattributes not null
	 */
	public void startList(Daisy3XMLReader reader, ZLStringMap xmlattributes) {
		final String type = xmlattributes.getValue("type");
		ListTypeItem list = null;
		if (type != null) {
			for (final ListType lt : ListType.values()) {
				if (lt.getName().equals(type.toLowerCase())) {
					boolean alphabetic = false;
					if (lt.equals(ListType.OL)) {
						final String enumValue = xmlattributes.getValue("enum");
						if (enumValue != null && enumValue.toLowerCase().equals("a")) {
							alphabetic = true;
						}
					}
					//See if start attribute was specified.
					int start = 1;
					if (xmlattributes.getValue("start") != null) {
						start = Integer.valueOf(xmlattributes.getValue("start"));
					}
					list = new ListTypeItem(lt, alphabetic, start);
					break;
				}
			}
		} 
		listStack.push(list != null ? list : new ListTypeItem(ListType.UL, false, 1));
		reader.getModelReader().beginParagraph();
	}
	
	/**
	 * End list.
	 */
	public void endList(Daisy3XMLReader reader) {
		reader.getModelReader().endParagraph();
		listStack.pop();
	}

	/**
	 * Get alphabetic value of int.
	 * @param itemNumber not null
	 */
	private String getAlphabeticValue(final Integer itemNumber) {
		String returnValue = "";
		int alphabetIndex = itemNumber;
		do {
			returnValue = BASE26CHARS[alphabetIndex % 26] + returnValue;
			alphabetIndex /= 26;
		} while (alphabetIndex-- != 0);
		return returnValue;
	}
}
