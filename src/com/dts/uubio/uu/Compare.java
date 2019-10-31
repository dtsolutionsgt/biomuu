package com.dts.uubio.uu;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.dts.uubio.uusample.Globals;
import com.dts.uubio.uusample.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


public class Compare extends PBase {

    private TextView lblProg,lblRes;
    private ImageView img1,img2;

    private Reader m_reader = null;
    private Engine m_engine = null;
    private Fmd fmd=null,fmdt=null;
    private Reader.CaptureResult cap_result = null;
    private int m_score = -1,m_DPI = 0;
    private String m_deviceName = "",m_enginError;

    private Bitmap bm,bmt;

    private String rootfold,fpfold,msg;
    private int match,nomatch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        super.InitBase();

        lblProg = (TextView) findViewById(R.id.textView4);lblProg.setText("");
        lblRes  = (TextView) findViewById(R.id.textView5);lblRes.setText("");
        img1 = (ImageView) findViewById(R.id.imageView2);
        img2 = (ImageView) findViewById(R.id.imageView3);

        initializeReader();

        initSearch();
    }

    //region Events

    public void doCompare(View view) {
        compare();
    }

    //endregion

    //region Main

    private void compare() {
        File file2;
        int size2;
        byte[] fmtByte2;

        match=0;nomatch=0;

        //file2 = new File(fpfold + "f00" + i + ".uuf");
        file2 = new File(fpfold + "2329.uud");
        size2 = (int) file2.length();
        fmtByte2 = new byte[size2];
        try {
            BufferedInputStream buf2 = new BufferedInputStream(new FileInputStream(file2));
            buf2.read(fmtByte2, 0, fmtByte2.length);
            buf2.close();
        } catch (Exception e) {
        }

        try {
            fmdt = m_engine.CreateFmd(
                    fmtByte2,
                    320,
                    360,
                    512, 0, 1, Fmd.Format.ANSI_378_2004);
        } catch (UareUException e) {
            e.printStackTrace();
        }


        for (int j = 1; j <15; j++) {

            for (int i = 1; i < 6; i++) {
                msg = "Comparando : " + i;
                showtext();

                //try {

                    try {
                        m_score = m_engine.Compare(fmd, 0, fmdt, 0);
                        if (m_score < (0x7FFFFFFF / 100000)) {
                            match++;
                        } else {
                            nomatch++;
                        }
                    } catch (UareUException e) {
                        msgbox(e.getMessage());
                        nomatch++;
                    }
/*
                } catch (UareUException e) {
                    msgbox(e.getMessage());
                    nomatch++;
                }*/

            }
        }

        lblRes.setText("Match : "+match+" , No match : "+nomatch);
    }

    private void initSearch() {
        rootfold=Environment.getExternalStorageDirectory()+"/";
        fpfold= Environment.getExternalStorageDirectory()+ "/fpuaudata/";

        try {

            File file = new File(fpfold + "2329.uud");
            int size = (int) file.length();
            byte[] fmtByte = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(fmtByte, 0, fmtByte.length);
                buf.close();
            } catch (Exception e) {
            }

            fmd=m_engine.CreateFmd(
                    fmtByte,
                    320,
                    360,
                    512,0,1,Fmd.Format.ANSI_378_2004 );


        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Aux

    private boolean initializeReader() {
        try {
            Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
            Context applContext = getApplicationContext();

            m_deviceName =  gl.devicename;

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

    private void showtext() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblProg.setText(msg);lblProg.invalidate();
            }
        });
    }

    //endregion

    //region Dialogs


    //endregion

    //region Activity Events


    //endregion


}
