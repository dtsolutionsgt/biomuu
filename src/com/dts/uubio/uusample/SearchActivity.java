package com.dts.uubio.uusample;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Engine.PreEnrollmentFmd;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Fmd.Format;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.UareUGlobal;
import com.dts.uubio.base.MiscUtils;
import com.dts.uubio.uu.PBase;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;

public class SearchActivity extends PBase {

    private ImageView m_imgView;
    private TextView m_text;
    private TextView m_text_conclusion;
    private TextView m_title;
    private Button m_back;

    private MiscUtils mu;

    private Engine m_engine = null;
    private Fmd m_enrollment_fmd = null;
    private SearchActivity.EnrollmentCallback enrollThread = null;
    private Reader.CaptureResult cap_result = null;

    private Reader m_reader = null;
    private Bitmap m_bitmap = null;

    private byte[] fmtByte;

    private String m_deviceName = "",m_enginError;
    private String m_textString, m_text_conclusionString;
    private int m_DPI = 0,m_current_fmds_count = 0;
    private boolean m_reset = false, m_first = true;
    private boolean m_success = false, exitflag=false;

    private String fname;

    final int REQUEST_CODE=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        super.InitBase();

        m_imgView = (ImageView) findViewById(R.id.bitmap_image);
        m_back = (Button) findViewById(R.id.back);
        m_text = (TextView) findViewById(R.id.text);
        m_text_conclusion = (TextView) findViewById(R.id.text_conclusion);
        m_title = (TextView) findViewById(R.id.textView8);

        mu=new MiscUtils(this);

        initializeActivity();

        fname=Environment.getExternalStorageDirectory()+ "/fpuaudata/"+gl.param1+".uud";

        // initiliaze dp sdk
        try {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
            m_reader.Open(Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();
        } catch (Exception e) {
            Log.w("UareUSampleJava", "error during init of reader");
            m_deviceName = "";
            onBackPressed();
            return;
        }

        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable() 	{
            @Override
            public void run() {
                try {
                    m_current_fmds_count = 0;
                    m_reset = false;
                    enrollThread = new SearchActivity.EnrollmentCallback(m_reader, m_engine);

                    while (!m_reset) {
                        try {
                            m_enrollment_fmd = m_engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, enrollThread);
                            if (m_success = (m_enrollment_fmd != null)) {
                                m_current_fmds_count = 0;	// reset count on success
                            }
                        } catch (Exception e) {
                            m_current_fmds_count = 0;
                        }
                    }

                    if (exitflag)  beginIdentification();

                } catch (Exception e) {
                    if(!m_reset) {
                        Log.w("UareUSampleJava", "Error de captura");
                        //m_deviceName = "";
                        onBackPressed();
                    }
                }
            }
        }).start();
    }

    //region Events

    public void onBackClick(View v) {
        onBackPressed();
    }

    //endregion

    //region Main

    public class EnrollmentCallback extends Thread 	implements Engine.EnrollmentCallback {
        public int m_current_index = 0;

        private Reader m_reader = null;
        private Engine m_engine = null;

        public EnrollmentCallback(Reader reader, Engine engine) {
            m_reader = reader;
            m_engine = engine;
        }

        // callback function is called by dp sdk to retrieve fmds until a null is returned
        @Override
        public PreEnrollmentFmd GetFmd(Format format) {
            PreEnrollmentFmd prefmd = null;
            PreEnrollmentFmd result = null;
            boolean quality=false;

            while (!m_reset) {
                try	{

                    m_text_conclusionString = "LISTO";
                    runOnUiThread(new Runnable(){
                        @Override public void run()  {
                            UpdateGUI();
                        }
                    });

                    cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                } catch (Exception e) {
                    Log.w("UareUSampleJava", "Error de captura : " + e.toString());
                    //m_deviceName = "";
                    onBackPressed();
                }

                m_text_conclusionString = "Procesando ...";
                runOnUiThread(new Runnable(){
                    @Override public void run()  {
                        UpdateGUI();
                    }
                });

                // an error occurred
                if (cap_result == null || cap_result.image == null) continue;

                try {
                    m_enginError = "";
                    // save bitmap image locally
                    m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                    fmtByte=cap_result.image.getViews()[0].getImageData();

                    prefmd = new Engine.PreEnrollmentFmd();
                    prefmd.fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                    prefmd.view_index = 0;
                    m_current_fmds_count++;

                    result =  prefmd;
                    break;
                } catch (Exception e) {
                    m_enginError = e.toString();
                    Log.w("UareUSampleJava", "Engine error: " + e.toString());
                }
            }

            m_text_conclusionString = Globals.QualityToString(cap_result);

            if(cap_result.quality==Reader.CaptureQuality.GOOD) {
                m_text_conclusionString="Lectura "+m_current_fmds_count;
                quality=true;
            }

            if(!m_enginError.isEmpty()) {
                m_text_conclusionString = "Error de captura : " + m_enginError;
            }

            if (prefmd.fmd != null && quality ) {
                m_textString = "Procesando ...";
                exitflag=true;
             } else 	{
                m_first = false;
                m_success = false;
                //m_imgView.setVisibility(View.INVISIBLE);
                //m_textString = "Coloque el MISMO dedo al lector";
            }


            runOnUiThread(new Runnable(){
                @Override public void run()  {
                    UpdateGUI();
                }
            });

            return result;
        }
    }

    private void beginIdentification() {

        try {

            m_text_conclusion.setText("Capturado");

            m_textString = "Coloque el dedo al lector";

            runOnUiThread(new Runnable(){
                @Override public void run()  {
                    UpdateGUI();
                }
            });

            //m_reset=true;
            //SystemClock.sleep(1200);
            //moveTaskToBack(true);
            //onBackPressed();
        } catch (Exception e) {
            m_text.setText("Error: ");
            m_text_conclusion.setText(e.toString());
        }
    }

    //endregion

    //region Aux

    private void initializeActivity() {

        m_title.setText("IDENTIFICACIÓN");
        m_textString = "Coloque el dedo al lector";
        m_enginError = "";
        m_deviceName = getIntent().getExtras().getString("device_name");

        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        m_imgView.setImageBitmap(m_bitmap);

        UpdateGUI();

        Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
    }

    public void UpdateGUI() {
        m_imgView.setVisibility(View.VISIBLE);
        m_imgView.setImageBitmap(m_bitmap);
        m_imgView.invalidate();
        m_text_conclusion.setText(m_text_conclusionString);
        m_text.setText(m_textString);
    }

    private void signEnroll() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/biomuu_erl.txt");
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(gl.param1.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region Dialogs


    //endregion

    //region Activity Events

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_engine);
        initializeActivity();
    }

    @Override
    public void onBackPressed() 	{
        try {
            m_reset = true;
            try {m_reader.CancelCapture(); } catch (Exception e) {}
            m_reader.Close();
        } catch (Exception e) {
            Log.w("UareUSampleJava", "Error de desactivación de lector");
        }

        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);

        finish();


    }

    //endregion


}
