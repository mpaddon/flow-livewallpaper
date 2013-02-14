package pong.flow;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

interface EGLConfigChooser {
	EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
}