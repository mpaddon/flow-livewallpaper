package pong.flow;

import android.opengl.Matrix;

public class Camera {
	public Vec3 Eye;
	public Vec3 LookAt;
	public Vec3 Up, U, V, W, N;
	float[] _mVMatrix;

	public Camera(float[] _mVMatrix) {
		Eye = new Vec3(0.0f, 0.0f, -2f);
		LookAt = new Vec3(0, 0, 0.0f);
		Up = new Vec3(0, 1.0f, 0);
		U = new Vec3();
		W = new Vec3();
		N = new Vec3();
		this._mVMatrix = _mVMatrix;
		// initCameraVecs();
	}

	public void initCameraVecs() {
		N = (Eye.Minus(LookAt));
		N.Normalize();
		U = (Up.Cross(N));
		U.Normalize();
		V = N.Cross(U);

	}

	public float[] getMVMatrix() {
		float[] matrix = new float[16];
		matrix[0] = U.x;
		matrix[4] = U.y;
		matrix[8] = U.z;
		matrix[12] = -Eye.Dot(Up);
		matrix[1] = V.x;
		matrix[5] = V.y;
		matrix[9] = V.z;
		matrix[13] = -Eye.Dot(V);
		matrix[2] = N.x;
		matrix[6] = N.y;
		matrix[10] = N.z;
		matrix[14] = -Eye.Dot(N);
		matrix[3] = 0;
		matrix[7] = 0;
		matrix[11] = 0;
		matrix[15] = 1.0f;
		return matrix;
	}

	public void setLookAtM() {

		Matrix.setLookAtM(_mVMatrix, 0, Eye.x, Eye.y, Eye.z, LookAt.x,
				LookAt.y, LookAt.z, 0f, 1.0f, 0.0f);
	}

	public void rotateCameraAroundY(float angle) {
		float[] mEyeMatrix = new float[16];
		float[] mEyePointTemp = new float[4];
		float[] mEyePoint = new float[4];

		mEyeMatrix[0] = 1;
		mEyeMatrix[1] = 0;
		mEyeMatrix[2] = 0;
		mEyeMatrix[3] = 0;
		mEyeMatrix[4] = 0;
		mEyeMatrix[5] = 1;
		mEyeMatrix[6] = 0;
		mEyeMatrix[7] = 0;
		mEyeMatrix[8] = 0;
		mEyeMatrix[9] = 0;
		mEyeMatrix[10] = 1;
		mEyeMatrix[11] = 0;
		mEyeMatrix[12] = 0;
		mEyeMatrix[13] = 0;
		mEyeMatrix[14] = 1;
		mEyeMatrix[15] = 1;

		mEyePoint[0] = Eye.x;
		mEyePoint[1] = Eye.y;
		mEyePoint[2] = Eye.z;
		mEyePoint[3] = 0;

		Matrix.rotateM(mEyeMatrix, 0, angle, 0, 1, 0);
		Matrix.multiplyMV(mEyePointTemp, 0, mEyeMatrix, 0, mEyePoint, 0);

		Eye.x = mEyePointTemp[0];
		Eye.y = mEyePointTemp[1];
		Eye.z = mEyePointTemp[2];

		// Matrix.setLookAtM(_mVMatrix, 0, Eye.x, Eye.y, Eye.z, 0.0f, 0f, 0f,
		// 0f, 1.0f, 0.0f);
	}

	public void rotateCameraAroundX(float angle) {
		float[] mEyeMatrix = new float[16];
		float[] mEyePointTemp = new float[4];
		float[] mEyePoint = new float[4];

		mEyeMatrix[0] = 1;
		mEyeMatrix[1] = 0;
		mEyeMatrix[2] = 0;
		mEyeMatrix[3] = 0;
		mEyeMatrix[4] = 0;
		mEyeMatrix[5] = 1;
		mEyeMatrix[6] = 0;
		mEyeMatrix[7] = 0;
		mEyeMatrix[8] = 0;
		mEyeMatrix[9] = 0;
		mEyeMatrix[10] = 1;
		mEyeMatrix[11] = 0;
		mEyeMatrix[12] = 0;
		mEyeMatrix[13] = 0;
		mEyeMatrix[14] = 1;
		mEyeMatrix[15] = 1;

		mEyePoint[0] = Eye.x;
		mEyePoint[1] = Eye.y;
		mEyePoint[2] = Eye.z;
		mEyePoint[3] = 0;

		Matrix.rotateM(mEyeMatrix, 0, angle, 1, 0, 0);
		Matrix.multiplyMV(mEyePointTemp, 0, mEyeMatrix, 0, mEyePoint, 0);

		Eye.x = mEyePointTemp[0];
		Eye.y = mEyePointTemp[1];
		Eye.z = mEyePointTemp[2];

	}

	public void zoom(float zoomVal) {
		float[] mEyeMatrix = new float[16];
		float[] mEyePointTemp = new float[4];
		float[] mEyePoint = new float[4];

		mEyeMatrix[0] = zoomVal;
		mEyeMatrix[1] = 0;
		mEyeMatrix[2] = 0;
		mEyeMatrix[3] = 0;
		mEyeMatrix[4] = 0;
		mEyeMatrix[5] = zoomVal;
		mEyeMatrix[6] = 0;
		mEyeMatrix[7] = 0;
		mEyeMatrix[8] = 0;
		mEyeMatrix[9] = 0;
		mEyeMatrix[10] = zoomVal;
		mEyeMatrix[11] = 0;
		mEyeMatrix[12] = 0;
		mEyeMatrix[13] = 0;
		mEyeMatrix[14] = 1;
		mEyeMatrix[15] = 1;

		mEyePoint[0] = Eye.x;
		mEyePoint[1] = Eye.y;
		mEyePoint[2] = Eye.z;
		mEyePoint[3] = 0;

		// Matrix.scaleM(mEyeMatrix, 0, zoomVal, 1, 0, 0);
		Matrix.multiplyMV(mEyePointTemp, 0, mEyeMatrix, 0, mEyePoint, 0);

		Eye.x = mEyePointTemp[0];
		Eye.y = mEyePointTemp[1];
		Eye.z = mEyePointTemp[2];

	}

	public void reset() {
		// TODO Auto-generated method stub
		Eye.x = 0;
		Eye.y = 1;
		Eye.z = -1;
	}

}
