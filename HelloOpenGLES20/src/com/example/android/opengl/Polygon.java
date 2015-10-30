/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;

import android.content.Context;
import android.util.Log;
import java.util.Arrays;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Polygon {
    private static final String TAG = "Polygon";

/*
    private static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";
    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";
 */
    private static final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +    // model/view/projection matrix for screen space
        "uniform mat4 uMVMatrix;" +     // model/view matrix used to transform vector into world space
        "uniform vec3 uLightPosition;" +// position of light in world space.
        "attribute vec4 vPosition;" +   // vertex position in object space
        "uniform vec4 vColor;" +        // vettex color
        "attribute vec3 vNormal;" +     // vertex normal in object space
        "varying vec4 v_Color;" +       // This will be passed into the fragment shader
        "void main()" +
        "{" +
                // transform position into world space
        "   vec3 modelViewVertex = vec3(uMVMatrix * vPosition);" +
                // transform normal into world space
        "   vec3 modelViewNormal = vec3(uMVMatrix * vec4(vNormal, 0.0));" +
        "   vec3 lightVector = normalize(uLightPosition - modelViewVertex);" +
                // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                // pointing in the same direction then it will get max illumination.
        "   float diffuse = max(dot(modelViewNormal, lightVector), 0.0);" +
                // Will be used for attenuation.
//      "   float distance = length(uLightPosition - modelViewVertex);" +
                // Get a lighting direction vector from the light to the vertex.
                // Attenuate the light based on distance.
//      "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));" +
                // Multiply the color by the illumination level. It will be interpolated across the triangle.
        "   v_Color = vColor * diffuse;" +
                // gl_Position is a special variable used to store the final position.
                // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
        "   gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private static final String fragmentShaderCode =
        "precision mediump float;" +    // set default precision to medium
        "varying vec4 v_Color;" +       // this is the color from the vertex shader interpolated across the triangle per fragment.
        "void main()" +
        "{" +
        "   gl_FragColor = v_Color;" +  // Pass the color directly through the pipeline.
        "}";


    private final FloatBuffer mVertexBuffer; // tell if loadking is successful or not; see drawAll
    private final int mProgram;
    private final int mPositionHandle;
    private final int mNormalHandle;
    private final int mLightPositionHandle;
    private final int mMVPMatrixHandle;
    private final int mMVMatrixHandle;
    private final int mColorHandle;

    private static float[] color = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private class Draw
    {
        IntBuffer drawListBuffer;
        int[] drawOrder;
        float[] color;
    }

    private static final int I27 = 27;
    private static final float F3 = 1.0f / 2;
    private static final float ALPHA = 1.0f;
    private float[][] mColor_a = {
        {0 * F3, 0 * F3, 0 * F3, ALPHA},
        {0 * F3, 0 * F3, 1 * F3, ALPHA},
        {0 * F3, 0 * F3, 2 * F3, ALPHA},
        {0 * F3, 1 * F3, 0 * F3, ALPHA},
        {0 * F3, 1 * F3, 1 * F3, ALPHA},
        {0 * F3, 1 * F3, 2 * F3, ALPHA},
        {0 * F3, 2 * F3, 0 * F3, ALPHA},
        {0 * F3, 2 * F3, 1 * F3, ALPHA},
        {0 * F3, 2 * F3, 2 * F3, ALPHA},
        {1 * F3, 0 * F3, 0 * F3, ALPHA},
        {1 * F3, 0 * F3, 1 * F3, ALPHA},
        {1 * F3, 0 * F3, 2 * F3, ALPHA},
        {1 * F3, 1 * F3, 0 * F3, ALPHA},
        {1 * F3, 1 * F3, 1 * F3, ALPHA},
        {1 * F3, 1 * F3, 2 * F3, ALPHA},
        {1 * F3, 2 * F3, 0 * F3, ALPHA},
        {1 * F3, 2 * F3, 1 * F3, ALPHA},
        {1 * F3, 2 * F3, 2 * F3, ALPHA},
        {2 * F3, 0 * F3, 0 * F3, ALPHA},
        {2 * F3, 0 * F3, 1 * F3, ALPHA},
        {2 * F3, 0 * F3, 2 * F3, ALPHA},
        {2 * F3, 1 * F3, 0 * F3, ALPHA},
        {2 * F3, 1 * F3, 1 * F3, ALPHA},
        {2 * F3, 1 * F3, 2 * F3, ALPHA},
        {2 * F3, 2 * F3, 0 * F3, ALPHA},
        {2 * F3, 2 * F3, 1 * F3, ALPHA},
        {2 * F3, 2 * F3, 2 * F3, ALPHA},
    };
    private Draw[] mDraw_a;

    private static final int INT_SIZE_BYTES = 4;
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int COORDS_PER_POSITION = 3;
    private static final int COORDS_PER_UV = 2;
    private static final int COORDS_PER_NORMAL = 3;
    private int TRIANGLE_VERTICES_DATA_STRIDE_BYTES; // (3 + 2 if texture + 3 if normal) * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0; // always
    private boolean mHasTexture;
    private final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;   // valid only if mHasTexure
    private boolean mHasNormal;
    private int TRIANGLE_VERTICES_DATA_NORMAL_OFFSET;   // 5 if texture, 3 unless texture but valid only if mHasNormal
    public Polygon(Context context, String objFile)
    {
        final String oviaFile = objFile + "_ovia.bin";  // change extension (.gz)
        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        MyGLRenderer.checkGlError("glCreateProgram");
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
        Log.d(TAG, "mProgram = " + mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        MyGLRenderer.checkGlError("glGetAttribLocation");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        MyGLRenderer.checkGlError("glGetAttribLocation");

        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");


        float[] fa = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(context.getAssets().open(oviaFile))));
            mHasTexture = ois.readBoolean();
            mHasNormal = ois.readBoolean();
            if (mHasTexture) {
                TRIANGLE_VERTICES_DATA_NORMAL_OFFSET = 5;
            } else {
                TRIANGLE_VERTICES_DATA_NORMAL_OFFSET = 3;
            }
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES = (3 + (mHasTexture ? 2 : 0) + (mHasNormal ? 3 : 0)) * FLOAT_SIZE_BYTES;
            fa = (float[]) ois.readObject();
            Log.d(TAG, "IN: # of floats = " + fa.length);

            int k = ois.readInt();
            Log.d(TAG, "IN: # of groups = " + k);
            mDraw_a = new Draw[k];
            for (int i = 0; i < k; ++i) {
                String name = (String) ois.readObject();
                Log.d(TAG, String.format("IN: name: |%s|%n", name));
                mDraw_a[i] = new Draw();
                mDraw_a[i].drawOrder = (int[]) ois.readObject();
                Log.d(TAG, "IN: # of indides = " + mDraw_a[i].drawOrder.length);
                ByteBuffer dlb = ByteBuffer.allocateDirect(mDraw_a[i].drawOrder.length * INT_SIZE_BYTES);
                dlb.order(ByteOrder.nativeOrder());
                mDraw_a[i].drawListBuffer = dlb.asIntBuffer();
                mDraw_a[i].drawListBuffer.put(mDraw_a[i].drawOrder);
                mDraw_a[i].drawListBuffer.position(0);

                mDraw_a[i].color = mColor_a[i % I27];
            }
            ois.close();
        } catch (IOException e) {     // new FileOutputStream(...)
            Log.e(TAG, "IOException: " + e.getMessage());
            fa = null;
            mDraw_a = null;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException: " + e.getMessage());
            fa = null;
            mDraw_a = null;
        }
        if (fa != null) {
            // initialize vertex byte buffer for shape coordinates
            // (# of coordinate values * 4 bytes per float)
            ByteBuffer bb = ByteBuffer.allocateDirect(fa.length * FLOAT_SIZE_BYTES);
            bb.order(ByteOrder.nativeOrder());
            mVertexBuffer = bb.asFloatBuffer();
            mVertexBuffer.put(fa);
            mVertexBuffer.position(0);
            fa = null; // no more use
        } else {
            mVertexBuffer = null;
        }
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void drawAll(float[] mvpMatrix, float[] mvMatrix) {
        if (mVertexBuffer == null)
            return;
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray");
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        MyGLRenderer.checkGlError("glEnableVertexAttribArray");

        // Prepare the triangle coordinate data
////  public static void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr)
        mVertexBuffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_POSITION,
                GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mVertexBuffer);
        mVertexBuffer.position(TRIANGLE_VERTICES_DATA_NORMAL_OFFSET);
        GLES20.glVertexAttribPointer(
                mNormalHandle, COORDS_PER_NORMAL,
                GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mVertexBuffer);

        // Apply the projection and view transformation
////  public static void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glUniform3f(mLightPositionHandle, 0.0f, 0.0f, 10.0f);
        for (int i = 0; i < mDraw_a.length; ++i) {
                // Set color for drawing the triangle
        ////  public static void glUniform4iv (int location, int count, int[] v, int offset)
                GLES20.glUniform4fv(mColorHandle, 1, mColor_a[i], 0);
                // Draw the square
        ////  public static void glDrawElements (int mode, int count, int type, Buffer indices)
                GLES20.glDrawElements(
                        GLES20.GL_TRIANGLES, mDraw_a[i].drawOrder.length,
                        GLES20.GL_UNSIGNED_INT, mDraw_a[i].drawListBuffer);
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }
}
