package pong.flow;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	private final String TAG = getClass().getName();

	// private static final String
	// ANDROIDNS="http://schemas.android.com/apk/res/android";
	// private static final String ROBOBUNNYNS="http://robobunny.com";
	private static final int DEFAULT_VALUE = 50;
	public static final int MAX_VALUE = 100;

	private int mMaxValue = 100;
	private int mMinValue = 0;
	private int mInterval = 1;
	private int mCurrentValue;
	private String mUnitsLeft = "";
	private String mUnitsRight = "";
	private SeekBar mSeekBar;
	boolean _touching;
	int thresh;

	private TextView mStatusText;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPreference(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPreference(context, attrs);
	}

	private void initPreference(Context context, AttributeSet attrs) {
		setValuesFromXml(attrs);
		mSeekBar = new SeekBar(context, attrs);
		mSeekBar.setMax(mMaxValue - mMinValue);
		mSeekBar.setOnSeekBarChangeListener(this);
		_touching = false;
	}

	private void setValuesFromXml(AttributeSet attrs) {
		mMaxValue = 100;// attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		mMinValue = 0;// attrs.getAttributeIntValue(ROBOBUNNYNS, "min", 0);

		mUnitsLeft = "";// getAttributeStringValue(attrs, ROBOBUNNYNS,
						// "unitsLeft", "");
		String units = "";// getAttributeStringValue(attrs, ROBOBUNNYNS,
							// "units", "");
		mUnitsRight = "%";// getAttributeStringValue(attrs, ROBOBUNNYNS,
							// "unitsRight", units);

		try {
			String newInterval = "1";// = attrs.getAttributeValue(ROBOBUNNYNS,
										// "interval");
			if (newInterval != null)
				mInterval = Integer.parseInt(newInterval);
		} catch (Exception e) {
			Log.e(TAG, "Invalid interval value", e);
		}

	}

	private String getAttributeStringValue(AttributeSet attrs,
			String namespace, String name, String defaultValue) {
		String value = attrs.getAttributeValue(namespace, name);
		if (value == null)
			value = defaultValue;

		return value;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {

		RelativeLayout layout = null;

		try {
			LayoutInflater mInflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			layout = (RelativeLayout) mInflater.inflate(
					R.layout.seek_bar_preference, parent, false);
		} catch (Exception e) {
			Log.e(TAG, "Error creating seek bar preference", e);
		}

		return layout;

	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		try {
			// move our seekbar to the new view we've been given
			ViewParent oldContainer = mSeekBar.getParent();
			ViewGroup newContainer = (ViewGroup) view
					.findViewById(R.id.seekBarPrefBarContainer);

			if (oldContainer != newContainer) {
				// remove the seekbar from the old view
				if (oldContainer != null) {
					((ViewGroup) oldContainer).removeView(mSeekBar);
				}
				// remove the existing seekbar (there may not be one) and add
				// ours
				newContainer.removeAllViews();
				newContainer.addView(mSeekBar,
						ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error binding view: " + ex.toString());
		}

		updateView(view);
	}

	/**
	 * Update a SeekBarPreference view with our current state
	 * 
	 * @param view
	 */
	protected void updateView(View view) {

		try {
			RelativeLayout layout = (RelativeLayout) view;

			mStatusText = (TextView) layout.findViewById(R.id.seekBarPrefValue);
			mStatusText.setText(String.valueOf(mCurrentValue));
			mStatusText.setMinimumWidth(30);

			mSeekBar.setProgress(mCurrentValue - mMinValue);

			TextView unitsRight = (TextView) layout
					.findViewById(R.id.seekBarPrefUnitsRight);
			unitsRight.setText(mUnitsRight);

			TextView unitsLeft = (TextView) layout
					.findViewById(R.id.seekBarPrefUnitsLeft);
			unitsLeft.setText(mUnitsLeft);

		} catch (Exception e) {
			Log.e(TAG, "Error updating seek bar preference", e);
		}

	}

	public void forceSetValue(int value) {
		mCurrentValue = value;
		// notifyChanged();
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int newValue = progress + mMinValue;

		if (newValue > mMaxValue)
			newValue = mMaxValue;
		else if (newValue < mMinValue)
			newValue = mMinValue;
		else if (mInterval != 1 && newValue % mInterval != 0)
			newValue = Math.round(((float) newValue) / mInterval) * mInterval;

		if (Math.abs(mCurrentValue - newValue) > thresh && fromUser) {
			seekBar.setProgress(mCurrentValue);
			return;
		}

		// change rejected, revert to the previous value
		if (!callChangeListener(newValue)) {
			seekBar.setProgress(mCurrentValue - mMinValue);
			return;
		}

		// change accepted, store it
		if (_touching) {
			thresh += 10;
		}

		mCurrentValue = newValue;
		mStatusText.setText(String.valueOf(newValue));
		persistFloat((float) newValue / (float) this.mMaxValue);
		// update_handler.post(r);

	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		_touching = true;
		thresh = 10;
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		_touching = false;
		thresh = 10;
		persistFloat((float) this.mCurrentValue / (float) this.mMaxValue);
		notifyChanged();
		// seekBar.setO
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {

		int defaultValue = ta.getInt(index, DEFAULT_VALUE);
		return defaultValue;

	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if (restoreValue) {
			mCurrentValue = (int) (100 * getPersistedFloat((float) .5));
		} else {
			int temp = 0;
			try {
				temp = (Integer) defaultValue;
			} catch (Exception ex) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}

			persistFloat((float) temp / (float) this.mMaxValue);
			mCurrentValue = temp;
		}

	}

}
