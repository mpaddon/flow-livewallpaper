package pong.flow;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/*
 * This animated wallpaper draws a rotating wireframe cube.
 */
public class FlowLiveWallpaperService extends GLWallpaperService {

	Context _context;

	private static String TAG = "lines";
	private static final boolean DEBUG = false;
	public static final String SHARED_PREFS_NAME = "flowLiveWallpaperService_settings";
	public static final FlowTheme DEFAULT_THEME = FlowTheme._themeTable.get((long)7);

	public static final int GALLERY_FLOWMAP_ID = R.raw.default_flowmap;
	


	public FlowLiveWallpaperService() {
		super();
		_context = this;
		
	}
	
	
	private static class ContextFactory implements EGLContextFactory {
		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		public EGLContext createContext(EGL10 egl, EGLDisplay display,
				EGLConfig eglConfig) {
			//Log.w(TAG, "creating OpenGL ES 2.0 context");
			checkEglError("Before eglCreateContext", egl);
			int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			EGLContext context = egl.eglCreateContext(display, eglConfig,
					EGL10.EGL_NO_CONTEXT, attrib_list);
			checkEglError("After eglCreateContext", egl);
			return context;
		}

		public void destroyContext(EGL10 egl, EGLDisplay display,
				EGLContext context) {
			egl.eglDestroyContext(display, context);
		}
	}

	private static void checkEglError(String prompt, EGL10 egl) {
		int error;
		while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			//Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
		}
	}

	private static class ConfigChooser implements EGLConfigChooser {

		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
		}

		/*
		 * This EGL config specification is used to specify 2.0 rendering. We
		 * use a minimum size of 4 bits for red/green/blue, but will perform
		 * actual matching in chooseConfig() below.
		 */
		private static int EGL_OPENGL_ES2_BIT = 4;
		private static int[] s_configAttribs2 = { EGL10.EGL_RED_SIZE, 4,
				EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

			/*
			 * Get the number of minimally matching EGL configurations
			 */
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}

			/*
			 * Allocate then read the array of minimally matching EGL configs
			 */
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs,
					num_config);

			if (DEBUG) {
				printConfigs(egl, display, configs);
			}
			/*
			 * Now return the "best" one
			 */
			return chooseConfig(egl, display, configs);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);

				// We need at least mDepthSize and mStencilSize bits
				if (d < mDepthSize || s < mStencilSize)
					continue;

				// We want an *exact* match for red/green/blue/alpha
				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);

				if (r == mRedSize && g == mGreenSize && b == mBlueSize
						&& a == mAlphaSize)
					return config;
			}
			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}

		private void printConfigs(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			int numConfigs = configs.length;
			//Log.w(TAG, String.format("%d configurations", numConfigs));
			for (int i = 0; i < numConfigs; i++) {
				//Log.w(TAG, String.format("Configuration %d:\n", i));
				printConfig(egl, display, configs[i]);
			}
		}

		private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
			int[] attributes = { EGL10.EGL_BUFFER_SIZE, EGL10.EGL_ALPHA_SIZE,
					EGL10.EGL_BLUE_SIZE,
					EGL10.EGL_GREEN_SIZE,
					EGL10.EGL_RED_SIZE,
					EGL10.EGL_DEPTH_SIZE,
					EGL10.EGL_STENCIL_SIZE,
					EGL10.EGL_CONFIG_CAVEAT,
					EGL10.EGL_CONFIG_ID,
					EGL10.EGL_LEVEL,
					EGL10.EGL_MAX_PBUFFER_HEIGHT,
					EGL10.EGL_MAX_PBUFFER_PIXELS,
					EGL10.EGL_MAX_PBUFFER_WIDTH,
					EGL10.EGL_NATIVE_RENDERABLE,
					EGL10.EGL_NATIVE_VISUAL_ID,
					EGL10.EGL_NATIVE_VISUAL_TYPE,
					0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
					EGL10.EGL_SAMPLES,
					EGL10.EGL_SAMPLE_BUFFERS,
					EGL10.EGL_SURFACE_TYPE,
					EGL10.EGL_TRANSPARENT_TYPE,
					EGL10.EGL_TRANSPARENT_RED_VALUE,
					EGL10.EGL_TRANSPARENT_GREEN_VALUE,
					EGL10.EGL_TRANSPARENT_BLUE_VALUE,
					0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
					0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
					0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
					0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
					EGL10.EGL_LUMINANCE_SIZE, EGL10.EGL_ALPHA_MASK_SIZE,
					EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RENDERABLE_TYPE,
					0x3042 // EGL10.EGL_CONFORMANT
			};
			String[] names = { "EGL_BUFFER_SIZE", "EGL_ALPHA_SIZE",
					"EGL_BLUE_SIZE", "EGL_GREEN_SIZE", "EGL_RED_SIZE",
					"EGL_DEPTH_SIZE", "EGL_STENCIL_SIZE", "EGL_CONFIG_CAVEAT",
					"EGL_CONFIG_ID", "EGL_LEVEL", "EGL_MAX_PBUFFER_HEIGHT",
					"EGL_MAX_PBUFFER_PIXELS", "EGL_MAX_PBUFFER_WIDTH",
					"EGL_NATIVE_RENDERABLE", "EGL_NATIVE_VISUAL_ID",
					"EGL_NATIVE_VISUAL_TYPE", "EGL_PRESERVED_RESOURCES",
					"EGL_SAMPLES", "EGL_SAMPLE_BUFFERS", "EGL_SURFACE_TYPE",
					"EGL_TRANSPARENT_TYPE", "EGL_TRANSPARENT_RED_VALUE",
					"EGL_TRANSPARENT_GREEN_VALUE",
					"EGL_TRANSPARENT_BLUE_VALUE", "EGL_BIND_TO_TEXTURE_RGB",
					"EGL_BIND_TO_TEXTURE_RGBA", "EGL_MIN_SWAP_INTERVAL",
					"EGL_MAX_SWAP_INTERVAL", "EGL_LUMINANCE_SIZE",
					"EGL_ALPHA_MASK_SIZE", "EGL_COLOR_BUFFER_TYPE",
					"EGL_RENDERABLE_TYPE", "EGL_CONFORMANT" };
			int[] value = new int[1];
			for (int i = 0; i < attributes.length; i++) {
				int attribute = attributes[i];
				String name = names[i];
				if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
					//Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
				} else {
					// Log.w(TAG, String.format("  %s: failed\n", name));
					while (egl.eglGetError() != EGL10.EGL_SUCCESS)
						;
				}
			}
		}

		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		private int[] mValue = new int[1];
	}

	class WallpaperEngine extends GLWallpaperService.GLEngine  implements SharedPreferences.OnSharedPreferenceChangeListener{
		float pX = 0;
		float pY = 0;
		FlowRenderer _renderer;
		
		public TextView fps;

		public WallpaperEngine(SharedPreferences preferences) {
			super();
			setEGLContextFactory(new ContextFactory());
			setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));
			_renderer = new FlowRenderer(_context, preferences);
			//_renderer.setSharedPreferences(preferences);
			// mPrefs = this.getSharedPreferences(SHARED_PREFS_NAME, 0);
			preferences.registerOnSharedPreferenceChangeListener(this);
	          //  onSharedPreferenceChanged(preferences, null);
			
		/*	if(preferences.contains("water_color_preference"))
			{
			FlowTheme ft = FlowTheme._themeTable.get((long)7);
			Editor e = preferences.edit();
			e.putInt("water_color_preference", ft.WaterColor);
			
			}

			if(preferences.contains("sun_color_preference"))
			{
				FlowTheme ft = FlowTheme._themeTable.get((long)7);
				Editor e = preferences.edit();
				e.putInt("sun_color_preference", ft.SunColor);
			}*/
			
			setRenderer(_renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
			//onSharedPreferenceChanged(preferences, "background_image_preference");
			
		}
		
		@Override
		public void onSurfaceCreated(SurfaceHolder holder)
		{
			super.onSurfaceCreated(holder);
		//	createDebugPrint(holder);
		}

		
		public void createDebugPrint(SurfaceHolder holder)
		{
			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					fps = new TextView(_context);
					fps.setVisibility(View.VISIBLE);
					fps.setText("FPS: 0");
					fps.setTextColor(Color.BLUE);
					RelativeLayout rl = new RelativeLayout(_context);
					rl.addView(fps);
					//rl.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
					//rl.measure(c.getWidth(), c.getHeight());
					//rl.layout(0, 0, c.getWidth(), c.getHeight());
					rl.draw(c);
				}
				else
				{ 
					//Log.e("Wall Paper Service","FPS draw fail");
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
		}

		/*
		 * Store the position of the touch event so we can use it for drawing
		 * later
		 */
		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			FlowInteraction.pressureStroke = (float)Math.min(FlowInteraction.brushStroke * (event.getPressure() * 10.0f), 0.15);
			//Log.e(TAG, "brush: " + FlowInteraction.pressureStroke);
			float x = event.getX();
			float y = event.getY();
			// construct direction from -1 -> 1 for x & y
			_renderer.UpdateFlowInput(x, y, pX, pY);
			pX = x;
			pY = y;
		}
		
		@Override
		public void onResume()
		{
			super.onResume();
			_renderer.onResume();
		}

		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			//Log.d("SHARED", key);
			if(_renderer.isLoaded())
			{
			if(key.equals("water_color_preference"))
			{
				//Log.d("THEME", "change water");
				int color = sharedPreferences.getInt(key, 0);
				float[] colorComp = new float[4];
				
				
				colorComp[0] = (float)Color.red(color)/(float)256;
				colorComp[1] = (float)Color.green(color)/(float)256;
				colorComp[2] = (float)Color.blue(color)/(float)256;		
				colorComp[3] = 1.0f;
				
				//Log.d("TEST water Pref", "" +  colorComp[1] + "," + colorComp[2] + "," + colorComp[3] );
				_renderer._flowSurface.setWaterColor(colorComp);
				
			}else if(key.equals("flow_speed_preference"))
			{
				float val = sharedPreferences.getFloat(key, (float) .5);
				_renderer._flowSurface.setFlowSpeed(val*FlowSurface.MAX_FLOW_SPEED);
			
			}else if(key.equals("flow_distortion_preference"))
			{
				float val = sharedPreferences.getFloat(key, (float) .5);
				_renderer._flowSurface.setFlowDistortion(val*FlowSurface.MAX_DIST_MULT);
			
			}else if(key.equals("flow_wave_preference"))
			{
				float val = sharedPreferences.getFloat(key, (float) .5);
				_renderer._flowSurface.setFlowWave(val*FlowSurface.MAX_WAVE_MULT);
			
			}else if(key.equals("input_dis_preference"))
			{
				float val = sharedPreferences.getFloat(key, (float) .25);
				if(FlowRenderer.EnableInput)
				{
				_renderer._flowInteraction.setDisRate(val*FlowInteraction.MAX_DIS_RATE);
				}
			
			}else if(key.equals("brushstroke_preference"))
			{
				float val = sharedPreferences.getFloat(key, (float) .5);
				if(FlowRenderer.EnableInput)
				{
				    _renderer._flowInteraction.setBrushSize(val*FlowInteraction.MAX_BRUSH_SIZE);
				}
			}
			else if(key.equals("sun_color_preference"))
			{
				//Log.d("THEME", "change sun");
				int color = sharedPreferences.getInt(key, 0);
				float[] colorComp = new float[4];
				
				colorComp[0] = (float)Color.red(color)/(float)256;
				colorComp[1] = (float)Color.green(color)/(float)256;
				colorComp[2] = (float)Color.blue(color)/(float)256;	
				colorComp[3] = 1.0f;
				_renderer._flowSurface.setSunColor(colorComp);
			}else if(key.equals("background_photo_preference"))
			{
				
				int imageID = (int) sharedPreferences.getLong("background_photo_preference", -1);
				Uri uri = Uri.withAppendedPath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                Integer.toString(imageID));
				if(imageID != -1)
				{
					
				   Bitmap background = loadFullImage(_context, uri );	
				   Editor e = sharedPreferences.edit();
					e.putInt("flowmap_preference", FlowLiveWallpaperService.GALLERY_FLOWMAP_ID);
					e.putLong("theme_preference", -1);
					e.commit();
					//_renderer.setFlowmapCompResource( FlowLiveWallpaperService.GALLERY_FLOWMAP_ID);
				   _renderer.setBackgroundBitmap(background);
				   
				   
				}
			}else if(key.equals("theme_preference"))
			{
               
				long id = sharedPreferences.getLong(key, 0);
				
				if(id != -1)
				{
				FlowTheme ft = FlowTheme._themeTable.get(id);
				Editor e = sharedPreferences.edit();
				e.putInt("water_color_preference", ft.WaterColor);
				e.putInt("sun_color_preference", ft.SunColor);
				e.putInt("diffuse_preference", ft.diffuseTextureID);
				e.putInt("wave1_preference", ft.wave1TextureID);
				e.putInt("wave2_preference", ft.wave2TextureID);
				e.putInt("wave2_preference", ft.wave2TextureID);
				e.putInt("flowmap_preference", ft.flowMapID);
				e.putLong("background_photo_preference", -1);		
				e.putFloat("flow_speed_preference", ft.flowSpeed);
				e.putFloat("flow_wave_preference", ft.waveIntensity);
				e.putFloat("flow_distortion_preference", ft.flowDistortion);
				e.putFloat("input_dis_preference", ft.inputDis);
				
				e.commit();
				
				_renderer.setBackgroundCompResource(ft.diffuseTextureID);
				}
			
			}
			else if(key.equals("flow_enable_input"))
			{
				boolean input = sharedPreferences.getBoolean(key, true);
				FlowRenderer.EnableInput = input;
			}
			else if(key.equals("flowmap_preference"))
			{
				int id = sharedPreferences.getInt(key, FlowLiveWallpaperService.DEFAULT_THEME.flowMapID);
				_renderer.setFlowmapCompResource(id);
			}
			else if(key.equals("flow_enable_clock"))
			{
				boolean input = sharedPreferences.getBoolean(key, true);
				FlowRenderer.DayNight = input;
			}
			}
		}
		
			
		
		@Override
		public void onVisibilityChanged (boolean visible)
		{
			
			
			if(!isPreview ())
			{
				super.onVisibilityChanged(visible);
			}else if(!visible)
			{
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				
				if(!pm.isScreenOn())
				{
					super.onVisibilityChanged(visible);
				}
			}else
			{
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				
				if(pm.isScreenOn())
				{
					super.onVisibilityChanged(visible);
				}
			}
		}
		
		@Override
	    public void onSurfaceDestroyed(SurfaceHolder holder) {
	       
	        super.onSurfaceDestroyed(holder);

	       

	    }
		

	
	}

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine(this.getSharedPreferences(SHARED_PREFS_NAME,
				0));
	}
	
	public static Bitmap loadFullImage( Context context, Uri photoUri  ) {
	    Cursor photoCursor = null;

	    try {
	        // Attempt to fetch asset filename for image
	        String[] projection = { MediaStore.Images.Media.DATA };
	        photoCursor = context.getContentResolver().query( photoUri, 
	                                                    projection, null, null, null );

	        if ( photoCursor != null && photoCursor.getCount() == 1 ) {
	            photoCursor.moveToFirst();
	            String photoFilePath = photoCursor.getString(
	                photoCursor.getColumnIndex(MediaStore.Images.Media.DATA) );
	            
	            // First decode with inJustDecodeBounds=true to check dimensions
	            final BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inJustDecodeBounds = true;
	            BitmapFactory.decodeFile(photoFilePath, options);
	            // Calculate inSampleSize
	            options.inSampleSize = calculateInSampleSize(options, (int)(options.outWidth*.5), (int)(options.outHeight*.5));

	          //  Matrix matrix = new Matrix();
	          //  matrix.postRotate(180);
	            options.inJustDecodeBounds = false;
	            Bitmap newBit;
	            Bitmap bit;
	            if(options.outWidth > options.outHeight)
	            {
	            	 bit = BitmapFactory.decodeFile(photoFilePath, options);
		             newBit =  Bitmap.createBitmap(bit, (int)((options.outWidth)/4), 0, (int)((options.outWidth)/2), (int)(options.outHeight));
	                 bit.recycle();
	            }else
	            {
	            	 newBit = BitmapFactory.decodeFile(photoFilePath, options);
		           // newBit =  Bitmap.createBitmap(bit, 0, 0, (int)(options.outWidth), (int)(options.outHeight), matrix, true);
	            }
	          
	            return newBit;

	        }
	    } finally {
	        if ( photoCursor != null ) {
	            photoCursor.close();
	        }
	    }

	    return null;
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
}

}
