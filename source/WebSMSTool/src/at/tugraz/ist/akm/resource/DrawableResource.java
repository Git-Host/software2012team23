/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.resource;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

public class DrawableResource {

	private Context mContext = null;

	public DrawableResource(Context context) {
		mContext = context;
	}

	/**
	 * @return raw bytes of resourceId
	 */
	public byte[] getBytes(int resourceId) {
		Drawable drawable = mContext.getResources().getDrawable(resourceId);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	/**
	 * @return base64 default encoded bytes of resourceId
	 */
	public byte[] getBase64EncodedBytes(int resourceId) {
		return Base64.encode(getBytes(resourceId), Base64.DEFAULT);
	}
}
