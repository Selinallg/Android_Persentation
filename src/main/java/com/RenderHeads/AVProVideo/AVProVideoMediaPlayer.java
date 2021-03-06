//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.RenderHeads.AVProVideo;

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.TrackInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Surface;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.json.JSONObject;

public class AVProVideoMediaPlayer extends AVProVideoPlayer implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnInfoListener, OnPreparedListener, OnVideoSizeChangedListener {
    private MediaPlayer m_MediaPlayer;
    private MediaExtractor m_MediaExtractor;
    private TrackInfo[] m_aTrackInfo = null;
    private static final String TAG = "AVProVideoMediaPlayer";

    public AVProVideoMediaPlayer(int playerIndex, boolean watermarked, Random random) {
        super(playerIndex, watermarked, random);
    }

    protected boolean InitialisePlayer(boolean enableAudio360, int audio360Channels, boolean preferSoftware) {
        Log.d(TAG, "InitialisePlayer: ");
        this.m_MediaPlayer = new MediaPlayer();
        this.m_aTrackInfo = null;
        return true;
    }

    protected void CloseVideoOnPlayer() {
        if (this.m_VideoState >= 3) {
            this._pause();
            this._stop();
        }

        if (VERSION.SDK_INT > 15 && this.m_MediaExtractor != null) {
            this.m_MediaExtractor.release();
            this.m_MediaExtractor = null;
        }

        this.m_aTrackInfo = null;
        if (this.m_MediaPlayer != null) {
            this.m_MediaPlayer.reset();
            this.m_VideoState = 0;
        }

    }

    protected void DeinitializeVideoPlayer() {
        if (this.m_MediaPlayer != null) {
            this.m_MediaPlayer.setSurface((Surface)null);
            this.m_MediaPlayer.stop();
            this.m_MediaPlayer.reset();
            this.m_MediaPlayer.release();
            this.m_MediaPlayer = null;
        }

    }

    public boolean IsPlaying() {
        return this.m_VideoState == 5;
    }

    public boolean IsPaused() {
        return this.m_VideoState == 7;
    }

    public boolean IsSeeking() {
        return this.m_VideoState == 2 || this.m_VideoState == 4;
    }

    public boolean IsFinished() {
        return this.m_VideoState == 8;
    }

    public boolean CanPlay() {
        return this.m_VideoState == 6 || this.m_VideoState == 7 || this.m_VideoState == 5 || this.m_VideoState == 8;
    }

    private static Map<String, String> GetJsonAsMap(String json) {
        HashMap result = new HashMap();

        try {
            JSONObject jsonObj;
            Iterator keyIt = (jsonObj = new JSONObject(json)).keys();

            while(keyIt.hasNext()) {
                String key = (String)keyIt.next();
                String val = jsonObj.getString(key);
                result.put(key, val);
            }

            return result;
        } catch (Exception var6) {
            throw new RuntimeException("Couldn't parse json:" + json, var6);
        }
    }

    public void SetHeadRotation(float x, float y, float z, float w) {
    }

    public void SetFocusRotation(float x, float y, float z, float w) {
    }

    public void SetFocusProps(float offFocusLevel, float widthDegrees) {
    }

    public void SetPositionTrackingEnabled(boolean enabled) {
    }

    public void SetFocusEnabled(boolean enabled) {
    }

    protected boolean OpenVideoFromFileInternal(String filePath, long fileOffset, String httpHeaderJson, int forcedFileFormat) {
        boolean bReturn = false;
        this.m_aTrackInfo = null;
        if (this.m_MediaPlayer != null) {
            boolean bFileGood = true;

            try {
                String lowerFilePath;
                if (!(lowerFilePath = filePath.toLowerCase()).startsWith("http://") && !lowerFilePath.startsWith("https://") && !lowerFilePath.startsWith("rtmp://") && !lowerFilePath.startsWith("rtsp://")) {
                    String strippedFilePath = filePath;
                    if (filePath.startsWith("file:/")) {
                        strippedFilePath = filePath.substring(6);
                    }

                    String fileName;
                    try {
                        String lookFor = ".obb!/";
                        int iIndexIntoString;
                        if ((iIndexIntoString = strippedFilePath.lastIndexOf(lookFor)) < 0) {
                            throw new IOException("Not an obb file");
                        }

                        fileName = strippedFilePath.substring(11, iIndexIntoString + lookFor.length() - 2);
                        String zipFileName = strippedFilePath.substring(iIndexIntoString + lookFor.length());
                        this.setMediaPlayerDataSourceFromZip(fileName, zipFileName, fileOffset);
                    } catch (IOException var46) {
                        try {
                            String searchString = "/assets/";
                            fileName = strippedFilePath.substring(strippedFilePath.lastIndexOf(searchString) + searchString.length());
                            AssetFileDescriptor assetFileDesc;
                            if ((assetFileDesc = this.m_Context.getAssets().openFd(fileName)) != null) {
                                this.m_MediaPlayer.setDataSource(assetFileDesc.getFileDescriptor(), assetFileDesc.getStartOffset() + fileOffset, assetFileDesc.getLength() - fileOffset);
                                if (VERSION.SDK_INT > 15) {
                                    this.m_MediaExtractor = new MediaExtractor();

                                    try {
                                        this.m_MediaExtractor.setDataSource(assetFileDesc.getFileDescriptor(), assetFileDesc.getStartOffset() + fileOffset, assetFileDesc.getLength() - fileOffset);
                                    } catch (IOException var41) {
                                        this.m_MediaExtractor.release();
                                        this.m_MediaExtractor = null;
                                    }
                                }
                            }
                        } catch (IOException var45) {
                            try {
                                FileInputStream inputStream;
                                FileDescriptor fileDescriptor;
                                if (fileOffset == 0L) {
                                    inputStream = null;
                                    boolean var37 = false;

                                    try {
                                        var37 = true;
                                        fileDescriptor = (inputStream = new FileInputStream(strippedFilePath)).getFD();
                                        this.m_MediaPlayer.setDataSource(fileDescriptor);
                                        if (VERSION.SDK_INT > 15) {
                                            this.m_MediaExtractor = new MediaExtractor();

                                            try {
                                                this.m_MediaExtractor.setDataSource(fileDescriptor);
                                                var37 = false;
                                            } catch (IOException var40) {
                                                this.m_MediaExtractor.release();
                                                this.m_MediaExtractor = null;
                                                var37 = false;
                                            }
                                        } else {
                                            var37 = false;
                                        }
                                    } finally {
                                        if (var37) {
                                            if (inputStream != null) {
                                                inputStream.close();
                                            }

                                        }
                                    }

                                    inputStream.close();
                                } else {
                                    inputStream = null;
                                    boolean var26 = false;

                                    try {
                                        var26 = true;
                                        fileDescriptor = (inputStream = new FileInputStream(strippedFilePath)).getFD();
                                        this.m_MediaPlayer.setDataSource(fileDescriptor, fileOffset, inputStream.getChannel().size() - fileOffset);
                                        if (VERSION.SDK_INT > 15) {
                                            this.m_MediaExtractor = new MediaExtractor();

                                            try {
                                                this.m_MediaExtractor.setDataSource(fileDescriptor, fileOffset, inputStream.getChannel().size() - fileOffset);
                                                var26 = false;
                                            } catch (IOException var39) {
                                                this.m_MediaExtractor.release();
                                                this.m_MediaExtractor = null;
                                                var26 = false;
                                            }
                                        } else {
                                            var26 = false;
                                        }
                                    } finally {
                                        if (var26) {
                                            if (inputStream != null) {
                                                inputStream.close();
                                            }

                                        }
                                    }

                                    inputStream.close();
                                }
                            } catch (IOException var44) {
                                Uri uri = Uri.parse("file:/" + strippedFilePath);
                                this.m_MediaPlayer.setDataSource(this.m_Context, uri);
                                if (VERSION.SDK_INT > 15) {
                                    this.m_MediaExtractor = new MediaExtractor();

                                    try {
                                        this.m_MediaExtractor.setDataSource(this.m_Context, uri, (Map)null);
                                    } catch (IOException var38) {
                                        this.m_MediaExtractor.release();
                                        this.m_MediaExtractor = null;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (httpHeaderJson != null && !httpHeaderJson.isEmpty()) {
                        Map<String, String> httpHeaderMap = GetJsonAsMap(httpHeaderJson);
                        this.m_MediaPlayer.setDataSource(this.m_Context, Uri.parse(filePath), httpHeaderMap);
                        Log.d(TAG, "OpenVideoFromFileInternal: setDataSource  "+filePath);
                    } else {
                        this.m_MediaPlayer.setDataSource(filePath);
                        Log.d(TAG, "OpenVideoFromFileInternal: setDataSource2   "+filePath);
                    }

                    this.m_bIsStream = true;
                }
            } catch (IOException var47) {
                (new StringBuilder("Failed to open video file: ")).append(var47);
                bFileGood = false;
            }

            if (bFileGood) {
                this.m_MediaPlayer.setOnPreparedListener(this);
                this.m_MediaPlayer.setOnVideoSizeChangedListener(this);
                this.m_MediaPlayer.setOnErrorListener(this);
                this.m_MediaPlayer.setOnCompletionListener(this);
                this.m_MediaPlayer.setOnBufferingUpdateListener(this);
                this.m_MediaPlayer.setOnInfoListener(this);
                this.m_MediaPlayer.setLooping(this.m_bLooping);
                this.m_VideoState = 2;
                this.m_bIsBuffering = true;
                this.m_MediaPlayer.prepareAsync();
            } else {
                this.m_iLastError = 100;
            }

            bReturn = bFileGood;
        }

        return bReturn;
    }

    public void SetLooping(boolean bLooping) {
        this.m_bLooping = bLooping;
        if (this.m_MediaPlayer != null && this.m_VideoState >= 3) {
            this.UpdateLooping();
        } else {
            this.AddVideoCommandInt(VideoCommand_SetLooping, 0);
        }
    }

    public long GetCurrentTimeMs() {
        long result = 0L;
        if (this.m_MediaPlayer != null && this.m_VideoState >= 3 && this.m_VideoState <= 8 && (result = (long)this.m_MediaPlayer.getCurrentPosition()) > this.m_DurationMs && this.m_DurationMs > 0L) {
            result = this.m_DurationMs;
        }

        return result;
    }

    public void SetPlaybackRate(float fRate) {
        if (VERSION.SDK_INT > 22 && this.m_MediaPlayer != null && this.m_VideoState >= 3) {
            if (fRate < 0.01F) {
                this.m_bPlaybackRateSetPaused = this.IsPaused();
                if (!this.m_bPlaybackRateSetPaused) {
                    this.Pause();
                }

                this.m_fPlaybackRate = 0.0F;
                return;
            }

            if (this.m_bPlaybackRateSetPaused) {
                this.Play();
                this.m_bPlaybackRateSetPaused = false;
            }

            PlaybackParams playbackParams;
            (playbackParams = new PlaybackParams()).setSpeed(fRate);
            this.m_MediaPlayer.setPlaybackParams(playbackParams);
            this.m_fPlaybackRate = fRate;
        }

    }

    public void SetAudioTrack(int iTrackIndex) {
        if (VERSION.SDK_INT > 15 && this.m_MediaPlayer != null && iTrackIndex < this.m_iNumberAudioTracks && iTrackIndex != this.m_iCurrentAudioTrackIndex) {
            int iAudioTrack = 0;
            int iTrack = 0;
            TrackInfo[] var4;
            int var5 = (var4 = this.m_aTrackInfo).length;

            for(int var6 = 0; var6 < var5; ++var6) {
                TrackInfo info;
                if ((info = var4[var6]) != null && info.getTrackType() == 2) {
                    if (iAudioTrack == iTrackIndex) {
                        this.m_MediaPlayer.selectTrack(iTrack);
                        this.m_iCurrentAudioTrackIndex = iTrackIndex;
                        return;
                    }

                    ++iAudioTrack;
                }

                ++iTrack;
            }
        }

    }

    private void ResetPlaybackFrameRate() {
        this.m_DisplayRate_FrameRate = 0.0F;
        this.m_DisplayRate_NumberFrames = 0L;
        this.m_DisplayRate_LastSystemTimeMS = System.nanoTime();
    }

    protected void _play() {
        if (this.m_MediaPlayer != null) {
            this.m_MediaPlayer.start();
        }

        this.ResetPlaybackFrameRate();
        this.m_VideoState = 5;
    }

    protected void _pause() {
        if (this.m_VideoState > 4 && this.m_VideoState != 6 && this.m_VideoState != 8) {
            if (this.m_MediaPlayer != null) {
                this.m_MediaPlayer.pause();
            }

            this.ResetPlaybackFrameRate();
            this.m_VideoState = 7;
        }

    }

    protected void _stop() {
        if (this.m_VideoState > 4 && this.m_VideoState < 6) {
            if (this.m_MediaPlayer != null) {
                this.m_MediaPlayer.stop();
            }

            this.ResetPlaybackFrameRate();
            this.m_VideoState = 6;
        }

    }

    @TargetApi(26)
    protected void _seek(int timeMs) {
        if (this.m_MediaPlayer != null) {
            if (VERSION.SDK_INT >= 26) {
                this.m_MediaPlayer.seekTo((long)timeMs, 3);
                return;
            }

            this.m_MediaPlayer.seekTo(timeMs);
        }

    }

    @TargetApi(26)
    protected void _seekFast(int timeMs) {
        if (this.m_MediaPlayer != null) {
            if (VERSION.SDK_INT >= 26) {
                this.m_MediaPlayer.seekTo((long)timeMs, 0);
                return;
            }

            this.m_MediaPlayer.seekTo(timeMs);
        }

    }

    protected void UpdateAudioVolumes() {
        float leftVolume = 0.0F;
        float rightVolume = 0.0F;
        if (!this.m_AudioMuted) {
            float leftPan = Math.max(Math.min(Math.abs(this.m_AudioPan - 1.0F), 1.0F), 0.0F);
            float rightPan = Math.max(Math.min(this.m_AudioPan + 1.0F, 1.0F), 0.0F);
            leftVolume = this.m_AudioVolume * leftPan;
            rightVolume = this.m_AudioVolume * rightPan;
            if (leftVolume > 1.0F) {
                leftVolume = 1.0F;
            }

            if (rightVolume > 1.0F) {
                rightVolume = 1.0F;
            }
        }

        if (this.m_MediaPlayer != null) {
            this.m_MediaPlayer.setVolume(leftVolume, rightVolume);
        }

    }

    protected void UpdateLooping() {
        if (this.m_MediaPlayer != null) {
            this.m_MediaPlayer.setLooping(this.m_bLooping);
        }

    }

    protected void BindSurfaceToPlayer() {
        if (this.m_MediaPlayer != null) {
            Surface surface = new Surface(this.m_SurfaceTexture);
            this.m_MediaPlayer.setSurface(surface);
            surface.release();
        }

    }

    private static ZipEntryRO zipFindFile(ZipResourceFile zip, String fileNameInZip) {
        ZipEntryRO[] var2;
        int var3 = (var2 = zip.getAllEntries()).length;

        for(int var4 = 0; var4 < var3; ++var4) {
            ZipEntryRO entry;
            if ((entry = var2[var4]).mFileName.equals(fileNameInZip)) {
                return entry;
            }
        }

        throw new RuntimeException(String.format("File \"%s\"not found in zip", fileNameInZip));
    }

    private void setMediaPlayerDataSourceFromZip(String zipFileName, String fileNameInZip, long fileOffset) throws IOException {
        if (this.m_MediaPlayer != null) {
            FileInputStream fis = null;
            boolean var12 = false;

            try {
                var12 = true;
                ZipResourceFile zip = new ZipResourceFile(zipFileName);
                FileDescriptor zipfd = (fis = new FileInputStream(zipFileName)).getFD();
                ZipEntryRO entry = zipFindFile(zip, fileNameInZip);
                this.m_MediaPlayer.setDataSource(zipfd, entry.mOffset + fileOffset, entry.mUncompressedLength - fileOffset);
                if (VERSION.SDK_INT > 15) {
                    if (this.m_MediaExtractor != null) {
                        try {
                            this.m_MediaExtractor.setDataSource(zipfd, entry.mOffset + fileOffset, entry.mUncompressedLength - fileOffset);
                            var12 = false;
                        } catch (IOException var13) {
                            this.m_MediaExtractor.release();
                            this.m_MediaExtractor = null;
                            var12 = false;
                        }
                    } else {
                        var12 = false;
                    }
                } else {
                    var12 = false;
                }
            } finally {
                if (var12) {
                    if (fis != null) {
                        fis.close();
                    }

                }
            }

            fis.close();
        }
    }

    public void onRenderersError(Exception e) {
        (new StringBuilder("ERROR - onRenderersError: ")).append(e);
    }

    private void UpdateGetDuration() {
        if (this.m_MediaPlayer != null) {
            this.m_DurationMs = (long)this.m_MediaPlayer.getDuration();
        }

        (new StringBuilder("Video duration is: ")).append(this.m_DurationMs).append("ms");
    }

    public double GetCurrentAbsoluteTimestamp() {
        return 0.0D;
    }

    public float[] GetSeekableTimeRange() {
        float[] result;
        (result = new float[2])[0] = 0.0F;
        result[1] = 0.0F;
        return result;
    }

    @TargetApi(19)
    public void onPrepared(MediaPlayer mp) {
        this.m_VideoState = 3;
        this.m_bIsBuffering = false;
        this.UpdateGetDuration();
        if (this.m_bIsStream) {
            this.m_iNumberAudioTracks = 1;
            this.m_bSourceHasAudio = true;
        }

        if (this.m_MediaPlayer != null && VERSION.SDK_INT > 15) {
            try {
                this.m_aTrackInfo = this.m_MediaPlayer.getTrackInfo();
                if (this.m_aTrackInfo != null) {
                    (new StringBuilder("Source has ")).append(this.m_aTrackInfo.length).append(" tracks");
                    if (this.m_aTrackInfo.length > 0) {
                        this.m_iNumberAudioTracks = 0;
                        int iTrack = 0;
                        TrackInfo[] var3;
                        int var4 = (var3 = this.m_aTrackInfo).length;

                        for(int var5 = 0; var5 < var4; ++var5) {
                            TrackInfo info;
                            if ((info = var3[var5]) != null) {
                                switch(info.getTrackType()) {
                                    case 1:
                                        this.m_bSourceHasVideo = true;
                                        MediaFormat mediaFormat;
                                        if (this.m_fSourceVideoFrameRate == 0.0F && VERSION.SDK_INT >= 19 && (mediaFormat = info.getFormat()) != null) {
                                            this.m_fSourceVideoFrameRate = (float)mediaFormat.getInteger("frame-rate");
                                        }

                                        if (VERSION.SDK_INT > 15 && this.m_MediaExtractor != null) {
                                            if (this.m_fSourceVideoFrameRate == 0.0F) {
                                                mediaFormat = this.m_MediaExtractor.getTrackFormat(iTrack);
                                                this.m_fSourceVideoFrameRate = (float)mediaFormat.getInteger("frame-rate");
                                                (new StringBuilder("Source video frame rate: ")).append(this.m_fSourceVideoFrameRate);
                                            }

                                            this.m_MediaExtractor.release();
                                            this.m_MediaExtractor = null;
                                        }
                                        break;
                                    case 2:
                                        ++this.m_iNumberAudioTracks;
                                        break;
                                    case 3:
                                        this.m_bSourceHasTimedText = true;
                                        break;
                                    case 4:
                                        this.m_bSourceHasSubtitles = true;
                                }
                            }

                            ++iTrack;
                        }

                        if (this.m_iNumberAudioTracks > 0) {
                            this.SetAudioTrack(0);
                            this.m_bSourceHasAudio = true;
                        }

                        (new StringBuilder("Number of audio tracks in source: ")).append(this.m_iNumberAudioTracks);
                    }
                }
            } catch (Exception var8) {
            }
        }

        if (this.m_bIsStream || this.m_iNumberAudioTracks > 0 || this.m_bVideo_RenderSurfaceCreated.get() && !this.m_bVideo_DestroyRenderSurface.get() && !this.m_bVideo_CreateRenderSurface.get()) {
            this.m_bVideo_AcceptCommands.set(true);
            if (this.m_VideoState != 5 && this.m_VideoState != 4) {
                this.m_VideoState = 6;
            }
        }

        if ((!this.m_bIsStream || this.m_Width > 0) && this.m_bShowPosterFrame) {
            this._seek(0);
        }

    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (this.m_Width != width || this.m_Height != height) {
            (new StringBuilder("onVideoSizeChanged : New size: ")).append(width).append(" x ").append(height);
            this.m_Width = width;
            this.m_Height = height;
            this.m_bSourceHasVideo = true;
            this.m_bVideo_CreateRenderSurface.set(true);
            this.m_bVideo_DestroyRenderSurface.set(true);
        }

    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        (new StringBuilder("onError what(")).append(what).append("), extra(").append(extra).append(")");
        boolean result = false;
        switch(this.m_VideoState) {
            case 0:
            case 3:
            default:
                break;
            case 1:
            case 2:
            case 4:
                this.m_iLastError = 100;
                result = true;
                break;
            case 5:
                this.m_iLastError = 200;
                result = true;
        }

        return result;
    }

    public void onCompletion(MediaPlayer mp) {
        if (!this.m_bLooping && this.m_VideoState >= 3 && this.m_VideoState < 8) {
            this.m_VideoState = 8;
        }

    }

    public float GetBufferingProgressPercent() {
        return this.m_fBufferingProgressPercent;
    }

    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        this.m_fBufferingProgressPercent = (float)percent;
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch(what) {
            case 701:
                this.m_bIsBuffering = true;
                break;
            case 702:
                this.m_bIsBuffering = false;
        }

        return false;
    }

    protected void UpdateVideoMetadata() {
    }
}
