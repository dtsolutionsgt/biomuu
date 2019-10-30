package com.dts.uubio.uu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;
import com.dts.uubio.uusample.Globals;
import com.dts.uubio.uusample.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class Test extends PBase {

    private TextView m_selectedDevice,lblRslt;
    private ImageView img1;
    private Button btnCap,btnVer;

    private Reader m_reader = null;
    private Engine m_engine = null;
    private Fmd m_fmd = null,m_fmdt = null;
    private Reader.CaptureResult cap_result = null;

    private Bitmap m_bitmap = null, m_bitmap2 = null;

    private String m_textString,m_text;
    private String m_deviceName = "",m_enginError;
    private int m_score = -1,m_DPI = 0;
    private boolean m_reset = false,m_first = true,m_resultAvailableToDisplay = false;
    private boolean captured=false,retflag,match;
    private double fscore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        super.InitBase();

        m_selectedDevice = (TextView) findViewById(R.id.textView2);
        lblRslt = (TextView) findViewById(R.id.textView3);lblRslt.setText("");
        img1 = (ImageView) findViewById(R.id.imageView);
        btnCap = (Button) findViewById(R.id.button3);
        btnVer = (Button) findViewById(R.id.button4);btnVer.setVisibility(View.INVISIBLE);

        initializeActivity();

        if (!initializeReader()) {
            onBackPressed();return;
        }

    }

    //region Events

    public void doCapture(View view) {
        capture();
    }

    public void doCompare(View view) {
        validate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_engine);
        initializeActivity();
    }

    //endregion

    //region Main

    private void capture() {
        retflag=false;

        new Thread(new Runnable()  {

            @Override
            public void run()  {

                try {
                    cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                } catch (Exception e) {
                    m_text=e.toString();msgtext();return;
                }

                m_resultAvailableToDisplay = false;

                if (cap_result == null || cap_result.image == null) return;

                try {
                    m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());
                    m_fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    m_bitmap.compress(Bitmap.CompressFormat.JPEG,90, outputStream);
                    byte[] imgByte =outputStream.toByteArray();

                    byte[] fmtByte=cap_result.image.getViews()[0].getImageData();
                    m_fmdt=m_engine.CreateFmd(
                            fmtByte,
                            m_bitmap.getWidth(),
                            m_bitmap.getHeight(),
                            512,0,1,Fmd.Format.ANSI_378_2004 );

                    m_text="Huella capturada";
                    retflag = true;

                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                    OutputStream outStream = null;

                    //String ffile= Environment.getExternalStorageDirectory()+"/RoadFotos/fprint.png";
                    //File file = new File(extStorageDirectory, "fprint.PNG");

                    String ffile= Environment.getExternalStorageDirectory()+"/fpuau.uuf";
                    File file = new File(ffile);

                    //outStream = new FileOutputStream(file);
                    //m_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    //outStream.flush();outStream.close();

                    try {
                        if (file.exists()) file.delete();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fmtByte);
                        fos.close();
                    } catch (Exception e) {
                        m_text="Error: " +e.toString();
                    }

                } catch (Exception e) {
                    m_text="Error: " +e.toString();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateGUI();
                        msgbox(m_text);
                        if (retflag)  {
                            btnCap.setVisibility(View.INVISIBLE);
                            btnVer.setVisibility(View.VISIBLE);
                        }
                    }
                });

                if (m_fmd==null) return;
            }

        }).start();

    }

    private void validate() {

        new Thread(new Runnable()
        {
            @Override
            public void run() {

                m_reset = false;

                while (!m_reset) {

                    try {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                    } catch (Exception e) {
                        m_text = e.toString();
                        if (!m_reset) {
                            msgtext();continue;
                        }
                    }

                    m_resultAvailableToDisplay = false;

                    if (cap_result == null || cap_result.image == null || m_fmd == null) continue;

                    try {
                        m_enginError = "";

                        m_bitmap2 = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        m_bitmap2.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                        byte[] imgByte2 =outputStream.toByteArray();

                        byte[] fmtByte=cap_result.image.getViews()[0].getImageData();

                        Fmd m_fmd2=m_engine.CreateFmd(
                                fmtByte,
                                m_bitmap2.getWidth(),
                                m_bitmap2.getHeight(),
                                512, 0, 1, Fmd.Format.ANSI_378_2004 );

                        m_score = m_engine.Compare(m_fmdt, 0, m_fmd2 , 0);
                        //m_score = m_engine.Compare(m_fmd, 0, m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004), 0);
                        m_resultAvailableToDisplay = true;
                    } catch (Exception e) {
                        m_text = e.toString(); msgtext();continue;
                    }

                    if (m_resultAvailableToDisplay) {
                        DecimalFormat formatting = new DecimalFormat("##.######");
                        //m_text = "Dissimilarity Score: " + String.valueOf(m_score) + ", False match rate: " + Double.valueOf(formatting.format((double) m_score / 0x7FFFFFFF)) + " (" + (m_score < (0x7FFFFFFF / 100000) ? "match" : "no match") + ")";

                        fscore=(double) m_score;fscore=100*fscore/0x7FFFFFFF;fscore=100-fscore;
                        if (m_score < (0x7FFFFFFF / 100000)) {
                            match=true;m_text="MATCH\n\n";
                        } else {
                            match=false;m_text="NO COINCIDE\n\n";
                        }
                        m_text += "Match rate: " +formatting.format(fscore) + " % ";

                    }

                    msgtext();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateGUI2();
                        }
                    });
                }
            }
        }).start();

    }

    private void doLoop() {
        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                m_reset = false;

                while (!m_reset) {
                    try {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                    } catch (Exception e) {
                        if(!m_reset) {
                            Log.w("UareUSampleJava", "error during capture: " + e.toString());
                            m_deviceName = "";
                            onBackPressed();
                        }
                    }

                    m_resultAvailableToDisplay = false;
                    // an error occurred
                    if (cap_result == null || cap_result.image == null) continue;

                    try {
                        m_enginError="";

                        // save bitmap image locally
                        m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                        if (m_fmd == null)	{
                            m_fmd = m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004);
                        } else {
                            m_score = m_engine.Compare(m_fmd, 0, m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004), 0);
                            m_fmd = null;
                            m_resultAvailableToDisplay = true;
                        }
                    } catch (Exception e) {
                        m_enginError = e.toString();
                        Log.w("UareUSampleJava", "Engine error: " + e.toString());
                    }

                    m_text = Globals.QualityToString(cap_result);
                    if(!m_enginError.isEmpty()) {
                        m_text = "Engine: " + m_enginError;
                    } else if (m_fmd == null) {
                        if ((!m_first) && (m_resultAvailableToDisplay)) {
                            DecimalFormat formatting = new DecimalFormat("##.######");
                            m_text = "Dissimilarity Score: " + String.valueOf(m_score)+ ", False match rate: " + Double.valueOf(formatting.format((double)m_score/0x7FFFFFFF)) + " (" + (m_score < (0x7FFFFFFF/100000) ? "match" : "no match") + ")";
                        }

                        m_textString = "Place any finger on the reader";
                    } else {
                        m_first = false;
                        m_textString = "Place the same or a different finger on the reader";
                    }

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            UpdateGUI();
                        }
                    });
                }
            }
        }).start();

    }

    //endregion

    //region Aux

    private void initializeActivity() {

        m_enginError = "";

        m_deviceName =  gl.devicename;
        m_selectedDevice.setText("Device: " + m_deviceName);

        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);

        UpdateGUI();


    }

    private boolean initializeReader() {
        try {
            Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
            Context applContext = getApplicationContext();

            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
            //m_reader.Open(Reader.Priority.EXCLUSIVE);
            m_reader.Open(Reader.Priority.COOPERATIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();
            return true;
        } catch (Exception e) {
            m_deviceName = "";
            toastlong("No se logro conectar con lector : "+e.getMessage());
            return false;
        }
    }

    public void UpdateGUI() {
        img1.setImageBitmap(m_bitmap);
        img1.invalidate();
    }

    public void UpdateGUI2() {
        img1.setImageBitmap(m_bitmap2);
        img1.invalidate();
    }

    private void msgtext() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblRslt.setText(m_text);
            }
        });
    }

    //endregion

    //region Dialogs


    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        try {
            m_reset = true;
            try { m_reader.CancelCapture(); } catch (Exception e) {}
            m_reader.Close();
        } 	catch (Exception e) {
        }

        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    //endregion

}
