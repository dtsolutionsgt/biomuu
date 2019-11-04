/* 
 * File: 		UareUSampleJava.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

package com.dts.uubio.uusample;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.dts.uubio.uu.PBase;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.content.Context;
import android.app.PendingIntent; 
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class UareUSampleJava extends PBase {
	private final int GENERAL_ACTIVITY_RESULT = 1;

	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
	
	private TextView m_selectedDevice;
	private Button m_getReader;
	private Button m_captureFingerprint;
	private Button m_enrollment;
	private Button m_verification;
	private String m_deviceName = "";
	private String m_versionName = "";
	private final String tag = "UareUSampleJava";

	private Reader m_reader;
	private boolean pendingbundle=false,pendingreader=true;

    //region Activity Events

    @Override
	public void onCreate(Bundle savedInstanceState)	{
		//enable tracing
		System.setProperty("DPTRACE_ON", "1");
		//System.setProperty("DPTRACE_VERBOSITY", "10");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		super.InitBase();

		gl.sdkready=false;

		m_getReader = (Button) findViewById(R.id.get_reader);
		m_captureFingerprint = (Button) findViewById(R.id.capture_fingerprint);
		m_enrollment = (Button) findViewById(R.id.enrollment);
		m_verification = (Button) findViewById(R.id.verification);
		m_selectedDevice = (TextView) findViewById(R.id.selected_device);

		setButtonsEnabled(false);

		m_getReader.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                launchGetReader();
			}
		});

		m_captureFingerprint.setOnClickListener(new View.OnClickListener()	{
			public void onClick(View v)	{
			    msgAskRestart("Reiniciar la aplicación");
			}
		});

		m_enrollment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchEnrollment();
			}
		});

		m_verification.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)	{
	             launchSearch();
			}
		});

		try	{
			m_versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(tag, e.getMessage());
		}

		ActionBar ab = getActionBar();
		ab.setTitle("DTSolutions Biometrico DPUaU");

         try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/fpuaudata");
            directory.mkdirs();
        } catch (Exception e) {}

        Bundle bundle = getIntent().getExtras();
        processBundle(bundle);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null) {
            reinitReader();
            return;
        }

        Globals.ClearLastBitmap();
        m_deviceName = (String) data.getExtras().get("device_name");
        gl.devicename=m_deviceName;

        switch (requestCode)   {
            case GENERAL_ACTIVITY_RESULT:

                if((m_deviceName != null) && !m_deviceName.isEmpty()) {
                    m_selectedDevice.setText("Device: " + m_deviceName);
                    gl.devicename=m_deviceName;

                    try {
                        Context applContext = getApplicationContext();
                        m_reader = Globals.getInstance().getReader(m_deviceName, applContext);

                        PendingIntent mPermissionIntent;
                        mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                        applContext.registerReceiver(mUsbReceiver, filter);

                        if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName)) {
                            CheckDevice();
                        }
                    } catch (Exception e)  {
                        reinitReader();return;
                    }
                } else {
                    reinitReader();return;
                }

                pendingreader=false;

                if (pendingbundle) {
                    pendingbundle=false;

                    toast("Bundle recall");

                    if (gl.method.equalsIgnoreCase("1")) launchEnrollment();
                    if (gl.method.equalsIgnoreCase("2")) launchSearch();

                    pendingbundle=!gl.sdkready;
                    if (pendingbundle) reinitReader();
                }

                break;
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()  {
        public void onReceive(Context context, Intent intent)  {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) CheckDevice();
                    } else {
                        setButtonsEnabled(false);
                    }
                }
            }

        }
    };

	//endregion

    //region Events

    public void doTest(View view) {
        startActivity(new Intent(this,com.dts.uubio.uu.Test.class));
    }

    public void doCompare(View view) {
        startActivity(new Intent(this,com.dts.uubio.uu.Compare.class));
    }

    public void doSendResult(View view) {
        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);

        moveTaskToBack(true);
    }

    //endregion

    //region Main

    private void processBundle(Bundle b) {
        try {

            pendingbundle=true;

            gl.method=b.getString("method");
            gl.param1=b.getString("param1");
            gl.param2=b.getString("param2");

            if (gl.method.equalsIgnoreCase("1")) launchEnrollment();
            if (gl.method.equalsIgnoreCase("2")) launchSearch();

            pendingbundle=!gl.sdkready;
            if (pendingbundle) reinitReader();

        } catch (Exception e) {
            gl.method="";gl.param1="";gl.param2="";gl.param3="";
        }

     }

    protected void launchGetReader() {
		Intent i = new Intent(UareUSampleJava.this, GetReaderActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	protected void launchEnrollment() 	{

        if (gl.param1.isEmpty()) {
            displayNoID();
        } else {
            gl.modoid=false;
            Intent i = new Intent(UareUSampleJava.this, EnrollmentActivity.class);
            i.putExtra("device_name", m_deviceName);
            startActivity(i);
        }
 	}

    protected void launchSearch() 	{
        gl.modoid=true;

        Intent i = new Intent(UareUSampleJava.this, EnrollmentActivity.class);
        i.putExtra("device_name", m_deviceName);
        startActivityForResult(i, 1);
     }

	//endregion

	//region Aux

	protected void setButtonsEnabled(Boolean enabled) {
        m_captureFingerprint.setEnabled(enabled);
		m_enrollment.setEnabled(enabled);
		m_verification.setEnabled(enabled);
	}

	protected void setButtonsEnabled_Capture(Boolean enabled) {
		m_captureFingerprint.setEnabled(enabled);
		m_enrollment.setEnabled(enabled);
		m_verification.setEnabled(enabled);
    }

	protected void CheckDevice() {
		try {
			m_reader.Open(Priority.EXCLUSIVE);
			Reader.Capabilities cap = m_reader.GetCapabilities();
			setButtonsEnabled(true);
			setButtonsEnabled_Capture(cap.can_capture);
			m_reader.Close();
		} catch (UareUException e1) {
			reinitReader();
		}
	}

    private void reinitReader() {
        pendingreader=true;

        try {
            m_selectedDevice.setText("Dispositivo : (Sin lector)");
            setButtonsEnabled(false);
        } catch (Exception e) {
        }

        //toast("Lector no encontrado");
        launchGetReader();

    }

    private void displayNoID() {
        setButtonsEnabled(false);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Identificacion incorrecta");
        alertDialogBuilder.setMessage("No se puede enrollar sin identificación de persona.").setCancelable(false).setPositiveButton("Ok",
                new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog,int id) {}
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        if(!isFinishing()) {
            alertDialog.show();
        }
    }

    private void msgAskRestart(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Reinicio");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                restartApp();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

    private void restartApp(){
        try{
            PackageManager packageManager = this.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent =Intent.makeRestartActivityTask(componentName);
            //Intent mainIntent = IntentCompat..makeRestartActivityTask(componentName);
            this.startActivity(mainIntent);
            System.exit(0);
        }catch (Exception e){
        }

    }

    protected void toast(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void dofillfp(View view) {
        Fmd fmdt=null;
        File file2;
        int size2,m_score = -1;
        byte[] fmtByte2;
        boolean rslt=false;
        Engine m_engine = null;

        try {

            m_engine = UareUGlobal.GetEngine();

            file2 = new File(Environment.getExternalStorageDirectory()+ "/fpuaudata/2329.uud");
            size2 = (int) file2.length();
            fmtByte2 = new byte[size2];

            BufferedInputStream buf2 = new BufferedInputStream(new FileInputStream(file2));
            buf2.read(fmtByte2, 0, fmtByte2.length);
            buf2.close();

            fmdt = m_engine.CreateFmd(fmtByte2,320,360,512, 0, 1, Fmd.Format.ANSI_378_2004);

            gl.fprints.add(fmdt);

            //toast("added :"+gl.fprints.size());
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }

    //endregion

    //region Activity Events

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (gl.devicename.isEmpty()) {
                setButtonsEnabled(false);
            } else {
                setButtonsEnabled(true);
            }
        } catch (Exception e) {
            setButtonsEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        // reset you to initial state when activity stops
        m_selectedDevice.setText("Dispositivo: (No Reader Selected)");
        setButtonsEnabled(false);
        //toast("Stop :"+gl.devicename);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //endregion

}
