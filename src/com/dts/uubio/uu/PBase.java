package com.dts.uubio.uu;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dts.uubio.base.DateUtils;
import com.dts.uubio.base.MiscUtils;
import com.dts.uubio.base.appGlobals;
import com.dts.uubio.base.clsClasses;
import com.dts.uubio.uusample.R;

public class PBase extends Activity {

    public appGlobals gl;
    public MiscUtils mu;
    public DateUtils du;
    protected Application vApp;
    protected clsClasses clsCls = new clsClasses();

    protected int itemid,browse,mode;
    protected int selid,selidx,deposito;
    protected long fecha;
    protected String s,ss;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbase);
    }

    public void InitBase(){

        vApp=this.getApplication();
        gl=((appGlobals) this.getApplication());

        mu=new MiscUtils(this);
        du=new DateUtils();

        browse=0;
    }

    // Web service call back

    protected void wsCallBack(Boolean throwing,String errmsg) throws Exception {
        if (throwing) throw new Exception(errmsg);
    }

    // Aux

    protected void msgbox(String msg){
        mu.msgbox(msg);
    }

    protected void toast(String msg) {
        toastcent(msg,48);
    }

    protected void toastbig(String msg) {
        toastcent(msg,72);
    }

    protected void toast(double val) {
        toastcent(""+val,48);
    }

    protected void toastlong(String msg) {
        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);

        RelativeLayout toastLayout = (RelativeLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(64);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void toastcent(String msg,int fsize) {

        if (mu.emptystr(msg)) return;

        try {
            Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);

            //RelativeLayout toastLayout = (RelativeLayout) toast.getView();
            //TextView toastTV = (TextView) toastLayout.getChildAt(0);
            //toastTV.setTextSize(fsize);

            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String iif(boolean condition,String valtrue,String valfalse) {
        if (condition) return valtrue;else return valfalse;
    }

    protected double iif(boolean condition,double valtrue,double valfalse) {
        if (condition) return valtrue;else return valfalse;
    }

    protected double iif(boolean condition,int valtrue,int valfalse) {
        if (condition) return valtrue;else return valfalse;
    }


    // Activity Events

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
