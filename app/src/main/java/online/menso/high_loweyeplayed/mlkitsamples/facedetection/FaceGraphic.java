// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package online.menso.high_loweyeplayed.mlkitsamples.facedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.Log;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import online.menso.high_loweyeplayed.MainActivity;
import online.menso.high_loweyeplayed.mlkitsamples.common.GraphicOverlay;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private int facing;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;

    public FaceGraphic(GraphicOverlay overlay, FirebaseVisionFace face, int facing) {
        super(overlay);

        firebaseVisionFace = face;
        this.facing = facing;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }


        Log.i("MENSO V","Vertical:" +face.getHeadEulerAngleY());
        Log.i("MENSO H","Horizontal:" +face.getHeadEulerAngleZ());


        Message msg = MainActivity.handler.obtainMessage();
        msg.what = MainActivity.FACE_FEATURES_STATUS;
        msg.obj = new double[]{face.getLeftEyeOpenProbability(), face.getRightEyeOpenProbability(), face.getSmilingProbability()};
        MainActivity.handler.sendMessage(msg);

        msg= MainActivity.handler.obtainMessage();
        msg.what = MainActivity.HEAD_ORIENTATION_X;
        msg.obj = face.getHeadEulerAngleZ();
        MainActivity.handler.sendMessage(msg);

        msg= MainActivity.handler.obtainMessage();
        msg.what = MainActivity.HEAD_ORIENTATION_Y;
        msg.obj = face.getHeadEulerAngleY();
        MainActivity.handler.sendMessage(msg);

    }

}
