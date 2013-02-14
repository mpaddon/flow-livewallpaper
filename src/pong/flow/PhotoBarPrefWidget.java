package pong.flow;

import android.content.Context;

import android.util.AttributeSet;

import android.view.LayoutInflater;

import android.widget.RelativeLayout;

public class PhotoBarPrefWidget extends RelativeLayout {

	final String _photoName = "";
	Context _context = null;

	public final static long NEXT_PAGE = -123;

	public PhotoBarPrefWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// attrs.getAttributeBooleanValue(namespace, attribute, defaultValue)
		// final View view =
		// LayoutInflater.from(context).inflate(R.layout.photo_bar_preference,
		// null);
		_context = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.photo_bar_preference, this);

	}

};
