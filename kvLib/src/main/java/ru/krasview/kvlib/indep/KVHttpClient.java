package ru.krasview.kvlib.indep;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import ru.krasview.kvlib.interfaces.OnLoadCompleteListener;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

public class KVHttpClient {
	OkHttpClient httpClient = new OkHttpClient();

	String run(String url) throws IOException {
		Request request = new Request.Builder()
				.url(url)
				.header("User-Agent", "krasview 2.0")
				.build();

		try (Response response = httpClient.newCall(request).execute()) {
			return response.body().string();
		}
	}

	public static String getXML(String address, String params) {
		return getXML(addParams(address, params));
	}

	public static String getXML(String address) {
		String line = "";
		KVHttpClient client = new KVHttpClient();
		try {
/*		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpGet httpGet = new HttpGet(address);
		httpGet.setHeader("User-Agent", "krasview 2.0");
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity != null){
			line = EntityUtils.toString(httpEntity, "UTF-8");
		}*/
			line = client.run(address);
		} catch (UnsupportedEncodingException e) {
			line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
		} catch (MalformedURLException e) {
			line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
		} catch (IOException e) {
			line = "<results status=\"error\"><msg>Can't connect to server</msg></results>";
		}
		return line;
	}

	protected static String addParams(String address, String params) {
		if(address == null || Uri.parse(address) == null) {
			return "";
		}
		if(Uri.parse(address) != null && Uri.parse(address).getQuery() == null) {
			address = address + "?";
		} else {
			address = address + "&";
		}
		if(params != null && params.length() != 0) {
			address = address + params + "&";
		}
		return address;
	}

	public static void getXMLAsync(String address, String params, OnLoadCompleteListener listener) {
		getXMLAsyncTask task = new getXMLAsyncTask();
		task.execute(address, params, listener);
	}

	private static class getXMLAsyncTask extends AsyncTask<Object, Object, String> {
		OnLoadCompleteListener listener1;
		String address = null;

		@Override
		protected String doInBackground(Object... params) {
			listener1 = (OnLoadCompleteListener)params[2];
			address = (String)params[0];
			return getXML(address, (String)params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			listener1.loadComplete(address, result);
			listener1.loadComplete(result);
		}
	};

	public static Bitmap getImage(String adress) {
		URL url = null;
		Bitmap bmp;
		try {
			url = new URL(adress);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		conn.setDoInput(true);
		try {
			conn.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InputStream is = null;
		try {
			is = conn.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bmp = BitmapFactory.decodeStream(is);
		return bmp;
	}

	public static String getXMLFromFile(String addres, Context context) {
		String xmlString = null;
		AssetManager am = context.getAssets();
		try {
			InputStream is = am.open(addres);
			int length = is.available();
			byte[] data = new byte[length];
			is.read(data);
			xmlString = new String(data);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return xmlString;
	}
}
