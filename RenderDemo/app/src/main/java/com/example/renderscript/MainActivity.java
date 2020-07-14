package com.example.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.q.renderscriptexample.ScriptC_histEQ;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private Button convert;
    private ImageView displayer;
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayer = findViewById(R.id.imageView);
        convert = findViewById(R.id.submit);
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                displayer.setImageBitmap(
                displayer.setImageBitmap(processs(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.adhar)));
//                        );
            }
        });



        ;
    }

    public Bitmap processs(Bitmap b){

        Mat mat = new Mat();
        Bitmap bmp32 = b.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat rgb = new Mat();
        Mat thresh = new Mat();

        Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2GRAY);

        Imgproc.threshold(rgb,thresh,200,225,Imgproc.THRESH_BINARY);
        Bitmap bmp = null;
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);

            bmp = Bitmap.createBitmap(thresh.cols(), thresh.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(thresh, bmp);
        } catch (CvException e) {
            Log.d("Exception", e.getMessage());
        }

        RenderScript mRS = RenderScript.create(this);

        // Loads example image

        Allocation inputAllocation = Allocation.createFromBitmap(mRS, bmp);

        // Allocation where to store the sum result (for output purposes)
        Allocation sumAllocation = Allocation.createSized(mRS, Element.I32(mRS), 1);

        // Init script
        ScriptC_histEQ scriptC_average = new  ScriptC_histEQ(mRS);

        // If you have a cycle, you have to reset the counters on each cycle
        //scriptC_average.invoke_resetCounters();

        // 1. Execute sum kernel
        scriptC_average.forEach_addRedChannel(inputAllocation);

        // 2. Execute a kernel that copies the sum into an output allocation
        scriptC_average.forEach_getTotalSum(sumAllocation);

        int sumArray[] = new int[1];
        sumAllocation.copyTo(sumArray);

        // E.g. simple output can be 66
        Log.d("AverageExample", String.format("The average of red channel is %d", sumArray[0]));
        return bmp;
    }
}