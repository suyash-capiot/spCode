package com.coxandkings.utils.files.unused;

import java.security.MessageDigest;
import java.util.Arrays;
//import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import org.apache.xmlbeans.impl.util.Base64;
import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("unused")
public class AESCrypt {
  /*
   * Please realise that the following IV is terrible.
   * (As easy to crack as ROT13...)
   * Real situations should use a randomly generated IV.
   */
  static String IV = "47ce2146-8e84-498c-8084-eb09634a5407";
  /* 
   * Note null padding on the end of the plaintext.
   */
  //static String plaintext = "test text 123\0\0\0"; 
  //static String plaintext = "<policy> <identity> <sign>b4e412a4-0cb7-49e5-a6b0-51e67f3b66b2</sign> <branchsign> c1e3d1f6-916a-456e-957c-b1fdf484d33b </branchsign> <username> Dummy_TA </username> <reference>86e21dae</reference> </identity> <plan> <categorycode>6B123144-2E3A-490E-BAEB-B59F09327B7C</categorycode> <plancode> 87e831b3-16be-49c9-994d-c2b52e9af113 </plancode> <basecharges>929</basecharges> <riders> <ridercode percent=10>d2b58365-5a73-4748-aa8f-51e5e78a7aba </ridercode> </riders > <totalbasecharges>1022</totalbasecharges> <servicetax>126</servicetax> <totalcharges>1022</totalcharges> </plan> <traveldetails> <departuredate>24-APR-2012</departuredate> <days>6</days> <arrivaldate>29-APR-2012</arrivaldate> </traveldetails> <insured> <passport>555</passport> <contactdetails> <address1>Test</address1> <address2>Test</address2> <city>Test</city> <district>Test</district> <state>Test</state> <pincode>3434</pincode> <country>Test</country> <phoneno>545435</phoneno> <mobile> 45545</mobile> <emailaddress>Test@hgd.com</emailaddress> </contactdetails> <name>Test</name> <dateofbirth>05-Aug-1941</dateofbirth> <age>70</age> <trawelltagnumber></trawelltagnumber> <nominee>self</nominee> <relation></relation> <pastillness></pastillness> </insured> <otherdetails><policycomment></policycomment> <universityname></universityname> <universityaddress></universityaddress> </otherdetails></policy>\0\0\0";
  static String plaintext = "<policy> <identity> <sign>b4e412a4-0cb7-49e5-a6b0-51e67f3b66b2</sign> <branchsign> c1e3d1f6-916a-456e-957c-b1fdf484d33b </branchsign> <username> Dummy_TA </username> <reference>86e21dae</reference> </identity> <plan> <categorycode>6B123144-2E3A-490E-BAEB-B59F09327B7C</categorycode> <plancode> 87e831b3-16be-49c9-994d-c2b52e9af113 </plancode> <basecharges>929</basecharges> <riders> <ridercode percent=10>d2b58365-5a73-4748-aa8f-51e5e78a7aba </ridercode> </riders > <totalbasecharges>1022</totalbasecharges> <servicetax>126</servicetax> <totalcharges>1022</totalcharges> </plan> <traveldetails> <departuredate>24-APR-2012</departuredate> <days>6</days> <arrivaldate>29-APR-2012</arrivaldate> </traveldetails> <insured> <passport>555</passport> <contactdetails> <address1>Test</address1> <address2>Test</address2> <city>Test</city> <district>Test</district> <state>Test</state> <pincode>3434</pincode> <country>Test</country> <phoneno>545435</phoneno> <mobile> 45545</mobile> <emailaddress>Test@hgd.com</emailaddress> </contactdetails> <name>Test</name> <dateofbirth>05-Aug-1941</dateofbirth> <age>70</age> <trawelltagnumber></trawelltagnumber> <nominee>self</nominee> <relation></relation> <pastillness></pastillness> </insured> <otherdetails><policycomment></policycomment> <universityname></universityname> <universityaddress></universityaddress> </otherdetails></policy>\0\0\0";
  static String encryptionKey = "d77112e9-40b3-49db-8522-bb127792b320";
  public static void main(String [] args) {
    try {

      System.out.println("==JAVA==");
      System.out.println("plain:   " + plaintext);
      System.out.println("length is " + plaintext.length());

      byte[] cipher;
      String ecipher = encrypt(plaintext, encryptionKey, IV);

      System.out.println("cipher:  " + ecipher.getBytes());
      cipher = ecipher.getBytes();
      
     // System.out.println("Tryin new one");
     // String ecipher = symmetricEncrypt(plaintext,encryptionKey);
     // System.out.println("ecipher is " + ecipher);
      
      /*for (int i=0; i<cipher.length; i++){
        System.out.print(new Integer(cipher[i])+" ");
      }*/
      //System.out.println("");

      String decrypted = decrypt(ecipher, encryptionKey, IV);

      System.out.println("decrypt: " + decrypted);

    } 
    
    catch (Exception e) 
    {
      e.printStackTrace();
    } 
  }

  public static String encrypt(String plainText, String encryptionKey, String InitVector) throws Exception 
  {
	  Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
	  //System.out.println("Length of key is : " + encryptionKey.getBytes("UTF-8").length);
    
	  MessageDigest sha = MessageDigest.getInstance("SHA-256");
	  byte[] keys = sha.digest(encryptionKey.getBytes("UTF-8"));
    
   // System.out.println("Length of key is : " + keys.length);
    
    SecretKeySpec key = new SecretKeySpec(keys,"AES");
    
    MessageDigest ivsha = MessageDigest.getInstance("SHA-1");
    byte[] ivs = ivsha.digest(InitVector.getBytes("UTF-8"));
    ivs = Arrays.copyOf(ivs, 16);
    
    //SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),1,36,"AES");
    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(ivs));
   // System.out.println(cipher.doFinal(plainText.getBytes("UTF-8")));
    return new String(cipher.doFinal(plainText.getBytes("UTF-8")));
  }

  public static String decrypt(String cipherText, String encryptionKey, String InitVector) throws Exception{
    Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
    byte[] cipherT = cipherText.getBytes();
    
    MessageDigest sha = MessageDigest.getInstance("SHA-256");
    byte[] keys = sha.digest(encryptionKey.getBytes("UTF-8"));

    SecretKeySpec key = new SecretKeySpec(keys,"AES");
    
    MessageDigest ivsha = MessageDigest.getInstance("SHA-1");
    byte[] ivs = ivsha.digest(InitVector.getBytes("UTF-8"));
    ivs = Arrays.copyOf(ivs, 16);
    
    //SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),1,36,"AES");
    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(ivs));
    return new String(cipher.doFinal(cipherT),"UTF-8");
  }
  
  
  
  public static String symmetricEncrypt(String text, String secretKey) {
      byte[] raw;
      String encryptedString;
      SecretKeySpec skeySpec;
      byte[] encryptText = text.getBytes();
      Cipher cipher;
      try {
          raw = Base64.decodeBase64(secretKey);
          skeySpec = new SecretKeySpec(raw, "AES");
          cipher = Cipher.getInstance("AES");
          cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
          encryptedString = Base64.encodeBase64String(cipher.doFinal(encryptText));
      } 
      catch (Exception e) {
          e.printStackTrace();
          return "Error";
      }
      return encryptedString;
  }

  public static String symmetricDecrypt(String text, String secretKey) {
      Cipher cipher;
      String encryptedString;
      byte[] encryptText = null;
      byte[] raw;
      SecretKeySpec skeySpec;
      try {
          raw = Base64.decodeBase64(secretKey);
          skeySpec = new SecretKeySpec(raw, "AES");
          encryptText = Base64.decodeBase64(text);
          cipher = Cipher.getInstance("AES");
          cipher.init(Cipher.DECRYPT_MODE, skeySpec);
          encryptedString = new String(cipher.doFinal(encryptText));
      } catch (Exception e) {
          e.printStackTrace();
          return "Error";
      }
      return encryptedString;
  }

  
  
}