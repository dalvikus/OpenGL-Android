package com.example.android.opengl20;

import android.content.Context;
import android.util.Log;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.content.res.AssetManager;

class Model
{
    private static final String TAG = "Model";

    // 3 * (18096 + 640 + 557 + 1102 + 1232 + 1232 + 24040 + 18195) = 195282
    public float[] va = new float[195282];
    public int[] ia1 = new int[3 * 32226];
    public int[] ia2  = new int[3 * 890];
    public int[] ia3  = new int[3 * 808];
    public int[] ia4  = new int[3 * 1672];
    public int[] ia5  = new int[3 * 1868];
    public int[] ia6  = new int[3 * 1920];
    public int[] ia7  = new int[3 * 39868];
    public int[] ia8  = new int[3 * 35158];
/*
name: |object_1|, # of vertices = 18096, # of faces = 32226
name: |object_2|, # of vertices = 640, # of faces = 890
name: |object_3|, # of vertices = 557, # of faces = 808
name: |object_4|, # of vertices = 1102, # of faces = 1672
name: |object_5|, # of vertices = 1232, # of faces = 1868
name: |object_6|, # of vertices = 1232, # of faces = 1920
name: |object_7|, # of vertices = 24040, # of faces = 39868
name: |object_8|, # of vertices = 18195, # of faces = 35158
 */
    private final AssetManager mAM;
    Model(Context context)
    {
        mAM = context.getAssets();
        setVertexArray("va", va);
        setIndexArray("ia1", ia1);
        setIndexArray("ia2", ia2);
        setIndexArray("ia3", ia3);
        setIndexArray("ia4", ia4);
        setIndexArray("ia5", ia5);
        setIndexArray("ia6", ia6);
        setIndexArray("ia7", ia7);
        setIndexArray("ia8", ia8);
    }

    private void setVertexArray(String vaFile, float[] va)
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mAM.open(vaFile)));
            String line;
            int n = 0;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                try {
                    float f = Float.parseFloat(line);
                    va[i++] = f;
                } catch (NumberFormatException e) {
                    Log.d(TAG, e.getMessage());
                }
                ++n;
            }
            Log.d(TAG, "# of lines = " + n);
            Log.d(TAG, "# of floats = " + i);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }

    private void setIndexArray(String iaFile, int[] iia)
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mAM.open(iaFile)));
            String line;
            int n = 0;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                try {
                    int idx = Integer.parseInt(line);
                    iia[i++] = idx;
                } catch (NumberFormatException e) {
                    Log.d(TAG, e.getMessage());
                }
                ++n;
            }
            Log.d(TAG, "# of lines = " + n);
            Log.d(TAG, "# of ints = " + i);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }
}
