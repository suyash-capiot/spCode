package com.coxandkings.utils.files;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testng.AssertJUnit;
//import org.testng.annotations.Test;

/**
 * Test on BouncyCastle.
 * 
 * 
 */
public class Rijndael {
   
    public static String EncryptRijndael(String XMLStrng, String key, String referencekey) 
		{
			BlockCipher engine = new RijndaelEngine(256);
			BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new ZeroBytePadding());
			byte[] keyBytes = key.substring(0, 32).getBytes();
			byte[] iv = referencekey.substring(0, 16).getBytes();
			cipher.init(true, new KeyParameter(keyBytes));
			byte[] data = XMLStrng.getBytes();
        
			KeyParameter k = new KeyParameter(keyBytes);
			CipherParameters params = new ParametersWithIV(k, iv);   
        
        
			// setup AES cipher in CBC mode with PKCS7 padding
			BlockCipherPadding padding = new PKCS7Padding();     
			BufferedBlockCipher cipherb = new PaddedBufferedBlockCipher(new CBCBlockCipher(new RijndaelEngine(128)), padding);
                
                                
			cipherb.reset();
			cipherb.init(true, params);
 
			// create a temporary buffer to decode into (it'll include padding)
			byte[] buf = new byte[cipherb.getOutputSize(data.length)];
			int len = cipherb.processBytes(data, 0, data.length, buf, 0);
			try {
				len += cipherb.doFinal(buf, len);
			} catch (DataLengthException e) {
				
				e.printStackTrace();
			} catch (IllegalStateException e) {
				
				e.printStackTrace();
			} catch (InvalidCipherTextException e) {
				
				e.printStackTrace();
			}
 
			// remove padding
			byte[] out = new byte[len];
			System.arraycopy(buf, 0, out, 0, len);
			// return string representation of decoded bytes
			//System.out.println("Rijndeal algo output  "+ out.toString());
			//System.out.println("Rijndeal algo output bytes  "+ Base64.encode(out).toString());
			//System.out.println("Rijndeal algo output in base64 :"+ new String(Base64.encode(out)) + "...    this is string");
			return (new String(Base64.encode(out)));
        
    }
    
    
    public static void main(String [] args) {
    //Rijndael b=new Rijndael();
	String plaintext = "<policy> <identity> <sign>b4e412a4-0cb7-49e5-a6b0-51e67f3b66b2</sign> <branchsign> c1e3d1f6-916a-456e-957c-b1fdf484d33b </branchsign> <username> Dummy_TA </username> <reference>86e21dae</reference> </identity> <plan> <categorycode>6B123144-2E3A-490E-BAEB-B59F09327B7C</categorycode> <plancode> 87e831b3-16be-49c9-994d-c2b52e9af113 </plancode> <basecharges>929</basecharges> <riders> <ridercode percent=\"10\">d2b58365-5a73-4748-aa8f-51e5e78a7aba </ridercode> </riders > <totalbasecharges>1022</totalbasecharges> <servicetax>126</servicetax> <totalcharges>1022</totalcharges> </plan> <traveldetails> <departuredate>24-APR-2012</departuredate> <days>6</days> <arrivaldate>29-APR-2012</arrivaldate> </traveldetails> <insured> <passport>555</passport> <contactdetails> <address1>Test</address1> <address2>Test</address2> <city>Test</city> <district>Test</district> <state>Test</state> <pincode>3434</pincode> <country>Test</country> <phoneno>545435</phoneno> <mobile> 45545</mobile> <emailaddress>Test@hgd.com</emailaddress> </contactdetails> <name>Test</name> <dateofbirth>05-Aug-1941</dateofbirth> <age>70</age> <trawelltagnumber></trawelltagnumber> <nominee>self</nominee> <relation></relation> <pastillness></pastillness> </insured> <otherdetails><policycomment></policycomment> <universityname></universityname> <universityaddress></universityaddress> </otherdetails></policy>";
	String encryptionKey = "d77112e9-40b3-49db-8522-bb127792b320";
	String refKey ="47ce2146-8e84-498c-8084-eb09634a5407";
    try {
		String s = Rijndael.EncryptRijndael(plaintext,encryptionKey,refKey);
		System.out.println("Encrypted value is : " + s);
	} catch (Exception e) {
		
		e.printStackTrace();
	} 
    }
    }

