package pong.flow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.net.Uri;
import android.opengl.GLES20;
import android.provider.MediaStore;
import android.util.Log;

public class FlowSurface {

	public String TAG = "FlowSurface";
	public boolean Initialized = false;
	public final static float MAX_FLOW_SPEED = .1f;
	public final static float MAX_DIST_MULT = 0.1f;
	public static final float MAX_WAVE_MULT = 2.0f;

	// Flow Animation
	private final float Cycle = .15f;
	private final float HalfCycle = Cycle * .5f;

	private float _flowMapOffset0 = 0.0f;
	private float _flowMapOffset1 = HalfCycle;

	private static float FlowSpeed = .05f;
	// public static float NoiseMultiplier = 0.0085f;
	public static float DistortionMultiplier = 0.05f;
	public static float WaveScale = 1.0f;

	// Themes
	private FloatBuffer _vertexBuffer;
	private float _width, _height;
	Context _context;
	SharedPreferences _pref;

	private int _diffuseTexture;

	private int _wave1Texture;

	private int _wave2Texture;

	private int _flowMap;

	private float[] _waterColor = { 1, (float) .7, (float) .5, (float) .6 };

	private float[] _sunColor = { 1, (float) .7, (float) .5, (float) .6 };

	private Bitmap _curBack;

	public FlowSurface(Context context, SharedPreferences pref) {
		_context = context;
		// _noiseTexture =
		// FlowRenderer.loadTexture(_context.getResources().openRawResource(R.raw.noise3));
		// _flowMap =
		// FlowRenderer.loadTextureBitmap(BitmapFactory.decodeStream(_context.getResources().openRawResource(R.raw.bg_beach_2_flowmap)));
		// CurrentTheme = new FlowTheme(context, new float[]{0.5f, 0.79f, 0.75f,
		// 1.0f}, new float[]{1.0f, 0.8f, 0.4f, 1.0f}, R.raw.radial,
		// R.raw.wavemap, R.raw.wavemap2, R.raw.flowmap, true);
		_pref = pref;

		initPreferences();
		buildThemeTable();

		// WaterColor = waterColor;
		// SunColor = sunColor;
	}

	public void setBackground(Bitmap bitmap) {

		IntBuffer i = IntBuffer.allocate(1);
		i.put(0, _diffuseTexture);
		GLES20.glDeleteTextures(1, i);
		_diffuseTexture = FlowRenderer.loadTextureBitmap(bitmap);
		if (_curBack != null) {
			_curBack.recycle();
		}
		_curBack = bitmap;
	}

	private void buildThemeTable() {

	}

	public void setFlowSpeed(float flowspeed) {
		FlowSpeed = flowspeed;
	}

	public void setFlowDistortion(float f) {
		// TODO Auto-generated method stub
		DistortionMultiplier = f;

	}

	public void setFlowWave(float f) {
		// TODO Auto-generated method stub
		WaveScale = f;

	}

	public void setWaterColor(float[] waterColor) {
		_waterColor[0] = waterColor[0];
		_waterColor[1] = waterColor[1];
		_waterColor[2] = waterColor[2];
	}

	public void setSunColor(float[] sunColor) {
		_sunColor[0] = sunColor[0];
		_sunColor[1] = sunColor[1];
		_sunColor[2] = sunColor[2];
	}

	private void initPreferences() {

		int color;
		int id;
		long imageID;

		imageID = _pref.getLong("background_photo_preference", -1);
		if (imageID != -1) {

			Uri uri = Uri.withAppendedPath(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					Integer.toString((int) imageID));

			Bitmap background = FlowLiveWallpaperService.loadFullImage(
					_context, uri);
			this.setBackground(background);
			_curBack = background;
			// be sure to freebackground

		} else {

			id = _pref.getInt("diffuse_preference", R.raw.bg_tile_teal);
			_diffuseTexture = FlowRenderer.loadTexture(_context.getResources()
					.openRawResource(id));
		}

		color = _pref.getInt("water_color_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.WaterColor);
		setWaterColor(new float[] { (float) Color.red(color) / (float) 255,
				(float) Color.green(color) / (float) 255,
				(float) Color.blue(color) / (float) 255 });

		color = _pref.getInt("sun_color_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.SunColor);
		setSunColor(new float[] { (float) Color.red(color) / (float) 255,
				(float) Color.green(color) / (float) 255,
				(float) Color.blue(color) / (float) 255 });

		id = _pref.getInt("wave1_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.wave1TextureID);
		_wave1Texture = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(id));

		id = _pref.getInt("wave2_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.wave2TextureID);
		_wave2Texture = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(id));

		float val = _pref.getFloat("flow_speed_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.flowSpeed);
		setFlowSpeed(val * FlowSurface.MAX_FLOW_SPEED);

		val = _pref.getFloat("flow_distortion_preference",
				(float) FlowLiveWallpaperService.DEFAULT_THEME.flowDistortion);
		setFlowDistortion(val * FlowSurface.MAX_DIST_MULT);

		val = _pref.getFloat("flow_wave_preference",
				(float) FlowLiveWallpaperService.DEFAULT_THEME.waveIntensity);
		setFlowWave(val * FlowSurface.MAX_WAVE_MULT);

		id = _pref.getInt("flowmap_preference",
				FlowLiveWallpaperService.DEFAULT_THEME.flowMapID);

		_flowMap = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(id));

		// _flowMap =
		// FlowRenderer.loadTextureBitmap(BitmapFactory.decodeStream(_context.getResources().openRawResource(R.raw.bg_flow_test)));
	}

	public void setDimensions(int w, int h) {
		_width = (float) w / 100.0f;
		_height = (float) h / 100.0f;
		float ratio = _width / _height;
		_width = ratio * 5.0f;
		_height = 1.0f * 5.0f;
		generateSurface();
		Initialized = true;
	}

	public void onResume() {
		_flowMapOffset0 = 0.0f;
		_flowMapOffset1 = HalfCycle;
		Log.e(TAG, "on resume!");
	}

	public void Update(float deltaTime) {
		_flowMapOffset0 += FlowSpeed * deltaTime;
		_flowMapOffset1 += FlowSpeed * deltaTime;
		if (_flowMapOffset0 >= Cycle)
			_flowMapOffset0 = 0.0f;
		if (_flowMapOffset1 >= Cycle)
			_flowMapOffset1 = 0.0f;

		// Quick check to account for massive deltaTime value
		double offset = Math.abs(_flowMapOffset0 - _flowMapOffset1);
		// Log.e(TAG, offset + " halfcycle " + HalfCycle);
		if (offset > (HalfCycle + 0.005) || offset < (HalfCycle - 0.005)) {
			onResume();
		}
	}

	public void onDrawFrame(int coreProgram) {
		int _diffuseLoc = GLES20
				.glGetUniformLocation(coreProgram, "diffuseMap");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _diffuseTexture);
		GLES20.glUniform1i(_diffuseLoc, 0);

		int _wave1Loc = GLES20.glGetUniformLocation(coreProgram, "waveMap1");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _wave1Texture);
		GLES20.glUniform1i(_wave1Loc, 1);

		int _wave2Loc = GLES20.glGetUniformLocation(coreProgram, "waveMap2");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _wave2Texture);
		GLES20.glUniform1i(_wave2Loc, 2);

		/*
		 * int _noiseLoc = GLES20.glGetUniformLocation(coreProgram, "noiseMap");
		 * GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
		 * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _noiseTexture);
		 * GLES20.glUniform1i(_noiseLoc, 3);
		 */

		if (!FlowRenderer.EnableInput) {
			int _flowLoc = GLES20.glGetUniformLocation(coreProgram, "flowMap");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _flowMap);
			GLES20.glUniform1i(_flowLoc, 3);
		}

		if (FlowRenderer.EnableInput) {
			int _flowLoc = GLES20.glGetUniformLocation(coreProgram, "flowMap");
			GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
					FlowInteraction.renderTex[0]);
			GLES20.glUniform1i(_flowLoc, 3);
		}

		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "FlowMapOffset0"),
				_flowMapOffset0);
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "FlowMapOffset1"),
				_flowMapOffset1);
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "HalfCycle"),
				HalfCycle);
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "TexScale"), WaveScale);
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "distortionMult"),
				DistortionMultiplier);
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(coreProgram, "useInput"),
				(FlowRenderer.EnableInput) ? 1.0f : 0.0f);

		GLES20.glUniform4fv(
				GLES20.glGetUniformLocation(coreProgram, "WaterColor"), 1,
				_waterColor, 0);

		GLES20.glUniform4fv(
				GLES20.glGetUniformLocation(coreProgram, "SunColor"), 1,
				_sunColor, 0);

		_vertexBuffer.position(0);
		GLES20.glVertexAttribPointer(
				GLES20.glGetAttribLocation(coreProgram, "aPosition"), 3,
				GLES20.GL_FLOAT, false, 0, _vertexBuffer);
		GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(
				coreProgram, "aPosition"));

		_vertexBuffer.position(18);
		GLES20.glVertexAttribPointer(
				GLES20.glGetAttribLocation(coreProgram, "aNormal"), 3,
				GLES20.GL_FLOAT, false, 0, _vertexBuffer);
		GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(
				coreProgram, "aNormal"));

		// texture coordinates
		_vertexBuffer.position(36);
		GLES20.glVertexAttribPointer(
				GLES20.glGetAttribLocation(coreProgram, "textureCoord"), 2,
				GLES20.GL_FLOAT, false, 0, _vertexBuffer);
		GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(
				coreProgram, "textureCoord"));

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		FlowRenderer.checkGlError("glDrawArrays");
	}

	private void generateSurface() {
		// top left
		Vec3 p1 = new Vec3(-_width, _height, 0);
		// top right
		Vec3 p2 = new Vec3(_width, _height, 0);
		// bottom left
		Vec3 p3 = new Vec3(-_width, -_height, 0);
		// bottom left
		Vec3 p4 = new Vec3(-_width, -_height, 0);
		// bottom right
		Vec3 p5 = new Vec3(_width, -_height, 0);
		// top right
		Vec3 p6 = new Vec3(_width, _height, 0);

		// face 1
		Vec3 v1 = p2.Minus(p1);
		Vec3 v2 = p3.Minus(p1);
		Vec3 norm1 = v1.Cross(v2);
		norm1.Normalize();

		// face2
		Vec3 v3 = p4.Minus(p5);
		Vec3 v4 = p6.Minus(p5);
		Vec3 norm2 = v3.Cross(v4);
		norm2.Normalize();

		Vec3 avgNorm = norm1.Plus(norm2);
		avgNorm.Normalize();

		float moneyCoords[] = { // locations
		p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p4.x, p4.y, p4.z,
				p5.x, p5.y, p5.z, p6.x,
				p6.y,
				p6.z,
				// normals
				norm1.x, norm1.y, norm1.z, avgNorm.x, avgNorm.y, avgNorm.z,
				avgNorm.x, avgNorm.y, avgNorm.z, avgNorm.x, avgNorm.y,
				avgNorm.z, norm2.x, norm2.y, norm2.z, avgNorm.x, avgNorm.y,
				avgNorm.z,
				// tex coord.
				0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 1, 1 };

		ByteBuffer vbb = ByteBuffer.allocateDirect(

		// (# of coordinate values * 4 bytes per float)
				moneyCoords.length * 4);

		vbb.order(ByteOrder.nativeOrder());// use the device hardware's native
											// byte order
		_vertexBuffer = vbb.asFloatBuffer(); // create a floating point buffer
		// from the ByteBuffer
		_vertexBuffer.put(moneyCoords); // add the coordinates to the
		// FloatBuffer
		_vertexBuffer.position(0); // set the buffer to read the first
		// coordinate
	}

	public void setBackgroundCompRes(int bacgroundCompRes) {
		IntBuffer i = IntBuffer.allocate(1);
		i.put(0, _diffuseTexture);
		GLES20.glDeleteTextures(1, i);
		_diffuseTexture = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(bacgroundCompRes));

		i = IntBuffer.allocate(1);
		i.put(0, _flowMap);
		GLES20.glDeleteTextures(1, i);

		_flowMap = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(FlowLiveWallpaperService.GALLERY_FLOWMAP_ID));

		if (_curBack != null) {
			_curBack.recycle();
			_curBack = null;
		}

	}

	public int getFlowMap() {
		return _flowMap;
	}

	public void setflowmap(int flowmapCompRes) {
		IntBuffer i = IntBuffer.allocate(1);
		i.put(0, _flowMap);
		GLES20.glDeleteTextures(1, i);

		_flowMap = FlowRenderer.loadTexture(_context.getResources()
				.openRawResource(flowmapCompRes));

	}

}
