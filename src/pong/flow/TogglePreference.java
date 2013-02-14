package pong.flow;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class TogglePreference extends Preference
{
	ImageView icon;
	boolean expanded;
	
	public TogglePreference(Context context, AttributeSet attrs)
	{
	   super(context, attrs);
	   expanded = false;
	}
	
	protected void onBindView(View view) {
		super.onBindView(view);
		icon = (ImageView) view.findViewById(R.id.icon);
		
		if(expanded)
		{
			icon.setImageResource(R.raw.expander_ic_maximized);
		}else
		{
			icon.setImageResource(R.raw.expander_ic_minimized);
		}
	}
	
	public void expand(boolean ex)
	{
		expanded = ex;
		this.persistBoolean(expanded);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		
		expanded  = this.getPersistedBoolean(false);
	}


}
