package ru.krasview.kvlib.widget.lists;


import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class SearchAnimeList extends SearchShowList {
	public SearchAnimeList(Context context) {
		super(context);
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.ANIME;
	}
}
