package ru.krasview.kvlib.widget.lists;

import java.util.Map;



import android.content.Context;

import ru.krasview.kvlib.ApiConst;

public class LetterMovieList extends LetterShowList {
	public LetterMovieList(Context context, Map<String, Object> map) {
		super(context, map);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getApiAddress() {
		return ApiConst.MOVIE + "/letter/" + getMap().get("name");
	}
}
