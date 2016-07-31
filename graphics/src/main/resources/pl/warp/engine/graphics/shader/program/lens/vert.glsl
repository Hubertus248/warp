#version 330

precision highp float;

uniform vec2 sourcePos;

layout(location = 0) in float offset;
layout(location = 1) in float scale;
layout(location = 2) in int textureIndex;

out vData {
    float scale;
    int textureIndex;
    float visibility;
} pointData;

float getVisibility();

void main(void) {
    pointData.visibility = getVisibility();
    pointData.scale = scale;
    pointData.textureIndex = textureIndex;
    gl_Position.xy = sourcePos * offset;
    gl_Position.w = 1;
}

float getVisibility() {
    float distance = length(sourcePos);
    return 0.5 - (distance / 2);
}