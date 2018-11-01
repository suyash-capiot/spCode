package com.coxandkings.utils.files.unused;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
 * @author julien
 */
public class BouncyCastleTest {
   
    //private static final Logger log = LoggerFactory.getLogger(BouncyCastleTest.class);

    //@Test
    public void testEncryptRijndael() throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        BlockCipher engine = new RijndaelEngine(256);
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new ZeroBytePadding());
        
        byte[] keyBytes = "0123456789abcdef0123456789abcdef".getBytes();
        //my add
        byte[] iv = "0123456789abcdef0123456789abcdef".substring(0, 16).getBytes();
        cipher.init(true, new KeyParameter(keyBytes));
        /* my addition starts ***********************************************/
        //my add
        byte[] data = "value".getBytes();
        
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
        len += cipherb.doFinal(buf, len);
 
        // remove padding
        byte[] out = new byte[len];
        System.arraycopy(buf, 0, out, 0, len);
        // return string representation of decoded bytes
        System.out.println("Rijndeal algo output in base64 "+ new String(Base64.encode(out)));
        //return (new String(Base64.encode(out)));

        
        
        
        
        /* my addition ends******************************/
        
        
        
        byte[] input = "value".getBytes();
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        
        int cipherLength = cipher.processBytes(input, 0, input.length, cipherText, 0);
        cipher.doFinal(cipherText, cipherLength);
        
        @SuppressWarnings("unused")
		String result = new String(Base64.encode(cipherText));
        //log.debug("result : " + result);
        //AssertJUnit.assertNotNull(result);
    }
    
    //@Test
    public void testDecryptRijndael() throws DataLengthException, IllegalStateException, InvalidCipherTextException {
        BlockCipher engine = new RijndaelEngine(256);
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new ZeroBytePadding());
        
        byte[] keyBytes = "0123456789abcdef0123456789abcdef".getBytes();
        cipher.init(false, new KeyParameter(keyBytes));
        
        byte[] output = Base64.decode("Ij7J7G33H5xE9K5vaTiEypPnjJPuDdZ0C9QyvcIj/ZI=".getBytes());
        byte[] cipherText = new byte[cipher.getOutputSize(output.length)];
        
        int cipherLength = cipher.processBytes(output, 0, output.length, cipherText, 0);
        int outputLength = cipher.doFinal(cipherText, cipherLength);
        outputLength += cipherLength;
        
        byte[] resultBytes = cipherText;
        if (outputLength != output.length) {
            resultBytes = new byte[outputLength];
            System.arraycopy(
                    cipherText, 0,
                    resultBytes, 0,
                    outputLength
                );
        }
        
        @SuppressWarnings("unused")
		String result = new String(resultBytes);
        //log.debug("result : " + result);
       // AssertJUnit.assertEquals("value", result);
    }
        
    //@Test
    public void testSha1() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        byte[] keyBytes = "0123456789abcdef0123456789abcdef".getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HMac-SHA1");
        
        Mac mac = Mac.getInstance("HMac-SHA1", "BC");
        mac.init(secretKey);
        mac.reset();
        
        byte[] input = "value".getBytes();
        mac.update(input, 0, input.length);
        byte[] out = mac.doFinal();
        
        String result = new String(Base64.encode(out));
        System.out.println("result "+ result);
        //log.debug("result : " + result);
       // AssertJUnit.assertNotNull(result);
    }
    public static void main(String [] args) {
    BouncyCastleTest b=new BouncyCastleTest();
    try {
		b.testEncryptRijndael();
	} catch (DataLengthException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalStateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvalidCipherTextException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    }

