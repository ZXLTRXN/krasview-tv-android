package ru.krasview.kvlib.widget.lists;

import java.util.Map;



import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class LetterAnimeList extends LetterShowList {
	public LetterAnimeList(Context context, Map<String, Object> m) {
		super(context, m);
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.LETTER_ANIME + getMap().get("name");
	}
}
