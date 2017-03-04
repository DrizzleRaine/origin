layout (location = 0) out vec4 fragColor;
layout (location = 1) out vec4 fragColor2;

uniform samplerCube u_texture1;
uniform samplerCube u_texture2;
uniform float u_blendValue;

uniform vec3 u_skyColor;

in vec3 texCoords;

const float lowerLimit = 0.0;
const float upperLimit = 20.0;

// =====================================================================================================================
//
// Atmospheric scattering fragment shader
//
// Author: Sean O'Neil
//
// Copyright (c) 2004 Sean O'Neil
//
uniform vec3 v3InvWavelength;	// 1 / pow(wavelength, 4) for the red, green, and blue channels

uniform vec3 v3LightPosition;
uniform float g;
uniform float g2;

in vec3 v3Direction;
in vec3 c0;
in vec3 c1;

// Calculates the Mie phase function
float getMiePhase(float fCos, float fCos2, float g, float g2)
{
	return 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos2) / pow(1.0 + g2 - 2.0 * g * fCos, 1.5);
}

// Calculates the Rayleigh phase function
float getRayleighPhase(float fCos2)
{
	return 0.75 + 0.75 * fCos2;
}

void main (void)
{
	vec4 color1 = texture(u_texture1, texCoords);
	vec4 color2 = texture(u_texture2, texCoords);
	vec4 finalColor = mix(color1, color2, u_blendValue);

	float fCos = dot(v3LightPosition, v3Direction) / length(v3Direction);
	float fCos2 = fCos * fCos;

	vec3 color =	getRayleighPhase(fCos2) * c0 +
					getMiePhase(fCos, fCos2, g, g2) * c1 * 0.1;

 	fragColor = vec4(color, 1.0);
	fragColor.a = fragColor.b;

//	fragColor = vec4( v3, 1.0);
//	fragColor = vec4( vec3(getRayleighPhase(fCos2)), 1.0);
//	fragColor = finalColor * getRayleighPhase(fCos2) + getMiePhase(fCos, fCos2, g, g2) * 0.02;
//	fragColor = vec4( vec3(getMiePhase(fCos, fCos2, g, g2) * 0.01), 1.0);

	// небо из текстур
	float factor = (texCoords.y - lowerLimit) / (upperLimit - lowerLimit);
	factor = clamp(factor, 0.0, 1.0);
//	fragColor = finalColor;
//	fragColor.a = 1;
//	fragColor = mix(vec4(u_skyColor, 1.0), finalColor, factor);

	fragColor2 = vec4(0);
}


//void main() {
//	vec4 color1 = texture(u_texture1, texCoords);
//	vec4 color2 = texture(u_texture2, texCoords);
//	vec4 finalColor = mix(color1, color2, u_blendValue);
//
//	float factor = (texCoords.y - lowerLimit) / (upperLimit - lowerLimit);
//	factor = clamp(factor, 0.0, 1.0);
//	fragColor = mix(vec4(u_skyColor, 1.0), finalColor, factor);
//
//	fragColor2 = vec4(0);
//}

