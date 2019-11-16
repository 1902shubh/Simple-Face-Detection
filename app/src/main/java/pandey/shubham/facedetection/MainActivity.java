package pandey.shubham.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button cameraBtn;
    private final static int REQUEST_IMAGE_CAPTURE=213;
    private FirebaseVisionImage image;
    private FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        cameraBtn=findViewById(R.id.camera_button);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras=data.getExtras();
            Bitmap bitmap=(Bitmap)extras.get("data");
            detectFace(bitmap);   
        }
    }

    private void detectFace(Bitmap bitmap) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)

                        .build();

        // Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();


        try {
            image=FirebaseVisionImage.fromBitmap(bitmap);
            detector= FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        } catch (Exception e) {
            e.printStackTrace();
        }

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String res="";
                int i=1;
                for (FirebaseVisionFace face : firebaseVisionFaces){
                    res=res.concat("\n"+i+".")
                            .concat("\nSmile : "+face.getSmilingProbability()*100+"%")
                            .concat("\nLeft Eye : "+face.getLeftEyeOpenProbability()*100+"%")
                            .concat("\nRight Eye : "+face.getRightEyeOpenProbability()*100+"%");
                    i++;

                    Rect bounds = face.getBoundingBox();
                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                    // nose available):
                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                    if (leftEar != null) {
                        FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                    }

                    // If contour detection was enabled:
                    List<FirebaseVisionPoint> leftEyeContour =
                            face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                    List<FirebaseVisionPoint> upperLipBottomContour =
                            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

                    // If classification was enabled:
                    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        float smileProb = face.getSmilingProbability();
                    }
                    if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                        float rightEyeOpenProb = face.getRightEyeOpenProbability();
                    }

                    // If face tracking was enabled:
                    if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                        int id = face.getTrackingId();
                    }
                }
                if (firebaseVisionFaces.size()==0){
                    Toast.makeText(MainActivity.this, "No Faces", Toast.LENGTH_SHORT).show();
                }
                else {
                    Bundle bundle=new Bundle();
                    bundle.putString(Facedetection.RESULT_TEXT,res);
                    DialogFragment resultDialog=new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(),Facedetection.RESULT_DIALOG);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }
}
