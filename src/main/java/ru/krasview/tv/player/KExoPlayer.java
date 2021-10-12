package ru.krasview.tv.player;

import java.util.Map;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
//import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
//import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.Player.EventListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Context;
import android.app.AlertDialog;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import static ru.krasview.tv.player.VideoController.mVideo;


// todo: https://developer.android.com/codelabs/exoplayer-intro#0
public class KExoPlayer extends SurfaceView implements VideoInterface, EventListener {
	private SurfaceView mSurface;
	SimpleExoPlayer player;
    PlayerView StyledPlayerView;
	DefaultHttpDataSourceFactory dataSourceFactory;
    DefaultTrackSelector trackSelector;

	TVController mTVController;
	VideoController mVideoController;
	Map<String, Object> mMap;
	public final static String TAG = "Krasview/KExoPlayer";
	public boolean is_playing = false;

	String pref_aspect_ratio = "default";
	String pref_aspect_ratio_video = "default";

	public KExoPlayer(Context context, PlayerView view) {
		super(context);
		StyledPlayerView = view;
		init();
	}

	private void init() {
		mSurface = this;

		// 1. Create a default TrackSelector
		//BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		//TrackSelection.Factory TrackSelectionFactory = new FixedTrackSelection.Factory();
		trackSelector = new DefaultTrackSelector(getContext());
		trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage("au1"));

		// 3. Create the player
		player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
		StyledPlayerView.requestFocus();
		StyledPlayerView.setPlayer(player);

		//DefaultHttpDataSourceFactory http
		dataSourceFactory = new DefaultHttpDataSourceFactory("http://kadu.ru", null);
		dataSourceFactory.getDefaultRequestProperties().set("Referer", "https://krasview.ru");
		//dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "krasview"), null);
		//dataSourceFactory = new DefaultDataSourceFactory(getContext(), "http://kadu.ru", httpDataSourceFactory);
	}

	private void setSize() {
		if(pref_aspect_ratio_video.equals("default")) {
			StyledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
		} else if(pref_aspect_ratio_video.equals("fullscreen")) {
			StyledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
		} else {
			StyledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
		}
		calcSize();
	}

	private void calcSize() {
		// get screen size
		int w = ((Activity)this.getContext()).getWindow().getDecorView().getWidth();
		int h = ((Activity)this.getContext()).getWindow().getDecorView().getHeight();
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		// sanity check
		if (w * h == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}
		if (w > h && isPortrait || w < h && !isPortrait) {
			int d = w;
			w = h;
			h = d;
		}

		double ar = 1;
		double dar = (double) w / (double) h;
		//double mult = dar;
		if(pref_aspect_ratio_video.equals("4:3")) {
			ar = 4.0 / 3.0;
			//ar = ar/mult*dar;
			if (dar < ar)
				h = (int) (w / ar);
			else
				w = (int) (h * ar);
		}
		if(pref_aspect_ratio_video.equals("16:9")) {
			ar = 16.0 / 9.0;
			//ar = ar/mult*dar;
			if (dar < ar)
				h = (int) (w / ar);
			else
				w = (int) (h * ar);
		}
		getHolder().setFixedSize(w, h);
		forceLayout();
		invalidate();
	}

	private void getPrefs() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		pref_aspect_ratio = prefs.getString("aspect_ratio", "default");
		if(mMap.get("type").equals("video")) {
			pref_aspect_ratio_video = prefs.getString("aspect_ratio_video", "default");
		} else {
			pref_aspect_ratio_video = prefs.getString("aspect_ratio_tv", "default");
		}
		Log.d(TAG, "aspect ratio: " + pref_aspect_ratio_video);
	}

	private void displayTrackSelector(Activity activity) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if(mappedTrackInfo != null) {
            /*Pair<AlertDialog, TrackSelectionDialogBuilder> dialogPair =
					TrackSelectionDialogBuilder.getDialog(activity, "Звуковая дорожка", trackSelector, 1);
            dialogPair.second.setShowDisableOption(true);
            //dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
            dialogPair.first.show();*/
			new TrackSelectionDialogBuilder(activity, "Звуковая дорожка",trackSelector,1)
					.setShowDisableOption(true)
					.build()
					.show();
        }
    }

	@Override
	public void setVideoAndStart(String address) {
        Log.d("ExoPlayer", "setVideoAndStart");
        Uri uri = Uri.parse(address);
        MediaSource mediaSource;
        if (address.indexOf("mpd") != -1) {
            mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                    .createMediaSource(uri);
        } else {
            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            if (address.indexOf("t.kraslan.ru") != -1)
                extractorsFactory.setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS | DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .setExtractorsFactory(extractorsFactory)
                    .createMediaSource(uri);
        }
        if(player == null) return; // sanity check
        player.prepare(mediaSource);

        /*MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            int rendererIndex = 0;
            TrackGroupArray rendererTrackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
            SelectionOverride selectionOverride = new SelectionOverride(0, 0);
            trackSelector.setParameters(trackSelector.buildUponParameters()
                    .setSelectionOverride(rendererIndex, rendererTrackGroups, selectionOverride));
        }*/
		//Log.d(TAG, "after prepare");

		player.setPlayWhenReady(true);
		Log.d(TAG, "after play");
		player.addListener(this);

		// todo subtitles: https://github.com/google/ExoPlayer/issues/1183
	}

	@Override
	public void stop() {
		if(player != null) player.setPlayWhenReady(false);
	}

	@Override
	public void pause() {
		if(player != null) player.setPlayWhenReady(false);
	}

	@Override
	public void play() {
		if(player != null) player.setPlayWhenReady(true);
		Log.d(TAG, "play");
	}

	@Override
	public void setTVController(TVController tc) {
		mTVController = tc;
		mTVController.setVideo(this);
	}

	@Override
	public void setVideoController(VideoController vc) {
		mVideoController = vc;
		mVideoController.setVideo(this);
	}

	@Override
	public void setMap(Map<String, Object> map) {
		Log.d(TAG, "setMap");
		mMap = map;
		if(mTVController != null) {
			mTVController.setMap(mMap);
		}
		if(mVideoController != null) {
			mVideoController.setMap(mMap);
		}
		getPrefs();
	}

	@Override
	public boolean isPlaying() {
		//Log.d(TAG, "isPlaying");
		if(player == null) return false;
		//return player.getPlayWhenReady();
		return is_playing;
	}

	@Override
	public boolean showOverlay() {
		if(mVideoController != null) mVideoController.showProgress();
		return true;
	}

	@Override
	public boolean hideOverlay() {
		return false;
	}

	@Override
	public int getProgress() {
	    if(player == null) return 0;
		Log.d(TAG, "progress " + player.getCurrentPosition()); return (int)player.getCurrentPosition();
	}

	@Override
	public int getLeight() {
	    if(player == null) return 0;
		Log.d(TAG, "duration " + player.getDuration()); return (int)player.getDuration();
	}

	@Override
	public int getTime() {
	    if(player == null) return 0;
		return (int)player.getCurrentPosition();
	}

	@Override
	public void setTime(int time) {
		int pos;
		Log.d(TAG, "time	 " + time);
		if (time >= 100 || time < 0) {
			pos = (int) player.getCurrentPosition() + time;
			if (pos < 0) pos = 0;
		} else {
			pos = (int) player.getDuration() * time / 100;
		}
		if(player != null) player.seekTo(pos);
	}

	@Override
	public void setPosition(int time) {
		if(player != null) player.seekTo(time);
	}

	@Override
	public int changeSizeMode() {
		setSize();
		return 0;
	}

	@Override
	public String changeAudio() {
		displayTrackSelector((VideoActivity) getContext());
		return "Следующая дорожка";
	}

	@Override
	public String changeSubtitle() {
		return null;
	}

	@Override
	public int getAudioTracksCount() {
        int tracks = 0;
        if(trackSelector == null || player == null) return tracks;
		for(int i = 0; i < player.getCurrentTrackGroups().length; i++) {
			String format = player.getCurrentTrackGroups().get(i).getFormat(0).sampleMimeType;
			if(format.contains("audio")) {
				tracks++;
			}
		}
        /*MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if(mappedTrackInfo != null) {
			Log.d(TAG, "tracks total: " + mappedTrackInfo.getRendererCount());
            for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                if(mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_AUDIO) tracks++;
				//Log.d(TAG, "track: " + mappedTrackInfo.getRendererType(i));
            }
        }*/

        Log.d(TAG, "tracks: " + tracks);
		return tracks;
	}

	@Override
	public int getSpuTracksCount() {
		return 0;
	}

	@Override
	public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
	}

	@Override
	public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
	}

	@Override
	public void onIsPlayingChanged(boolean isPlaying) {
		is_playing = isPlaying;
	}

	@Override
	public int changeOrientation() {
		setSize();
		return 0;
	}

	@Override
	public void end() {
		Log.d(TAG, "end");
        if(player != null) {
            player.stop();
            player.release();
            player = null;
        }
		if(mTVController != null) {
			mTVController.end();
		}
		if(mVideoController != null) {
			mVideoController.end();
		}
	}

	// ExoPlayer.EventListener implementation

	@Override
	public void onLoadingChanged(boolean isLoading) {
		Log.d(TAG, "isLoading: " + isLoading);
		//Log.d(TAG, "duration " + player.getDuration());
		if(isLoading) {
			setSize();
			if(mVideoController!=null) {
				mVideoController.showProgress();
			}
		}
		// Do nothing.
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		Log.d(TAG, "playbackState: " + playbackState);
		//Log.d(TAG, "duration " + player.getDuration());
		if (playbackState == 3 && player != null) {
			if(mVideoController!=null) {
				Log.i("Debug", "Проверить число треков");
				mVideoController.checkTrack();
			}
		}
		if (playbackState == 4 && player != null) {
			//if(mTVController != null) mTVController.end();
			if(mVideoController != null) mVideoController.next();
			//end();
		}
	}

	@Override
	public void onPositionDiscontinuity(int reason) {
	}

	@Override
	public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
	}

	@Override public void onSeekProcessed() {
	}

	@Override
	public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
		// Do nothing.
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
		// Do nothing.
	}

	@Override
	public void onPlayerError(ExoPlaybackException e) {
		String errorString = null;
		if (e.type == ExoPlaybackException.TYPE_RENDERER) {
			Exception cause = e.getRendererException();
			if (cause instanceof DecoderInitializationException) {
				// Special case for decoder initialization failures.
				errorString = "Ошибка декодера";
			}
		} else if (e.type == ExoPlaybackException.TYPE_SOURCE) {
			String SourceEx = e.getSourceException().getMessage();
			if (SourceEx.contains("404")) {
				errorString = "Файл не найден";
			} else if (SourceEx.contains("Unable to connect")) {
				errorString = "Ошибка сети";
			} else {
				errorString = "Ошибка воспроизведения";
			}
		}
		if (errorString != null) {
			Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
	}
	@Override
	public void onRepeatModeChanged(int repeatMode) {
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		//Toast.makeText(getContext(), "Нажата клавиша: " + event.getKeyCode(), Toast.LENGTH_LONG).show();
        if (
				(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) ||
				(event.getAction() == KeyEvent.ACTION_DOWN && event.isLongPress() && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)
			) {
            displayTrackSelector((VideoActivity) getContext());
            return true;
        }

        if(mTVController!=null) {
			return mTVController.dispatchKeyEvent(event) || StyledPlayerView.dispatchKeyEvent(event);
		}
		if(mVideoController!=null) {
			return mVideoController.dispatchKeyEvent(event) || StyledPlayerView.dispatchKeyEvent(event);
		}
		return true;
	}
}
