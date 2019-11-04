/* 
 * File: 		Globals.java
 * Created:		2013/05/03
 * 
 * copyright (c) 2013 DigitalPersona Inc.
 */

package com.dts.uubio.uusample;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.Reader.Capabilities;

import android.content.Context;

public class Globals 
{
	public static Reader.ImageProcessing DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;
	//public static final Reader.ImageProcessing DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_PIV;

	public Reader getReader(String name, Context applContext) throws UareUException 
	{
		getReaders(applContext);

		for (int nCount = 0; nCount < readers.size(); nCount++)
		{
			if (readers.get(nCount).GetDescription().name.equals(name))
			{
				return readers.get(nCount);
			}
		}
		return null;
	}

	public ReaderCollection getReaders(Context applContext) throws UareUException
	{
		readers = UareUGlobal.GetReaderCollection(applContext);
		readers.GetReaders();
		return readers;
	}

	private ReaderCollection readers = null;
	private static Globals instance;

	static 
	{
		instance = new Globals();
	}

	public static Globals getInstance()
	{
		return Globals.instance;
	}

	private static Bitmap m_lastBitmap = null;

	public static void ClearLastBitmap()
	{
		m_lastBitmap = null;
	}

	public static Bitmap GetLastBitmap()
	{
		return m_lastBitmap;
	}

	private static int m_cacheIndex = 0;
	private static int m_cacheSize = 2;
	private static ArrayList<Bitmap> m_cachedBitmaps = new ArrayList<Bitmap>();

	public synchronized static Bitmap GetBitmapFromRaw(byte[] Src, int width, int height)
	{
		byte [] Bits = new byte[Src.length*4];
		int i = 0;
		for(i=0;i<Src.length;i++)
		{
			Bits[i*4] = Bits[i*4+1] = Bits[i*4+2] = (byte)Src[i];
			Bits[i*4+3] = -1;
		}

		Bitmap bitmap = null;
		if (m_cachedBitmaps.size() == m_cacheSize) {
			bitmap = m_cachedBitmaps.get(m_cacheIndex);
		}

		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			m_cachedBitmaps.add(m_cacheIndex, bitmap);
		} else if (bitmap.getWidth() != width || bitmap.getHeight() != height) {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			m_cachedBitmaps.set(m_cacheIndex, bitmap);
		}
		m_cacheIndex = (m_cacheIndex+1)%m_cacheSize;

		bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

		// save bitmap to history to be restored when screen orientation changes
		m_lastBitmap = bitmap;
		return bitmap;
	}

	public static final String QualityToString(Reader.CaptureResult result)
	{
		if(result == null) {
			return "";
		} if(result.quality == null) {
			return "Ocurrió un error";
		} switch(result.quality) {
            case FAKE_FINGER:         return "Dedo erroneo";
            case NO_FINGER:           return "Sin dedo";
            case CANCELED:            return "Captura cancelada";
            case TIMED_OUT:           return "Tiempo de captura expiro";
            case FINGER_TOO_LEFT:     return "Dedo muy a izquierda";
            case FINGER_TOO_RIGHT:    return "Dedo muy a derecho";
            case FINGER_TOO_HIGH:     return "Dedo muy arriba";
            case FINGER_TOO_LOW:      return "Dedo muy abajo";
            case FINGER_OFF_CENTER:   return "Dedo colocado incorrectamente";
            case SCAN_SKEWED:         return "Escaneo distorcionado";
            case SCAN_TOO_SHORT:      return "Escaneo muy corto";
            case SCAN_TOO_LONG:       return "Escaneo muy largo";
            case SCAN_TOO_SLOW:       return "Escaneo muy lento";
            case SCAN_TOO_FAST:       return "Escaneo muy rapido";
            case SCAN_WRONG_DIRECTION:return "Dirección incorrecta";
            case READER_DIRTY:        return "Lector sucio";
            case GOOD:                return "Imagen capturado";
            default:                  return "Ocurrió un error";
		}
	}
	public static final int GetFirstDPI(Reader reader)
	{
		Capabilities caps = reader.GetCapabilities();
		return caps.resolutions[0];
	}
}
