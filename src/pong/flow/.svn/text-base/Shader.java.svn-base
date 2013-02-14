package pong.flow;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class Shader {

	private int _program, _vertexShader, _pixelShader;

	private String _vertex, _fragment;

	public Shader() {
	}

	public int getProgram() {
		return _program;
	}

	public Shader(Context context, int vertexResource, int fragmentResource) {
		StringBuffer vs = new StringBuffer();
		StringBuffer fs = new StringBuffer();

		try {
			InputStream inputStream = context.getResources().openRawResource(
					vertexResource);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					inputStream));

			String read = in.readLine();
			while (read != null) {
				vs.append(read + "\n");
				read = in.readLine();
			}

			vs.deleteCharAt(vs.length() - 1);

			inputStream = context.getResources().openRawResource(
					fragmentResource);

			in = new BufferedReader(new InputStreamReader(inputStream));

			read = in.readLine();
			while (read != null) {
				fs.append(read + "\n");
				read = in.readLine();
			}

			fs.deleteCharAt(fs.length() - 1);
		} catch (Exception e) {
			Log.e("Could not read shader", e.toString());
		}

		this._fragment = fs.toString();
		this._vertex = vs.toString();

		InitShaderProgram();
	}

	public int InitShaderProgram() {
		_vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, _vertex);
		if (_vertexShader == 0) {
			Log.e("Error loading vertex Shader", _vertex);
			return 0;
		}

		_pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, _fragment);
		if (_pixelShader == 0) {
			Log.e("Error loading pixel Shader", _fragment);
			return 0;
		}

		_program = GLES20.glCreateProgram();
		if (_program != 0) {
			GLES20.glAttachShader(_program, _vertexShader);
			GLES20.glAttachShader(_program, _pixelShader);
			GLES20.glLinkProgram(_program);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(_program, GLES20.GL_LINK_STATUS, linkStatus,
					0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				Log.e("Shader", "Program could not link: " + GLES20.glGetProgramInfoLog(_program));
				GLES20.glDeleteProgram(_program);
				_program = 0;
				return 0;
			}
		} else{
			Log.e("Shader", "Program error");
		}

		return 1;

	}

	private int loadShader(int typeCode, String source) {
		int shader = GLES20.glCreateShader(typeCode);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e("Shader error", GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

}
