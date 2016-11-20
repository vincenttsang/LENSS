/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vec.demo.mediacodec;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DecodeActivity extends Activity implements SurfaceHolder.Callback {
	private static final String SAMPLE = Environment.getExternalStorageDirectory() + "/videoplayback.mp4";
	private PlayerThread mPlayer = null;
	private static final boolean VERBOSE = false;
	private static final String TAG = "DecodeActivity";
	private static final int MAX_FRAMES = 10;
	private static final File FILES_DIR = Environment.getExternalStorageDirectory();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SurfaceView sv = new SurfaceView(this);
		sv.getHolder().addCallback(this);
		setContentView(sv);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (mPlayer == null) {
			//original
			//mPlayer = new PlayerThread(holder.getSurface());
			mPlayer = new PlayerThread(width, height, holder.getSurface());
			mPlayer.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mPlayer != null) {
			mPlayer.interrupt();
		}
	}

	private class PlayerThread extends Thread {
		private MediaExtractor extractor;
		private MediaCodec decoder;
		private Surface surface;
		private ExtractMpegFramesTest.CodecOutputSurface outputSurface;

		public PlayerThread(Surface surface){
			this.surface = surface;
		}
		//Added by Yukun Zeng
		public PlayerThread(int width, int height, Surface surface) {
			this.outputSurface = new ExtractMpegFramesTest.CodecOutputSurface(width, height, surface);
		}

		@Override
		public void run() {
			extractor = new MediaExtractor();
			try {
				extractor.setDataSource(SAMPLE);
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					extractor.selectTrack(i);
					try {
						decoder = MediaCodec.createDecoderByType(mime);
					} catch (IOException e) {
						e.printStackTrace();
					}
					decoder.configure(format, surface, null, 0);
					//Added by Yukun Zeng
					//decoder.configure(format, outputSurface.getSurface(), null, 0);
					break;
				}
			}

			if (decoder == null) {
				Log.e("DecodeActivity", "Can't find video info!");
				return;
			}

			decoder.start();

			ByteBuffer[] inputBuffers = decoder.getInputBuffers();
			ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
			BufferInfo info = new BufferInfo();
			boolean isEOS = false;
			long startMs = System.currentTimeMillis();
			int decodeCount = 0;
			long frameSaveTime = 0;

			while (!Thread.interrupted()) {
				if (!isEOS) {
					int inIndex = decoder.dequeueInputBuffer(10000);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = extractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							// We shouldn't stop the playback at this point, just pass the EOS
							// flag to decoder, we will get it again from the
							// dequeueOutputBuffer
							Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
							decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
							extractor.advance();
						}
					}
				}

				int decoderStatus = decoder.dequeueOutputBuffer(info, 10000);
				switch (decoderStatus) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = decoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					Log.d("DecodeActivity", "New format " + decoder.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
					break;
				default:
					ByteBuffer buffer = outputBuffers[decoderStatus];
					Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buffer);

					// We use a very simple clock to keep the video FPS, or the video
					// playback will be too fast
					while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					//decoder.releaseOutputBuffer(decoderStatus, true);

					//added by Yukun Zeng for frame extraction test
					boolean doRender = (info.size != 0);

					// As soon as we call releaseOutputBuffer, the buffer will be forwarded
					// to SurfaceTexture to convert to a texture.  The API doesn't guarantee
					// that the texture will be available before the call returns, so we
					// need to wait for the onFrameAvailable callback to fire.
					decoder.releaseOutputBuffer(decoderStatus, doRender);
					if (doRender) {
						if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);
						outputSurface.awaitNewImage();
						outputSurface.drawImage(true);

						if (decodeCount < MAX_FRAMES) {
							File outputFile = new File(FILES_DIR,
									String.format("frame-%02d.png", decodeCount));
							long startWhen = System.nanoTime();
							try {
								outputSurface.saveFrame(outputFile.toString());
							} catch (IOException e) {
								e.printStackTrace();
							}
							frameSaveTime += System.nanoTime() - startWhen;
						}
						decodeCount++;
					}
					break;
				}

				// All decoded frames have been rendered, we can stop playing now
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
					break;
				}
			}
			int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
			Log.d(TAG, "Saving " + numSaved + " frames took " +
					(frameSaveTime / numSaved / 1000) + " us per frame");

			decoder.stop();
			decoder.release();
			extractor.release();
		}
	}
}