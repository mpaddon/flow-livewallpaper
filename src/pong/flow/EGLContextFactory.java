package pong.flow;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public interface EGLContextFactory {
	EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);

	void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
}
