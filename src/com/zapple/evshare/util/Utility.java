/*
 * Copyright (C) 2012 Li Cong, forlong401@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zapple.evshare.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class Utility {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset ASCII = Charset.forName("US-ASCII");
    
    public final static String readInputStream(InputStream in, String encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, encoding);
        StringBuffer sb = new StringBuffer();
        int count;
        char[] buf = new char[512];
        while ((count = reader.read(buf)) != -1) {
            sb.append(buf, 0, count);
        }
        return sb.toString();
    }

    public final static boolean arrayContains(Object[] a, Object o) {
        for (int i = 0, count = a.length; i < count; i++) {
            if (a[i].equals(o)) {
                return true;
            }
        }
        return false;
    }	
	
    private static byte[] encode(Charset charset, String s) {
        if (s == null) {
            return null;
        }
        final ByteBuffer buffer = charset.encode(CharBuffer.wrap(s));
        final byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }

    private static String decode(Charset charset, byte[] b) {
        if (b == null) {
            return null;
        }
        final CharBuffer cb = charset.decode(ByteBuffer.wrap(b));
        return new String(cb.array(), 0, cb.length());
    }

    /** Converts a String to UTF-8 */
    public static byte[] toUtf8(String s) {
        return encode(UTF_8, s);
    }

    /** Builds a String from UTF-8 bytes */
    public static String fromUtf8(byte[] b) {
        return decode(UTF_8, b);
    }

    /** Converts a String to ASCII bytes */
    public static byte[] toAscii(String s) {
        return encode(ASCII, s);
    }

    /** Builds a String from ASCII bytes */
    public static String fromAscii(byte[] b) {
        return decode(ASCII, b);
    }

    /**
     * @return true if the input is the first (or only) byte in a UTF-8 character
     */
    public static boolean isFirstUtf8Byte(byte b) {
        // If the top 2 bits is '10', it's not a first byte.
        return (b & 0xc0) != 0x80;
    }

    public static String byteToHex(int b) {
        return byteToHex(new StringBuilder(), b).toString();
    }

    public static StringBuilder byteToHex(StringBuilder sb, int b) {
        b &= 0xFF;
        sb.append("0123456789ABCDEF".charAt(b >> 4));
        sb.append("0123456789ABCDEF".charAt(b & 0xF));
        return sb;
    }	
	
    /**
     * Cancel an {@link AsyncTask}.  If it's already running, it'll be interrupted.
     */
    public static void cancelTaskInterrupt(AsyncTask<?, ?, ?> task) {
        cancelTask(task, true);
    }

    /**
     * Cancel an {@link AsyncTask}.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *        task should be interrupted; otherwise, in-progress tasks are allowed
     *        to complete.
     */
    public static void cancelTask(AsyncTask<?, ?, ?> task, boolean mayInterruptIfRunning) {
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(mayInterruptIfRunning);
        }
    }	  

    /**
     * @return Device's unique ID if available.  null if the device has no unique ID.
     */
    public static String getConsistentDeviceId(Context context) {
        final String deviceId;
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return null;
            }
            deviceId = tm.getDeviceId();
            if (deviceId == null) {
                return null;
            }
        } catch (Exception e) {
            Log.d("utility", "Error in TelephonyManager.getDeviceId(): " + e.getMessage());
            return null;
        }
        final MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            return null;
        }
        sha.update(Utility.toUtf8(deviceId));
        final int hash = getSmallHashFromSha1(sha.digest());
        return Integer.toString(hash);
    }    
    
    /**
     * @return a non-negative integer generated from 20 byte SHA-1 hash.
     */
    /* package for testing */ static int getSmallHashFromSha1(byte[] sha1) {
        final int offset = sha1[19] & 0xf; // SHA1 is 20 bytes.
        return ((sha1[offset]  & 0x7f) << 24)
                | ((sha1[offset + 1] & 0xff) << 16)
                | ((sha1[offset + 2] & 0xff) << 8)
                | ((sha1[offset + 3] & 0xff));
    }    
    
    public static ByteArrayInputStream streamFromAsciiString(String ascii) {
        return new ByteArrayInputStream(toAscii(ascii));
    }    
    
	public static String getMimeType(String fileName) {
		String mimeType = "application/octet-stream";

        // Try to find an extension in the filename
        if (!TextUtils.isEmpty(fileName)) {
            int lastDot = fileName.lastIndexOf('.');
            String extension = null;
            if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
        		// Convert extensions to lower case letters for easier comparison
            	extension = fileName.substring(lastDot + 1).toLowerCase();
            }
            if (!TextUtils.isEmpty(extension)) {
                // Extension found.  Look up mime type, or synthesize if none found.
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null) {
                	if(extension.equalsIgnoreCase("aac") || 
                			extension.equalsIgnoreCase("amr")){
                		mimeType = "audio/" + extension;
                	}else{
                		mimeType = "application/" + extension;
                	}                    
                }else if(extension.equals("3gpp")){
                	mimeType = "audio/3gpp";
                }
            }
        }

        // Fallback case - no good guess could be made.
        return mimeType;		
	}    
    
	public static boolean isApkFile(String fileName){
		String type = getMimeType(fileName);
		if(type.equals("application/vnd.android.package-archive")){
			return true;
		}
		return false;			
	}    
    
    public static String convertSqlite3KeyWord(String target){
    	String ret = "";
    	
    	if(target != null){
    		for(int i = 0; i < target.length(); i++){
    			String convert = null;
    			char cchar = target.charAt(i);
    			switch(cchar){
    			case '/':
    				break;
    			case '\'':
    				convert = "''";
    				ret += convert;  	
    			break;
    			case '[':
    			case ']':
    			case '%':
    			case '&':
    			case '_':
    			case '(':
    			case ')':
    				convert = "/"+cchar;
    				ret += convert;
    				break;
    			default:
    				ret += cchar;
    				break;
    			}		
    		}
    	}	
    	return ret;    	
    }    
    
	/*
	1. (?i) means IgnoreCase mode option.
	2. (?s) means Singleline mode option which make the "." matchs 
	any characters (Include "\n").
	3. (.*?) means it matchs any characters except "\n" and 
	repeats any times but least repeat.
	4. extracting the BODY from an HTML string
	(?si)<body>(?P<contents>.*)</body>
	5. Replace everything except the contents of BODY Section from html 
	(?is)(<html>.*<head>.*</head>.*<body>)|(</body>.*</html>)
	*/    
    public static String getTextFromHtml(String html){
    	String text = html;
    	// remove comment tag
//    	body = body.replaceAll("<!--(?s)(.*?)-->", "");
    	// remove style tag
    	text = text.replaceAll("<(?i)style(?s)(.*?)>(?s)(.*?)</(?i)style>", "");
    	// remove script tag
    	text = text.replaceAll("<(?i)script(?s)(.*?)>(?s)(.*?)</(?i)script>", "");
    	// remove all tag
    	text = text.replaceAll("(?is)<.*?>", "");
    	// handle the some html attribute
    	text = Html.fromHtml(text).toString();
    	// remove /r, /n, and more space
    	text = text.replaceAll("\\s+", " ");
    	return text;
    }    
    
    public static String getBodyTextFromHtml(String html){
    	String body = html;
        Matcher m = Pattern.compile("(?is)<(?i)body(?s)(.*?)>(.*)</(?i)body>").matcher(html);
        
        if(m.find()){
        	// regular html
           	body = m.group();
           	body = getTextFromHtml(body);
        }else{
        	// unregular html
        	body = getTextFromHtml(body);
        }
        return body;        
    }    
    
}