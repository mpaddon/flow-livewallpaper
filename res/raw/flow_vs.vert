uniform mat4 uMVPMatrix;
uniform mat4 normalMatrix;
uniform vec3 eyePos;
uniform vec4 lightPos;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 textureCoord;

varying vec3 EyespaceNormal;
varying vec2 tCoord; 
varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeVec;

void main() 
{
	EyespaceNormal = vec3(normalMatrix * vec4(aNormal, 1.0));
	vNormal = aNormal;
	tCoord = textureCoord;
	
	vec3 tangent; 
	vec3 binormal; 
	vec3 c1 = cross(aNormal, vec3(0.0, 0.0, 1.0)); 
	vec3 c2 = cross(aNormal, vec3(0.0, 1.0, 0.0)); 
	if(length(c1)>length(c2))
	{
		tangent = c1;	
	}
	else
	{
		tangent = c2;	
	}
	tangent = normalize(tangent);
	vec3 eyespaceTangent = vec3(normalMatrix * vec4(tangent, 1.0));
	binormal = cross(EyespaceNormal, eyespaceTangent);
	
	vec4 finalPosition = uMVPMatrix * aPosition;
	vec3 tmpVec = lightPos.xyz - finalPosition.xyz;
	lightDir.x = dot(tmpVec, eyespaceTangent);
	lightDir.y = dot(tmpVec, binormal); 
	lightDir.z = dot(tmpVec, EyespaceNormal);
	// eye space
	tmpVec = -finalPosition.xyz;
	// convert to tangent space
	eyeVec.x = dot(tmpVec, eyespaceTangent);
	eyeVec.y = dot(tmpVec, binormal);
	eyeVec.z = dot(tmpVec, EyespaceNormal);
	
	gl_Position = finalPosition; 
}