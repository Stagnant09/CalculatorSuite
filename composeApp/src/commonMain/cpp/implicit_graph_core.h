#ifndef IMPLICIT_H
#define IMPLICIT_H

#include <jni.h>

extern "C" {
JNIEXPORT jintArray JNICALL
Java_com_example_calculator_NativePlot_computeImplicit(
        JNIEnv* env,
        jobject,
        jint width,
        jint height,
        jfloat originX,
        jfloat originY,
        jfloat step,
        jfloat scale,
        jfloat threshold,
        jobject evaluator
);
}

#endif
