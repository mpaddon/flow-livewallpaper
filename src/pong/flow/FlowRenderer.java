package pong.flow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class FlowRenderer implements GLWallpaperService.Renderer {
	public static boolean DEBUG = true;
	public static String TAG = "FlowRenderer";
	
	public static boolean Portrait = true;

	public enum RenderMode {
		Final, Flow
	}

	public static RenderMode renderMode = RenderMode.Final;

	// Rendering Options
	public static boolean EnableInput = true;
	public static boolean DayNight = false;
	// screen resolution
	public int WIDTH;
	public int HEIGHT;
	private Context _context;
	boolean _changeBackgroundBitmap;
	boolean _loaded;

	// matrices
	private float[] _mMVPMatrix = new float[16];
	private float[] _mProjMatrix = new float[16]; // projection
	private float[] _mMMatrix = new float[16]; // rotation
	private float[] _mVMatrix = new float[16]; // modelview
	private float[] _normalMatrix = new float[16]; // modelview normal
	private Shader _coreShader;

	// light parameters
	private float minuteCounter = 100.0f;
	double angle = 0.0f;
	private float[] _lightPos;

	private long _lastTime = 0;
	private long _pausedTime = 0;
	private float _deltaTime = 0;
	private float _frameTime = 0;
	private int _frameCounter = 0;

	// eye pos
	private float[] _eyePos = { 0.0f, 1.0f, -2.0f };
	protected Camera camera = new Camera(_mVMatrix);
	 FlowSurface _flowSurface;
	FlowInteraction _flowInteraction;
	SharedPreferences _pref;

	private Bitmap _background;

	private boolean _changeBackgroundCompRes;

	private int _bacgroundCompRes;
	private boolean _changeFlowmap;
	private int _flowmapCompRes;

	public Bitmap get_background()
	{
		return _background;
	}


	public FlowRenderer(Context context, SharedPreferences pref) {
		_context = context;
		_pref = pref;
		_changeBackgroundBitmap = false;
		_changeBackgroundCompRes = false;
		_changeFlowmap = false;
		_loaded = false;
		minuteCounter = 100.0f;
		
		
		//enable or disable input
		boolean input = _pref.getBoolean("flow_enable_input", true);
		FlowRenderer.EnableInput = input;
		
		input = _pref.getBoolean("flow_enable_clock", true);
		FlowRenderer.DayNight= input;
		
		if(!FlowRenderer.DayNight)
		{
			float[] t_lightPos = { 1.0f, 0, -2.0f, 1.0f };
			_lightPos = t_lightPos;
		}
	}
	
	public void setBackgroundBitmap(Bitmap bitmap)
	{
		_background = bitmap;
		_changeBackgroundBitmap = true;
		_changeBackgroundCompRes = false;
	}
	
	public void setBackgroundCompResource(int id)
	{
		_bacgroundCompRes = id;
		_changeBackgroundCompRes =true;
		_changeBackgroundBitmap = false;
	}
	
	
	public void setFlowmapCompResource(int id)
	{
		_flowmapCompRes = id;
		_changeFlowmap = true;
	}
	

	public void onDrawFrame(GL10 arg0) {
		Update();
		if(_changeBackgroundBitmap)
		{
			_flowSurface.setBackground(_background);
			_changeBackgroundBitmap =false;
			_changeBackgroundBitmap = false;
			_changeBackgroundCompRes = false;
		}
		
		if(_changeBackgroundCompRes)
		{
			_flowSurface.setBackgroundCompRes(_bacgroundCompRes);
			_changeBackgroundCompRes = false;
		}
		
		if(_changeFlowmap)
		{
			_flowSurface.setflowmap(_flowmapCompRes);
			_changeFlowmap = false;
		}
		
		if (EnableInput) {
			if(this._flowInteraction == null)
			{
				_flowInteraction = new FlowInteraction(_context, WIDTH, HEIGHT, _pref, _flowSurface);
				_flowInteraction.initInteraction();
			}
			_flowInteraction.DrawInput(_mMVPMatrix);
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		}
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
		DrawSurface();
	}

	public void onResume() {
		//if (_flowSurface != null)
			//_flowSurface.onResume();
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
		_pausedTime = SystemClock.uptimeMillis();
		minuteCounter = 100.0f;
	}
	
	public void updateLight()
	{
		if(!DayNight || minuteCounter < 30.0f) return;
		
		//angle += 100.0f * _deltaTime;
		//if(angle >= 360) angle = 0.0f;
		
		float timeAngle = ((Calendar.getInstance().get(Calendar.HOUR) * 60) + Calendar.getInstance().get(Calendar.MINUTE)) / 2.0f;
		//Log.i(TAG, "Time Angle: " + timeAngle);
		
		//float timeAngle = 6 * Calendar.getInstance().get(Calendar.SECOND);
		//timeAngle = (float) angle;
		timeAngle += 90;
		float x = (float) Math.cos(Math.toRadians(timeAngle));
		float y = (float) Math.sin(Math.toRadians(timeAngle));
		
		float[] t_lightPos = { -x * 6.0f, y * 3.0f, -5.0f, 1.0f };
		_lightPos = t_lightPos;
		minuteCounter = 0.0f;
	}

	public void Update() {
		if (_lastTime == 0)
			_lastTime = SystemClock.uptimeMillis();
		long curTime = SystemClock.uptimeMillis();
		if(_pausedTime > 0.0f)
		{
			//Log.i("Pausedtime", "pauseTime: " + (float)_pausedTime / 1000.0f + " curTime: " + (float)curTime / 1000.0f + " difference: " + (float)(curTime - _pausedTime) / 1000.0f);
			//curTime -= _pausedTime;
			_lastTime = curTime;
			_pausedTime = 0;
			
		}
		_deltaTime = (curTime - _lastTime) / 1000.0f;
		_lastTime = curTime;
		_frameTime += _deltaTime;
		minuteCounter += _deltaTime;
		_frameCounter++;
		if(_frameTime >= 1.0f)
		{
			_frameTime = 0.0f;
			if(DEBUG)
			Log.d("FlowRenderer", "FPS: " + _frameCounter);
			_frameCounter = 0;
		}
		
		_flowSurface.Update(_deltaTime);
		if(EnableInput)
		{
			if(this._flowInteraction == null)
			{
				_flowInteraction = new FlowInteraction(_context, WIDTH, HEIGHT, _pref, _flowSurface);
				_flowInteraction.initInteraction();
			}
			_flowInteraction.Update(_deltaTime);
		}

		updateLight();
	}

	public void UpdateFlowRotation(float[] rotation) {
		if (_flowInteraction != null)
			_flowInteraction.UpdateRotation(rotation);
	}

	public void UpdateFlowInput(float x, float y, float pX, float pY) {
		float U = x / WIDTH;
		float p_U = pX / WIDTH;
		float V = 1.0f - (y / HEIGHT);
		float p_V = 1.0f - (pY / HEIGHT);
		float xDir = U - p_U;
		float yDir = V - p_V;
		// Log.i("Input Coords", "U: " + U + " V: " + V);
		float mag = (float) Math.sqrt((xDir * xDir) + (yDir * yDir));
		// Log.i("Input Direction", "xDir: " + xDir / mag + " yDir: " + yDir /
		// mag
		// + " Mag: " + mag);
		if (_flowInteraction != null)
			_flowInteraction.UpdateInput(U, V, xDir / mag, yDir / mag, mag);
	}

	public void DrawSurface() {
		int program = _coreShader.getProgram();
		GLES20.glUseProgram(program);
		FlowRenderer.checkGlError("glUseProgram");
		int mvpLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mvpLoc, 1, false, _mMVPMatrix, 0);
		int normLoc = GLES20.glGetUniformLocation(program, "normalMatrix");
		GLES20.glUniformMatrix4fv(normLoc, 1, false, _normalMatrix, 0);
		GLES20.glUniform4fv(GLES20.glGetUniformLocation(program, "lightPos"),
				1, _lightPos, 0);
		GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, "eyePos"), 1,
				_eyePos, 0);
		_flowSurface.onDrawFrame(program);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		boolean prevOritation = Portrait;
		if(width > height)
		{
			Portrait = false;
		}
		else
		{
			Portrait = true;
		}
		WIDTH = width;
		HEIGHT = height;
		GLES20.glViewport(0, 0, WIDTH, HEIGHT);
		 setPerspective();
		//setOrtho();
		_eyePos[0] = camera.Eye.x;
		_eyePos[1] = camera.Eye.y;
		_eyePos[2] = camera.Eye.z;
		if(!_flowSurface.Initialized || prevOritation != Portrait)
			_flowSurface.setDimensions(WIDTH, HEIGHT);
		if (EnableInput && (_flowInteraction == null || prevOritation != Portrait)) {
			_flowInteraction = new FlowInteraction(_context, WIDTH, HEIGHT, _pref, _flowSurface);
			_flowInteraction.initInteraction();
		}
		_loaded =true;
	}
	
	public boolean isLoaded()
	{
	  return _loaded;
	}

	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		GLES20.glClearDepthf(1.0f);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glDepthMask(false);
		//GLES20.glDisable(GLES20.GL_DITHER);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);


		if(_coreShader == null)
			_coreShader = new Shader(_context, R.raw.flow_vs, R.raw.flow_ps);
		camera.setLookAtM();
		// float[] t_lightPos = {10.0f,-20.5f,-10.05f,1.0f};
		float[] t_lightPos = { 1.0f, 0, -2.0f, 1.0f };
		_lightPos = t_lightPos;
		if(_flowSurface == null)
			_flowSurface = new FlowSurface(_context, _pref);
	}
	
	

	public void setPerspective() {
		float ratio = (float) WIDTH / HEIGHT;
		Matrix.frustumM(_mProjMatrix, 0, -ratio, ratio, -1, 1, 0.4f, 10); // 0.4f
		Matrix.setIdentityM(_mMMatrix, 0);
		Matrix.multiplyMM(_mMVPMatrix, 0, _mVMatrix, 0, _mMMatrix, 0);
		Matrix.multiplyMM(_mMVPMatrix, 0, _mProjMatrix, 0, _mMVPMatrix, 0);
		Matrix.invertM(_normalMatrix, 0, _mMVPMatrix, 0);
		Matrix.transposeM(_normalMatrix, 0, _normalMatrix, 0);
	}

	public void setOrtho() {
		float[] tMatrix = new float[16];
		Matrix.setIdentityM(tMatrix, 0);
		Matrix.setLookAtM(tMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

		float ratio = (float) WIDTH / HEIGHT;
		Matrix.orthoM(_mProjMatrix, 0, -ratio, ratio, -1, 1, 0.4f, 10);
		Matrix.setIdentityM(_mMMatrix, 0);
		Matrix.multiplyMM(_mMVPMatrix, 0, tMatrix, 0, _mMMatrix, 0);
		Matrix.multiplyMM(_mMVPMatrix, 0, _mProjMatrix, 0, _mMVPMatrix, 0);
		Matrix.invertM(_normalMatrix, 0, _mMVPMatrix, 0);
		Matrix.transposeM(_normalMatrix, 0, _normalMatrix, 0);
	}

	public static int loadTexture(InputStream is) {
		int[] textureId = new int[1];

		GLES20.glGenTextures(1, textureId, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

		try {
			ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB,
					GLES20.GL_UNSIGNED_BYTE, is);
		} catch (IOException e) {
			Log.e("Flow Render", "Error Loading Texture!");
			e.printStackTrace();
		}

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_MIRRORED_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_MIRRORED_REPEAT);

		return textureId[0];
	}

	public static int loadTextureBitmap(Bitmap bitmap) {

		int[] textureId = new int[1];
	
		byte[] buffer = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];

		for (int y = 0; y < bitmap.getHeight(); y++)
			for (int x = 0; x < bitmap.getWidth(); x++) {
				int pixel = bitmap.getPixel(x, y);
				buffer[(y * bitmap.getWidth() + x) * 3 + 0] = (byte) ((pixel >> 16) & 0xFF);
				buffer[(y * bitmap.getWidth() + x) * 3 + 1] = (byte) ((pixel >> 8) & 0xFF);
				buffer[(y * bitmap.getWidth() + x) * 3 + 2] = (byte) ((pixel >> 0) & 0xFF);
			}

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getWidth()
				* bitmap.getHeight() * 3);
		byteBuffer.put(buffer).position(0);

		GLES20.glGenTextures(1, textureId, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
				bitmap.getWidth(), bitmap.getHeight(), 0, GLES20.GL_RGB,
				GLES20.GL_UNSIGNED_BYTE, byteBuffer);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_MIRRORED_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_MIRRORED_REPEAT);

		return textureId[0];
	}

	/*
	 * 1280 GL_INVALID_ENUM | 1281 GL_INVALID_VALUE | 1282 GL_INVALID_OPERATION | 1283
	 * GL_STACK_OVERFLOW | 1284 GL_STACK_UNDERFLOW | 1285 GL_OUT_OF_MEMORY
	 */
	public static void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("FlowRender", op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	public void setLightPosition(float[] lightPosition) {
		_lightPos = lightPosition;
	}

	/**
	 * Called when the engine is destroyed. Do any necessary clean up because at
	 * this point your renderer instance is now done for.
	 */
	public void release() {
	}



}
