package pong.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;

public class FlowTheme {

	
	
	public int WaterColor;
	public int SunColor;

	
	//textureID
	public int diffuseTextureID;
	public int wave1TextureID;
	public int wave2TextureID;
	public int flowMapID;
	public float[] lightLocation;
	public float waveIntensity;
	public float flowSpeed;
	public float flowDistortion;
	public float inputDis;
	

	
public static Map<Long, FlowTheme> _themeTable;
	
    static {
    	Map<Long, FlowTheme> aMap = new HashMap<Long, FlowTheme>();
        aMap.put((long)3, new FlowTheme(-15521879, -2756158, R.raw.bg_jellyfish, R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.bg_jellyfish_flowmap, null, .4f, .60f, .12f, .66f));
        aMap.put((long)4, new FlowTheme( -5268141,-7371201 , R.raw.bg_sand_footprint,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.bg_sand_footprint2_flowmap, null, .23f, .25f, .65f, .5f));
        aMap.put((long)6, new FlowTheme(-7898162,-592251, R.raw.bg_tile_spiral,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.bg_tile_spiral_flowmap, null, .45f, .87f, .31f, .37f));
        aMap.put((long)7, new FlowTheme(-9874232, -263224, R.raw.bg_tile_teal,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.default_flowmap, null, .4f,  .4f,.3f, .5f));
        aMap.put((long)8, new FlowTheme(-14131952, -4069650, R.raw.bg_goo,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.default_flowmap, null, .35f,  .22f,.70f, .0f));
        aMap.put((long)9, new FlowTheme(Color.argb(255, (int)(.0627451*255), (int)(.22352941*255), (int)(.32156864*255)), Color.argb(255, (int)(1*255), (int)(1*255), (int)(1*255)), R.raw.bg_stones_mod,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.default_flowmap, null, .3f,  .5f,.25f, .2f));
        aMap.put((long)10,new FlowTheme(-8931610, -808278, R.raw.bg_lava,  R.raw.wave5_nrm, R.raw.wave6_nrm, R.raw.default_flowmap, null, 1f,  .6f,1f, .0f));
        _themeTable  = Collections.unmodifiableMap(aMap);
    }
	
	
	
	public FlowTheme(int waterColor, int sunColor, int diffTexture, int wavetex1, int wavetex2, int flowMap, float[] light_location, float wave_intensity, float flow_speed, float flow_distortion, float input_dis )
	{
		WaterColor = waterColor;
		SunColor = sunColor;
		diffuseTextureID = diffTexture;
		wave1TextureID = wavetex1;
		wave2TextureID = wavetex2;
	    flowMapID = flowMap;
	    
	     lightLocation = light_location;
		 waveIntensity = wave_intensity;
	     flowSpeed = flow_speed;
		flowDistortion = flow_distortion;
		inputDis = input_dis;

	}
	
	
	

}

