precision highp float;
uniform sampler2D flowMap;
uniform sampler2D themeFlowMap;
uniform float usingTheme; // 1 = true
uniform vec3 phoneRotation;
uniform vec2 touch;
uniform vec2 dir;
uniform float clear;
uniform float clock;
uniform float alternate;
uniform float disipationRate;
uniform float brushStroke;
uniform float screenRatio;
uniform float width;
uniform float height;
varying vec2 tCoord;
float blurSize = 1.0/300.0;
//r = x vec g = y vec b = lifecycle a = reserved

float mCircle(vec2 coord, vec2 t_in)
{
	vec2 subtr = coord - t_in;
	return sqrt(pow(subtr.x,2.0 ) + pow(subtr.y / screenRatio, 2.0));
}

void main()
{
	vec4 finalColor;
	vec2 modTCoord = vec2((1.0 - tCoord.x) * 2.0, (1.0 - tCoord.y) * 2.0);
	
	if(clear == 1.0)
	{
		finalColor = texture2D(themeFlowMap, modTCoord);	
	}
	else
	{
		vec2 modTouch = vec2((touch.x + 1.0) / 2.0,(1.0 - touch.y) / 2.0);
		float dist = mCircle(tCoord * 2.0, modTouch * 2.0);
		if(dist < brushStroke)
		{
			   //pack from -1 -> 1 into 0 -> 1
			   vec2 modTex = vec2((1.0 - tCoord.x) * 2.0, (tCoord.y) * 2.0);
			   vec4 texColor = texture2D(flowMap, modTex);
			   
			   //dist from range 0->brushStroke
			   float range = 1.0 - (dist * (1.0 / brushStroke));
			   finalColor = vec4((dir.x / 2.0) + 0.5 , (dir.y / 2.0) + 0.5,0.0,1.0); 
			   finalColor = (finalColor * range) + (texColor * (1.0 - range));
			   finalColor.b = 1.0;
		}
		else 
		{
				vec2 modTex = vec2((1.0 - tCoord.x) * 2.0, (tCoord.y) * 2.0);
				vec4 background;
				
				background = texture2D(themeFlowMap, modTCoord);
				
				if(clock > 0.0)
				{
				vec4 sum = vec4(0.0); //texture2D(flowMap, modTex);
				if(alternate == 1.0) 
				{
			  	 sum += texture2D(flowMap, vec2(modTex.x - 4.0*blurSize, modTex.y)) * 0.05;
			  	 sum += texture2D(flowMap, vec2(modTex.x - 3.0*blurSize, modTex.y)) * 0.09;
			  	 sum += texture2D(flowMap, vec2(modTex.x - 2.0*blurSize, modTex.y)) * 0.12;
			  	 sum += texture2D(flowMap, vec2(modTex.x - blurSize, modTex.y)) * 0.14;
			  	 sum += texture2D(flowMap, vec2(modTex.x, modTex.y)) * (0.20 - disipationRate);
			  	 sum += texture2D(flowMap, vec2(modTex.x + blurSize, modTex.y)) * 0.14;
			  	 sum += texture2D(flowMap, vec2(modTex.x + 2.0*blurSize, modTex.y)) * 0.12;
			  	 sum += texture2D(flowMap, vec2(modTex.x + 3.0*blurSize, modTex.y)) * 0.09;
			  	 sum += texture2D(flowMap, vec2(modTex.x + 4.0*blurSize, modTex.y)) * 0.05;
			  	 sum +=  background * disipationRate; //0.03
			  	 } 
			  	 else
			  	 {
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y - 4.0*blurSize)) * 0.05;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y - 3.0*blurSize)) * 0.09;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y - 2.0*blurSize)) * 0.12;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y - blurSize)) * 0.14;
			  	 sum += texture2D(flowMap, vec2(modTex.x, modTex.y)) * (0.20 - disipationRate);
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y + blurSize)) * 0.14;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y + 2.0*blurSize)) * 0.12;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y + 3.0*blurSize)) * 0.09;
			  	 sum += texture2D(flowMap, vec2(modTex.x , modTex.y + 4.0*blurSize)) * 0.05;
			  	 sum +=  background * disipationRate;
			  	 }
				 finalColor = sum;
				}
				else
				{
					finalColor = texture2D(flowMap, modTex);
				}
		}
	}
	gl_FragColor = finalColor;
}