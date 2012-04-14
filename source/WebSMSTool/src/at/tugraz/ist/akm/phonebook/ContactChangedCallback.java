package at.tugraz.ist.akm.phonebook;

import android.content.Context;
import android.content.Intent;

public interface ContactChangedCallback {

	public void contactModifiedCallback(Context context, Intent intent);

	public void contactCreatedCallback(Context context, Intent intent);

}
