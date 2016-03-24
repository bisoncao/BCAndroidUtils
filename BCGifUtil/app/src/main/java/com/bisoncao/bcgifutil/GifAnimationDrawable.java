/**
 * Copyright (C) 2013 Orthogonal Labs, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bisoncao.bcgifutil;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.bisoncao.bccommonutil.BCDebug;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Creates an AnimationDrawable from a GIF image.</p>
 *
 * @author Femi Omojola <femi@hipmob.com>
 */
public class GifAnimationDrawable extends AnimationDrawable
{
	private boolean decoded;
	
	private GifDecoder mGifDecoder;

	private Bitmap mTmpBitmap;

	private int height, width;
	
	public GifAnimationDrawable(File f) throws IOException
	{
		this(f, false);
	}
	
	public GifAnimationDrawable(InputStream is) throws IOException
	{
		this(is, false);
	}
	
	public GifAnimationDrawable(File f, boolean inline) throws IOException
	{
	    this(new BufferedInputStream(new FileInputStream(f), 32768), inline);
	}
	
	@SuppressWarnings("deprecation")
	public GifAnimationDrawable(InputStream is, boolean inline) throws IOException
	{
		super();
		InputStream bis = is;
		if(!BufferedInputStream.class.isInstance(bis)) bis = new BufferedInputStream(is, 32768);
		decoded = false;
		mGifDecoder = new GifDecoder();
		mGifDecoder.read(bis);
		mTmpBitmap = mGifDecoder.getFrame(0);
		Log.v("GifAnimationDrawable", "===>Lead frame: ["+width+"x"+height+"; "+mGifDecoder.getDelay(0)+";"+mGifDecoder.getLoopCount()+"]");
		height = mTmpBitmap.getHeight();
    	width = mTmpBitmap.getWidth();
        addFrame(new BitmapDrawable(mTmpBitmap), mGifDecoder.getDelay(0));
        setOneShot(mGifDecoder.getLoopCount() != 0);
        setVisible(true, true);
		if(inline){
			loader.run();
		}else{
			new Thread(loader).start();
		}
	}

	public boolean isDecoded(){ return decoded; }

	private Runnable loader = new Runnable(){
		@SuppressWarnings("deprecation")
		public void run()
		{
			mGifDecoder.complete();
			int i, n = mGifDecoder.getFrameCount(), t;
	        for(i=1;i<n;i++){
	            mTmpBitmap = mGifDecoder.getFrame(i);
	            t = mGifDecoder.getDelay(i);
	            Log.v("GifAnimationDrawable", "===>Frame "+i+": "+t+"]");
	            addFrame(new BitmapDrawable(mTmpBitmap), t);
	        }
	        decoded = true;
	        mGifDecoder = null;
	    }
	};
	
	public int getMinimumHeight(){ return height; }
	public int getMinimumWidth(){ return width; }
	public int getIntrinsicHeight(){ return height; }
	public int getIntrinsicWidth(){ return width; }
	
	/**
	 * @author Bison Cao
	 * recycle the bitmap allocated by GifDecoder and the member variable mTmpBitmap
	 */
	public void recycle() {

		Log.d(BCDebug.BISON, "mGifDecoder is null: " + (mGifDecoder == null));
		if (mGifDecoder != null) {
			BCDebug.d(BCDebug.BISON, "recycling mGifDecoder");
			mGifDecoder.recycle();
		}

		if (mTmpBitmap != null) {
			try {
				BCDebug.d(BCDebug.BISON, "recycling mTmpBitmap");
				mTmpBitmap.recycle();
			} catch (Exception e) {
				BCDebug
						.e(BCDebug.BISON,
								"error when recycle mTmpBitmap in GifAnimationDrawable's recycle()");
			}
		}

	}
}
