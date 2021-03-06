package pong.flow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

public class FlowEnvironment {
	//textures
		private FloatBuffer _vertexBuffer;
		private float _width, _height;
		Shader phong;
		Context _context;

	public FlowEnvironment(Context context)
	{
		_context = context;
		
	}
	
	public void setDimensions(int w, int h)
	{
		_width = (float)w / 100.0f; 
		_height = (float)h / 100.0f; 
		float ratio = _width/_height; 
		_width = ratio * 5.0f; 
		_height = 1.0f * 5.0f;
		generateSurface();
	}
	
	public void onDrawFrame(float[] _uMVPMatrix, float[] normalMatrix)
	{
	GLES20.glClearColor(.0f, .0f, .0f, 1.0f);
		
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		int _program = phong.getProgram();
		GLES20.glUseProgram(_program);
		FlowRenderer.checkGlError("glUseProgram");
		int mvpLoc = GLES20.glGetUniformLocation(_program, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mvpLoc, 1, false, _uMVPMatrix, 0);
		 GLES20.glUniformMatrix4fv(
	                GLES20.glGetUniformLocation(_program, "normalMatrix"), 1,
	                false, normalMatrix, 0);
		
	}
	
	private void generateSurface()
	{
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

		float moneyCoords[] =
		{ // locations
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
				0,1, 1,1, 0,0, 0,0, 1,0, 1,1};

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
}
