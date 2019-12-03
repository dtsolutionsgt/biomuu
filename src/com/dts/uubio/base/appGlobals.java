package com.dts.uubio.base;

import android.app.Application;

import com.digitalpersona.uareu.Fmd;
import java.util.ArrayList;

public class appGlobals extends Application {

    public String devicename,method="",param1 ="",param2="",param3="";
    public boolean modoid = false,sdkready,idle=false;
    public ArrayList<Fmd> fprints = new ArrayList<Fmd>();

}
