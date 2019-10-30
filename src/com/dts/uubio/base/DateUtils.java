package com.dts.uubio.base;

import java.util.Calendar;

public class DateUtils {
	
	public DateUtils() {
	}
	
	public String sfecha(long f) {
		long vy,vm,vd;
		String s;
		
		vy=(long) f/100000000;
		f=f % 100000000;
		vm=(long) f/1000000;
		f=f % 1000000;
		vd=(long) f/10000;
		f=f % 10000;
		
		s="";
		if (vd>9) { s=s+ String.valueOf(vd)+"/";} else {s=s+"0"+ String.valueOf(vd)+"/";}
		if (vm>9) { s=s+ String.valueOf(vm)+"/20";} else {s=s+"0"+ String.valueOf(vm)+"/20";}
		if (vy>9) { s=s+ String.valueOf(vy);} else {s=s+"0"+ String.valueOf(vy);}
		
		return s;
	}
	
	public String sfechash(long f) {
		int vy,vm,vd;
		String s;
		
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		
		s="";
		if (vd>9) { s=s+ String.valueOf(vd)+"/";} else {s=s+"0"+ String.valueOf(vd)+"/";}
		if (vm>9) { s=s+ String.valueOf(vm);} else {s=s+"0"+ String.valueOf(vm);}
		
		return s;
	}
	
	public String shora(long vValue) {
		long h,m;
		String sh,sm;
			
		h=vValue % 10000;
		m=h % 100;if (m>9) {sm= String.valueOf(m);} else {sm="0"+ String.valueOf(m);}
		h=(int) h/100;if (h>9) {sh= String.valueOf(h);} else {sh="0"+ String.valueOf(h);}
			
		return sh+":"+sm;
	}
	
	
	public String geActTimeStr(){
		long f,ch,cm,cs;
		String s,ss;
		
		final Calendar c = Calendar.getInstance();
		
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs = c.get(Calendar.SECOND);
		
		s=""+ch;if (ch<10) s="0"+s;
		ss=""+cm;if (cm<10) ss="0"+ss;s=s+":"+ss;
		ss=""+cs;if (cs<10) ss="0"+ss;s=s+":"+ss;
		
		return s;
	}
	
	public String sSecond(){
		long cs;
		String sss;
		
		final Calendar c = Calendar.getInstance();
		cs=c.get(Calendar.SECOND);
				
		sss=""+cs;
		if (cs<10) sss="0"+sss;
		
		return sss;
	}
	
	public String univfecha(long f) {
		long vy,vm,vd,m,h;
		String s;
		
		//yyyyMMdd hh:mm:ss
		
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		h= (int) f/100;
		m= f % 100;
		
		s="20";
		if (vy>9) s=s+vy; else s=s+"0"+vy; 
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;  
		s=s+" ";  
		if (h>9)  s=s+h;  else s=s+"0"+h;
		s=s+":";
		if (m>9)  s=s+m;  else s=s+"0"+m;
		s=s+":00";
		
		return s;
	}

	//#CKFK 20190509 Modifiqué para quitale el 00 que le puso Henry
	public String univfechasinhora(long f) {
		int vy,vm,vd;
		String s;

		//yyyyMMdd

		vy=(int) f/10000;f=f % 10000;
		vm=(int) f/100;f=f % 100;
		vd=(int) f;

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s=vy+" "+vm+":"+vd;

		return s;
	}
	
	public String univfechaseg() {

		long f,fecha, vy,vm,vd;;
		int cyear,cmonth,cday,ch,cm, cs;
		String s;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		//yyyyMMdd hh:mm:ss
		
		vy=cyear;
		vm=cmonth;
		vd=cday;
			
		s=""+vy; 
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s = s + " ";
		if (ch>9) s=s+ch; else s=s+"0"+ch; s=s+ ":";
		if (cm>9) s=s+cm; else s=s+"0"+cm; s=s+ ":";
		if (cs>9) s=s+cs; else s=s+"0"+cs;

		return s;
	}

	public String univfechaext(long f) {
		long vy,vm,vd;
		String s;

		//yyyyMMdd hh:mm:ss

		vy=(long) f/10000;f=f % 10000;
		vm=(long) f/100;f=f % 100;
		vd=(long) f;

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s=vy+" "+vm+":"+vd+":00"; //#HS_20181128_1102 Agregue " "+vm+":"+vd+":00" para que devolviera la hora.

		return s;
	}

	public String univfechasql(long f) {
		long vy,vm,vd;
		String sy,sm,sd;

		//yyyy-MM-dd

		vy=(long) f/100000000;f=f % 100000000;
		vm=(long) f/1000000;f=f % 1000000;
		vd=(long) f/10000;f=f % 10000;

		if (vy>9) sy="20"+vy; else sy="200"+vy;
		if (vm>9) sm=""+vm; else sm="0"+vm;
		if (vd>9) sd=""+vd; else sd="0"+vd;

		return sy+sm+sd;
	}

	public String univfechaReport(long f) {
		long vy,vm,vd;
		String sy,sm,sd;

		//yyyy-MM-dd

		vy=(long) f/100000000;
		f=f % 100000000;
		vm=(long) f/1000000;
		f=f % 1000000;
		vd=(long) f/10000;
		f=f % 10000;

		if(vy<100){
			if (vy>9) sy="20"+vy; else sy="200"+vy;
		}else {
			sy = "" + vy;
		}

		if (vm>9) sm=""+vm; else sm="0"+vm;
		if (vd>9) sd=""+vd; else sd="0"+vd;

		return sd+"/"+sm+"/"+sy;
	}

	public long ffecha00(long f) {
		f=(long) f/10000;
		f=f*10000;
		return f;
	}
	
	public long ffecha24(long f) {
		f=(long) f/10000;
		f=f*10000+2359;
		return f;
	}

	//#HS_20181121_1008 Funcion para fecha sin hora.
	public long cfechaSinHora(int year,int month, int day) {
		long c;
		c=year % 100;
		c=c*10000+month*100+day;

		//return	c;
		return c; //*10000;
	}
	  
	public long cfecha(int year,int month, int day) {
		long c;
		c=year % 100;
		c=c*10000+month*100+day;

		return c*10000;
	}

	public long cfechaDesc(int year,int month, int day) {
		long c;
		String d,mes,dia,ano;
		int siglo;

		if(year<2000){
			siglo = 0;
		}else {
			siglo = 2000;
		}

		year = year - siglo;
		ano = Integer.toString(year);
		mes = Integer.toString(month);
		dia = Integer.toString(day);

		if (dia.length()<2) {
			dia = "0" + day;
		}

		if (mes.length()<2) {
			mes = "0" + month;
		}

		d= ano + mes + dia + "0000";

		c= Long.parseLong(d);

		return c;
	}
	
	public long parsedate(int date,int hour,int min) {
		long f;
		f=date+100*hour+min;
		return f;
	}
		
	public int getyear(long f) {
		int vy;
				
		vy=(int) f/100000000;f=f % 100000000;
		vy=vy+2000;
		
		return vy;
	}
	
	public int getmonth(long f) {
		int vy,vm;
				
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
				
		return vm;
	}
	
	public int getday(long f) {
		int vy,vm,vd;
				
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		
		return vd;
	}
	
	public int LastDay(int year,int month) {
		int m,y,ld;
		
		m=month % 2;
		if (m==1) {
			ld=31;
		} else {
			ld=30;
		}
		
		if (month==2) {
			ld=28;
			if (year % 4==0) {ld=29;}
		}

		return ld;
		
	}
	
	public long timeDiff(long v1,long v2) {
		long h1,m1,h2,m2,vm1,vm2,dif;

		h1=v1 % 10000;m1=h1 % 100;h1=(int) h1/100;
		vm1=h1*60+m1;
		
		h2=v2 % 10000;m2=h2 % 100;h2=(int) h2/100;
		vm2=h2*60+m2;
		
		dif=vm1-vm2;
		if (dif<0) dif=-dif;
		
		return dif;
		
	}
	
	public int dayofweek(long f) {
		int y,m,d,dw;
	     
		final Calendar c = Calendar.getInstance();
		
		c.set(getyear(f), getmonth(f)-1, getday(f));
			
	    dw=c.get(Calendar.DAY_OF_WEEK);
	    
	    if (dw==1) dw=7;else dw=dw-1;
	    
	    return dw;
	}
	
	public long getActDate(){
		long f;
		int cyear,cmonth,cday;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		
		f=cfecha(cyear,cmonth,cday);
		
		return f;
	}

	//#HS_20181121_1008 Funcion para obtener la fecha sin hora.
	public long getFechaActual(){
		long f,fecha;
		String fechaS;
		int cyear,cmonth,cday,ch,cm;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		//#HS_20181120_1725 Campo de fecha sin hora.
		f=cfechaSinHora(cyear,cmonth,cday);
		fechaS=f+"0000";
		fecha= Long.parseLong(fechaS);

		return fecha;
	}

	public long getFechaActualReport(){
		long f,fecha;
		int cyear,cmonth,cday,ch,cm;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		//#HS_20181120_1725 Campo de fecha sin hora.
		f=cfechaSinHora(cyear,cmonth,cday);
		fecha=f*10000;

		return fecha;
	}
	
	public long getActDateTime(){
		long f,fecha;
		int cyear,cmonth,cday,ch,cm;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);

		f=cfecha(cyear,cmonth,cday);
		fecha=f+ch*100+cm;
		
		return fecha;
	}

	public String getActDateStr(){
		long f;
		int cyear,cmonth,cday;
		
		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		
		f=cfecha(cyear,cmonth,cday);
		
		return sfecha(f);
	}
	
	public long getCorelBase(){
		long f;
		int cyear,cmonth,cday,ch,cm,cs,vd,vh;
		
		final Calendar c = Calendar.getInstance();
		
		cyear = c.get(Calendar.YEAR);cyear=cyear % 10;
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);
				
		vd=cyear*384+cmonth*32+cday;
		vh=ch*3600+cm*60+cs;
		
		f=vd*100000+vh;
		f=f*100;
		
		return f;
	}

	public String getCorelBaseLong(long f) {
		long vy,vm,vd,m,h,sec;
		String s;

		//yyyyMMddhhmmss
		final Calendar c = Calendar.getInstance();

		vy = c.get(Calendar.YEAR);
		vm = c.get(Calendar.MONTH)+1;
		vd = c.get(Calendar.DAY_OF_MONTH);
		h=c.get(Calendar.HOUR_OF_DAY);
		m=c.get(Calendar.MINUTE);
		sec=c.get(Calendar.SECOND);

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		if (h>9)  s=s+h;  else s=s+"0"+h;
		if (m>9)  s=s+m;  else s=s+"0"+m;
		if (sec>9)  s=s+sec;  else s=s+"0"+sec;

		return s;
	}
	
}