package ru.krasview.kvlib.widget.lists;


import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class SearchMovieList extends SearchShowList {
	public SearchMovieList(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.MOVIE;
	}
}
