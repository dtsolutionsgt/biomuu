/* 
 * File: 		EnrollmentActivity.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

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
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EnrollmentActivity extends PBase {

    private ImageView m_imgView;
    private TextView m_text;
    private TextView m_text_conclusion;
    private TextView m_text_prog;
    private TextView m_title,m_fprints;
	private Button m_back;

    private MiscUtils mu;

    private Engine m_engine = null;
    private Fmd m_enrollment_fmd = null;
    private EnrollmentCallback enrollThread = null;
    private Reader.CaptureResult cap_result = null;
    private PreEnrollmentFmd prefmd=null;
    private Fmd fmd=null;

    private Reader m_reader = null;
    private Bitmap m_bitmap = null;

    private byte[] fmtByte;
    private ArrayList<String> fprint = new ArrayList<String>();

    private String m_deviceName = "",m_enginError;
    private String m_textString, m_text_conclusionString,m_textprog;
	private int m_DPI = 0,m_current_fmds_count = 0;
	private boolean m_reset = false, m_first = true;
	private boolean m_success = false, exitflag=false;

	private String fname,fpfold;
    private boolean modo,match,callflag=false;
    private int spos,matchid;
    private String scode,matchcode,ss,fn,fnn;

    final int REQUEST_CODE=101;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine);

        super.InitBase();

        m_imgView = (ImageView) findViewById(R.id.bitmap_image);
        m_back = (Button) findViewById(R.id.back);
        m_text = (TextView) findViewById(R.id.text);
        m_text_conclusion = (TextView) findViewById(R.id.text_conclusion);
        m_text_prog = (TextView) findViewById(R.id.text_conclusion2);
        m_title = (TextView) findViewById(R.id.textView8);
        m_fprints = (TextView) findViewById(R.id.textView11);

        mu=new MiscUtils(this);

        modo=gl.modoid;

        initializeActivity();

        fpfold= Environment.getExternalStorageDirectory()+ "/fpuaudata/";
        fname=Environment.getExternalStorageDirectory()+ "/fpuaudata/"+gl.param1+".uud";

        if (!initializeSDK()) {
            m_deviceName = "";
            callflag = true;
            onBackPressed();
            return;
        };

        //getFPList();

        m_bitmap= null;
        m_fprints.setText("Huellas : "+gl.fprints.size());
        UpdateGUI();

        if (modo) {
            beginIdentification();
           } else  {
            beginEnrollment();
        }

    }

    //region Events

    public void onBackClick(View v) {
        callflag=true;
        moveTaskToBack(true);
        onBackPressed();
    }

    //endregion

    //region Main

    private void beginEnrollment() {
        // loop capture on a separate thread to avoid freezing the UI
        new Thread(new Runnable() 	{
            @Override
            public void run() {
                try {
                    m_current_fmds_count = 0;
                    m_reset = false;
                    enrollThread = new EnrollmentCallback(m_reader, m_engine);

                    while (!m_reset) {

                        try {
                            m_enrollment_fmd = m_engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, enrollThread);
                            if (m_success = (m_enrollment_fmd != null)) {
                                   // m_current_fmds_count = 0;	// reset count on success
                            }
                        } catch (Exception e) {
                            m_current_fmds_count = 0;
                        }
                    }

                    if (exitflag)  completeEnrollment();

                } catch (Exception e) {
                    if(!m_reset) {
                        Log.w("UareUSampleJava", "Error de captura");
                        //m_deviceName = "";
                        callflag=true;
                        onBackPressed();
                    }
                }
            }
        }).start();

    }

    private void beginIdentification() {

        new Thread(new Runnable()         {
            @Override
            public void run() {

                m_reset = false;

                while (!m_reset) {

                    try {
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                    } catch (Exception e) {
                        m_textString = e.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateGUI();
                            }
                        });
                        if (!m_reset) continue;
                    }

                    if (cap_result == null || cap_result.image == null) continue;

                    try {
                        m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                        byte[] fmtByte=cap_result.image.getViews()[0].getImageData();

                        fmd=m_engine.CreateFmd(
                                fmtByte,
                                m_bitmap.getWidth(),
                                m_bitmap.getHeight(),
                                512, 0, 1, Fmd.Format.ANSI_378_2004 );

                        m_textString="Comparando ";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateGUI();
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Compare();
                            }
                        });

                        m_reset=true;

                    } catch (Exception e) {
                        m_textString = e.toString(); continue;
                    }
                }
            }
        }).start();

    }

    private void initializeActivity() {

        if (gl.modoid) {
            m_title.setText("IDENTIFICACIÓN");
        } else {
            m_title.setText("Enrolamiento : "+ gl.param2);
        }

        m_textString = "Coloque el dedo al lector";
        m_enginError = "";m_textprog="";
        m_deviceName = getIntent().getExtras().getString("device_name");

        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null) m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        m_imgView.setImageBitmap(m_bitmap);

        UpdateGUI();

        Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
    }

    @Override
    protected void onResume()  {
        super.onResume();

        try     {
            m_imgView.setImageBitmap(null);
        }catch (Exception e) {
         Log.e("Error mae",e.getMessage());
        }
    }

    private boolean initializeSDK()  {
        try  {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(gl.devicename, applContext);
            m_reader.Open(Priority.COOPERATIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
            m_engine = UareUGlobal.GetEngine();

            gl.sdkready=true;
            return true;
        } catch (Exception e) {
            //toastlong("Error inicializar SDK : "+e.getMessage());
            gl.sdkready=false;
            return false;
        }
    }

    //endregion

    //region Enrollment

    public class EnrollmentCallback extends Thread 	implements Engine.EnrollmentCallback   {
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

            PreEnrollmentFmd result = null;

            while (!m_reset) {

                try   {

                    m_text_conclusionString = "LISTO PARA LECTURA " + (m_current_fmds_count + 1);

                    runOnUiThread(new Runnable(){
                        @Override public void run()  {
                            UpdateGUI();
                        }
                    });

                    cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);

                } catch (Exception e){
                    callflag=true;
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

                try  {

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

                } catch (Exception e)  {
                    m_enginError = e.toString();
                }
            }

            m_text_conclusionString = Globals.QualityToString(cap_result);

            if(cap_result.quality==Reader.CaptureQuality.GOOD) {
                m_text_conclusionString="Lectura "+m_current_fmds_count;
            }

            if(!m_enginError.isEmpty()) {
                m_text_conclusionString = "Error de captura : " + m_enginError;
            }

            if (m_enrollment_fmd != null || m_current_fmds_count == 0) {
                if (!m_first)
                {
                    m_text_conclusionString = m_success ? "Enrolamiento completo ": "Enrolamiento falló. Intente de nuevo";
                }

                if (m_success)
                {
                    m_textString = "Completo";
                    exitflag = true;
                    m_reset = true;
                    gl.param1 = "";
                    gl.param2 = "";
                    gl.param3 = "";
                } else {
                    m_textString = "Coloque dedo en lector";
                    m_enrollment_fmd = null;
                }
            } else  {
                m_first = false;
                m_success = false;
                m_textString = "Coloque el MISMO dedo en lector";
            }

            runOnUiThread(new Runnable(){
                @Override public void run()  {
                    UpdateGUI();
                }
            });

            return result;
        }
    }

    private void completeEnrollment()  {

        try {

            if (!fprintExists())  {

                File file = new File(fname);
                if (file.exists()) file.delete();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(fmtByte);
                fos.close();

                signEnroll();

                gl.param1 = "";
                gl.param2 = "";
                gl.param3 = "";

                SystemClock.sleep(1200);

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast= Toast.makeText(getApplicationContext(),
                                "LA HUELLA YA EXISTE , CODIGO : "+fnn, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);toast.show();
                    }
                });
            }

            moveTaskToBack(true);
            callflag=true;
            onBackPressed();

        } catch (Exception e)
        {
            m_text.setText("Error: ");
            m_text_conclusion.setText(e.toString());
        }
    }

    private void signEnroll()  {
        try  {
            File file = new File(Environment.getExternalStorageDirectory() + "/biomuu_erl.txt");
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(gl.param1.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean fprintExists()  {

        match=false;spos=-1;
        scode="";matchcode="";

        fmd=prefmd.fmd;

        for (int i = 0; i <fprint.size(); i++)  {

            try {

                fn=fprint.get(i); fnn=fn.substring(0,fn.length()-4);

                m_textString="Verificando existencia de huella...";
                m_text_conclusionString="";
                m_textprog=(i+1)+" / "+fprint.size();

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UpdateGUI();
                    }
                });

                if (!fn.equalsIgnoreCase(gl.param1+".uud"))   {
                    match(fn);
                    if (match) return true;
                }

            } catch (Exception e)
            {
                String aa=e.getMessage();
            }

        }
        return false;
    }

    //endregion

    //region Identification

    private void Compare()  {
        String fn;
        int ii=0;

        match=false;spos=-1;
        scode="";matchcode="";

        m_text_conclusionString=".........";
        m_textprog="";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UpdateGUI();
            }
        });


        //Date time1 = Calendar.getInstance().getTime();

        matchid=-1;

        int fpc=gl.fprints.size();

        try {
            for (int i = 0; i <fpc; i++)  {
               matcharray(i);ii=i;
               if (match) {
                   toast("Huella encontrada");
                   completeMatch();return;
               }
            }
        } catch (Exception e) {
            toast("err "+e.getMessage());
        }

        //Date time2 = Calendar.getInstance().getTime();
        //long td=time2.getTime()-time1.getTime();
        //toastlong("Tiempo : "+mu.frmdecimal(((double)td)/1000,2)+" [seg] cant : "+ii);

         /*
        for (int i = 0; i < fprint.size(); i++) {
            fn = fprint.get(i);
            match(fn);
            if (match) {
                toast("Huella encontrada.");
                completeMatch();
                return;
            }
        }
        */

        try {

            toastbig("\n  HUELLA NO  \n\n ENCONTRADA \n");

            SystemClock.sleep(100);
            moveTaskToBack(true);
            callflag=true;
            onBackPressed();
        } catch (Exception e) {
            toast("err "+e.getMessage());
        }
    }

    private boolean match(String iid)     {
        Fmd fmdt=null;
        File file2;
        int size2,m_score = -1;

        byte[] fmtByte2;
        boolean rslt=false;

        try {

            file2 = new File(fpfold + iid);
            size2 = (int) file2.length();
            fmtByte2 = new byte[size2];

            BufferedInputStream buf2 = new BufferedInputStream(new FileInputStream(file2));
            buf2.read(fmtByte2, 0, fmtByte2.length);
            buf2.close();

            fmdt = m_engine.CreateFmd(fmtByte2,320,360,512, 0, 1, Fmd.Format.ANSI_378_2004);

            m_score = m_engine.Compare(fmd, 0, fmdt, 0);
            if (m_score < (0x7FFFFFFF / 100000)) rslt=true; else rslt=false;

        } catch (Exception e)  {
            msgbox(e.getMessage());rslt=false;
        }

        if (rslt) {
            match=true;matchcode=iid;
        }

        return rslt;
    }

    private boolean matcharray(int fpos)     {
        Fmd fmdt=null;
        int m_score = -1;
        boolean rslt=false;

        try {
            fmdt=gl.fprints.get(fpos);

            m_score = m_engine.Compare(fmd, 0, fmdt, 0);
            if (m_score < (0x7FFFFFFF / 100000)) rslt=true; else rslt=false;

        } catch (Exception e) {
            msgbox(e.getMessage());rslt=false;
        }

        if (rslt){
            match=true;matchid=fpos;matchcode=gl.fprintid.get(fpos);
        }

        return rslt;
    }

    private void completeMatch()  {
        String cod;

        try {
            cod=matchcode;
            cod=cod.substring(0,cod.length()-4);

            File file = new File(Environment.getExternalStorageDirectory() + "/biomuu_idf.txt");
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(cod.getBytes());
            stream.flush();
            stream.close();

            m_textString="Encontrado";m_textprog="";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateGUI();
                }
            });

            SystemClock.sleep(1000);
            moveTaskToBack(true);
            callflag=true;
            onBackPressed();
        } catch (Exception e) {
            m_text.setText("Error: ");
            m_text_conclusion.setText(e.toString());
        }
    }

    //endregion

    //region Aux

    public void UpdateGUI()  {
        m_imgView.setVisibility(View.VISIBLE);
        m_imgView.setImageBitmap(m_bitmap);
        m_imgView.invalidate();

        m_text_conclusion.setText(m_text_conclusionString);
        m_text.setText(m_textString);
        m_text_prog.setText(m_textprog);
    }

    public void UpdateTexto()  {
        m_text_conclusion.setText(m_text_conclusionString);
        m_text.setText(m_textString);
        m_text_prog.setText(m_textprog);
    }

    private void getFPList()  {
        String fn;
        ss="";

        try   {
            File folder = new File(Environment.getExternalStorageDirectory()+ "/fpuaudata");
            File[] filesInFolder = folder.listFiles();

            for (File file : filesInFolder) {
                if (!file.isDirectory()) {
                    fn=new String(file.getName());
                    fprint.add(fn);ss+=fn+"\n";
                }
            }
        } catch (Exception e) {
            msgbox(e.getMessage());
        }

    }

    //private void

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
    public void onBackPressed()
    {

        if (!callflag) return;

        try {
            m_reset = true;
            try {m_reader.CancelCapture(); } catch (Exception e) {}
            m_reader.Close();
        } catch (Exception e) {
        }

        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);

        finish();
    }

    //endregion


}
