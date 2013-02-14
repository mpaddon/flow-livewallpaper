precision mediump float;

uniform sampler2D diffuseMap;
uniform sampler2D waveMap1;
uniform sampler2D waveMap2;
//uniform sampler2D noiseMap;
uniform sampler2D flowMap;
uniform vec4 SunColor;
uniform vec4 WaterColor;
uniform float FlowMapOffset0;
uniform float FlowMapOffset1;
uniform float TexScale;
uniform float HalfCycle;
uniform float distortionMult;
uniform float useInput;

varying vec3 EyespaceNormal;
varying vec2 tCoord; 
varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeVec;

vec3 lerp(vec3 normal1, vec3 normal2, float s)
{
	return (normal1 * (1.0 - s)) + (normal2 * s);
}

void main()
{ 
        vec2 modTcoord = vec2(tCoord.x , 1.0 - tCoord.y);
        modTcoord.y = 	(1.0 - tCoord.y);
        
		vec4 finalColor = vec4(0.0);
		vec4 flowColor;
		
		flowColor = texture2D(flowMap, modTcoord);
			
		vec2 flowNormals = flowColor.rg * 2.0 - 1.0;
		//float noise = texture2D(noiseMap, modTcoord).r;
		float phase0 = FlowMapOffset0;
		float phase1 = FlowMapOffset1;
		vec3 normalT0 = texture2D(waveMap1, (modTcoord * TexScale) + flowNormals * phase0).xyz;
		vec3 normalT1 = texture2D(waveMap2, (modTcoord * TexScale) + flowNormals * phase1).xyz;
		float flowLerp = (abs(HalfCycle - FlowMapOffset0) / HalfCycle);
		vec3 normal = normalize(lerp(normalT0, normalT1, flowLerp) * 2.0 - 1.0);
		
		vec4 waveMapTemp = texture2D(waveMap2, tCoord);
		
		vec4 reflectDiffuse = texture2D(diffuseMap, modTcoord + (normal * distortionMult).xy);
			
		vec4 localWaterColor = WaterColor;
		
		if(flowColor.b > 0.0)
		{
			localWaterColor += (0.30 * (flowColor.b));
		}
		
		float distSqr = dot(lightDir, lightDir);
		vec3 L = lightDir * inversesqrt(distSqr); 
		vec3 E = normalize(eyeVec);
		vec3 reflectV = reflect(-L, normal);
		vec4 ambientTerm = reflectDiffuse * localWaterColor * 0.25;
		vec4 diffuseTerm = reflectDiffuse* localWaterColor * max(dot(normal, L), 0.0);
		vec4 specularTerm = reflectDiffuse * SunColor * pow(max(dot(reflectV, E), 0.2), 0.45);
		finalColor = ambientTerm + diffuseTerm + specularTerm;
		gl_FragColor = finalColor ;
}