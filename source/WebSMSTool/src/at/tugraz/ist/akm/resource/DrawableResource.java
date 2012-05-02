package at.tugraz.ist.akm.resource;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

public class DrawableResource {

	private Context mContext = null;

	public DrawableResource(Context c) {
		mContext = c;
	}

	/**
	 * @return raw bytes of resourceId
	 */
	public byte[] getBytes(int resourceId) {
		Drawable d = mContext.getResources().getDrawable(resourceId);
		Bitmap b = ((BitmapDrawable) d).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	/**
	 * @return base64 default encoded bytes of resourceId
	 */
	public byte[] getBase64EncodedBytes(int resourceId) {
		return Base64.encode(getBytes(resourceId), Base64.DEFAULT);
	}
}
