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

import java.io.File;

public class UareUSampleJava extends PBase
{
	private final int GENERAL_ACTIVITY_RESULT = 1;

	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
	
	private TextView m_selectedDevice;
	private Button m_getReader;
	//private Button m_getCapabilities;
	private Button m_captureFingerprint;
	//private Button m_streamImage;
	private Button m_enrollment;
	private Button m_verification;
	//private Button m_identification;
	private String m_deviceName = "";
	private String m_versionName = "";

	private final String tag = "UareUSampleJava";

	Reader m_reader;

    //region Activity Events

    @Override
	public void onCreate(Bundle savedInstanceState)	{
		//enable tracing
		System.setProperty("DPTRACE_ON", "1");
		//System.setProperty("DPTRACE_VERBOSITY", "10");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		super.InitBase();

        Bundle bundle = getIntent().getExtras();
        processBundle(bundle);

		m_getReader = (Button) findViewById(R.id.get_reader);
		//m_getCapabilities = (Button) findViewById(R.id.get_capabilities);
		m_captureFingerprint = (Button) findViewById(R.id.capture_fingerprint);
		//m_streamImage = (Button) findViewById(R.id.stream_image);
		m_enrollment = (Button) findViewById(R.id.enrollment);
		m_verification = (Button) findViewById(R.id.verification);
		//m_identification = (Button) findViewById(R.id.identification);
		m_selectedDevice = (TextView) findViewById(R.id.selected_device);

		setButtonsEnabled(false);

		// register handler for UI elements
		m_getReader.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                launchGetReader();
			}
		});

		/*
		m_getCapabilities.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)	{
				launchGetCapabilities();
			}
		});
        */
		m_captureFingerprint.setOnClickListener(new View.OnClickListener()	{
			public void onClick(View v)	{
			    msgAskRestart("Reiniciar la aplicación");
				//launchCaptureFingerprint();
			}
		});
        /*
		m_streamImage.setOnClickListener(new View.OnClickListener()	{
			public void onClick(View v) {
				launchStreamImage();
			}
		});
        */
		m_enrollment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				launchEnrollment();
			}
		});

		m_verification.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)	{
				//launchVerification();
                launchSearch();
			}
		});

		/*
		m_identification.setOnClickListener(new View.OnClickListener()	{
			public void onClick(View v)	{
				launchIdentification();
			}
		});
  	   */

		// get the version name
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



	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            displayReaderNotFound();
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

                        {
                            PendingIntent mPermissionIntent;
                            mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                            applContext.registerReceiver(mUsbReceiver, filter);

                            if(DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName))
                            {
                                CheckDevice();
                            }
                        }
                    } catch (UareUException e1)  {
                        displayReaderNotFound();
                    } catch (DPFPDDUsbException e)  {
                        displayReaderNotFound();
                    }

                } else {
                    displayReaderNotFound();
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
            gl.method=b.getString("method");
            gl.param1=b.getString("param1");
            gl.param2=b.getString("param2");

            if (gl.method.equalsIgnoreCase("1")) launchEnrollment();
        } catch (Exception e) {
            gl.method="";gl.param1="";gl.param2="";gl.param3="";
        }

        //toast("M : "+gl.method+" P:"+param);
    }

    protected void launchGetReader() {
		Intent i = new Intent(UareUSampleJava.this, GetReaderActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	protected void launchGetCapabilities() 	{
		Intent i = new Intent(UareUSampleJava.this,GetCapabilitiesActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	protected void launchCaptureFingerprint() 	{
		Intent i = new Intent(UareUSampleJava.this,CaptureFingerprintActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	protected void launchStreamImage() 	{
		Intent i = new Intent(UareUSampleJava.this, StreamImageActivity.class);
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
            startActivityForResult(i, 1);
        }
 	}

    protected void launchSearch() 	{
        gl.modoid=true;
        Intent i = new Intent(UareUSampleJava.this, EnrollmentActivity.class);
        i.putExtra("device_name", m_deviceName);
        startActivityForResult(i, 1);
     }

	protected void launchVerification() {
		Intent i = new Intent(UareUSampleJava.this, VerificationActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	protected void launchIdentification() {
		Intent i = new Intent(UareUSampleJava.this,IdentificationActivity.class);
		i.putExtra("device_name", m_deviceName);
		startActivityForResult(i, 1);
	}

	//endregion

	//region Aux

	protected void setButtonsEnabled(Boolean enabled) {
		//m_getCapabilities.setEnabled(enabled);
		//m_streamImage.setEnabled(enabled);
		m_captureFingerprint.setEnabled(enabled);
		m_enrollment.setEnabled(enabled);
		m_verification.setEnabled(enabled);
		//m_identification.setEnabled(enabled);
	}

	protected void setButtonsEnabled_Capture(Boolean enabled) {
		m_captureFingerprint.setEnabled(enabled);
		m_enrollment.setEnabled(enabled);
		m_verification.setEnabled(enabled);
		//m_identification.setEnabled(enabled);
	}

	protected void setButtonsEnabled_Stream(Boolean enabled) {
		//m_streamImage.setEnabled(enabled);
	}

	protected void CheckDevice() {
		try {
			m_reader.Open(Priority.EXCLUSIVE);
			Reader.Capabilities cap = m_reader.GetCapabilities();
			setButtonsEnabled(true);
			setButtonsEnabled_Capture(cap.can_capture);
			setButtonsEnabled_Stream(cap.can_stream);
			m_reader.Close();
		} catch (UareUException e1) {
			displayReaderNotFound();
		}
	}

    private void displayReaderNotFound() {
        m_selectedDevice.setText("Dispositivo : (Sin lector)");
        setButtonsEnabled(false);

        //toast("Lector no encontrado");
        launchGetReader();

        /*
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Lector no encontrado");
        alertDialogBuilder.setMessage("Conecte el lector e intente de nuevo.").setCancelable(false).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        launchGetReader();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        if(!isFinishing()) {
            alertDialog.show();
        }

         */
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

    //endregion

    //region Activity Events

    @Override
    protected void onResume() {
        super.onResume();

        try {
            //toast("Resume :"+gl.devicename);
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
        m_selectedDevice.setText("Device: (No Reader Selected)");
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
