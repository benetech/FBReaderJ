package org.geometerplus.android.fbreader.network.bookshare.socialnetworks;

import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Edition_Metadata_Bean;
import org.geometerplus.android.fbreader.network.bookshare.Bookshare_Metadata_Bean;

import android.text.TextUtils;

public class SocialNetowkPosts {

	public static String getBookPost(Bookshare_Metadata_Bean bean) {
		StringBuilder bookPostBuilder = new StringBuilder();
		bookPostBuilder
				.append("Hi friends, I found this interesting book on Bookshare. You might like it too!");
		if (bean.getTitle() != null && bean.getTitle().length > 0) {
			bookPostBuilder.append("\n\n\tTitle: "
					+ strArrayToString(bean.getTitle()));
		}
		if (bean.getAuthors() != null && bean.getAuthors().length > 0) {
			bookPostBuilder.append("\n\tAuthor: "
					+ strArrayToString(bean.getAuthors()));
		}
		if (bean.getIsbn() != null && !TextUtils.isEmpty(bean.getIsbn())) {
			bookPostBuilder.append("\n\tISBN: " + bean.getIsbn());
		}
		if (bean.getCategory() != null && bean.getCategory().length > 0) {
			bookPostBuilder.append("\n\tCategory: "
					+ strArrayToString(bean.getCategory()));
		}

		return bookPostBuilder.toString();
	}

	public static String getShortenedBookPost(Bookshare_Metadata_Bean bean) {
		StringBuilder shortBookPostBuilder = new StringBuilder();
		shortBookPostBuilder.append("Hi, I found this on Bookshare.");
		if (bean.getTitle() != null && bean.getTitle().length > 0) {
			shortBookPostBuilder.append(" Title: "
					+ strArrayToString(bean.getTitle()));
		}
		if (shortBookPostBuilder.length() < 125 && bean.getIsbn() != null
				&& !TextUtils.isEmpty(bean.getIsbn())) {
			shortBookPostBuilder.append(" ISBN: " + bean.getIsbn());
		}
		if (shortBookPostBuilder.length() < 125 && bean.getCategory() != null
				&& bean.getCategory().length > 0) {
			shortBookPostBuilder.append(" Category: ");
			for (String cat : bean.getCategory()) {

				if (shortBookPostBuilder.length() + cat.length() + 1 < 140) {
					shortBookPostBuilder.append(cat + " ");
				}
			}

		}

		return shortBookPostBuilder.toString();
	}

	public static String getShortenedPeriodicalPost(
			Bookshare_Edition_Metadata_Bean bean) {
		StringBuilder shortPeriodicalBuilder = new StringBuilder();
		shortPeriodicalBuilder.append("Hi, I found a periodical on Bookshare.");
		if (bean.getTitle() != null && bean.getTitle().length() > 0) {
			shortPeriodicalBuilder.append(" Title: " + bean.getTitle());
		}
		if (bean.getEdition() != null && !TextUtils.isEmpty(bean.getEdition())) {
			shortPeriodicalBuilder.append(" Edition: "
					+ formatEdition(bean.getEdition()));
		}
		if (bean.getCategory() != null && bean.getCategory().length() > 0
				&& bean.getCategory().length() < 20) {
			if (shortPeriodicalBuilder.length() < 125) {
				shortPeriodicalBuilder.append(" Category: "
						+ bean.getCategory());
			}
		}

		return shortPeriodicalBuilder.toString();
	}

	public static String getPeriodicalPost(Bookshare_Edition_Metadata_Bean bean) {
		StringBuilder periodicalBuilder = new StringBuilder();
		periodicalBuilder
				.append("Hi friends, I found this interesting periodical on Bookshare. You might like it too!");

		if (bean.getTitle() != null && bean.getTitle().length() > 0) {
			periodicalBuilder.append("\n\tTitle: " + bean.getTitle());
		}
		if (bean.getEdition() != null && !TextUtils.isEmpty(bean.getEdition())) {
			periodicalBuilder.append("\n\tEdition: "
					+ formatEdition(bean.getEdition()));
		}
		if (bean.getCategory() != null && bean.getCategory().length() > 0) {
			periodicalBuilder.append("\n\tCategory: " + bean.getCategory());
		}

		return periodicalBuilder.toString();
	}

	private static String formatEdition(String edition) {
		if (edition != null) {
			String month = edition.substring(0, 2);
			String date = edition.substring(2, 4);
			String year = edition.substring(4, 8);

			return month + "/" + date + "/" + year;
		}
		return null;
	}

	private static String strArrayToString(String[] arr) {
		String concatString = "";
		if (arr == null || arr.length == 0 || arr[0] == null) {
			return "Not available";
		}
		if (arr.length == 1) {
			return arr[0];
		}
		for (int i = 0; i < arr.length; i++) {

			if (i == arr.length - 1) {
				concatString += arr[i];
			} else {
				concatString += arr[i] + ", ";
			}
		}
		return concatString;
	}
}
