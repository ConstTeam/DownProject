package platform.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

import util.ErrorPrint;

/**
 * 加密解密
 * @author zy
 *
 */
public class RSASignature{
	
	public static final String  SHA1WITHRSA = "SHA1WithRSA";
	public static final String  MD5WITHRSA = "MD5WithRSA";

	/**
	* 得到公钥
	*/
	public static PublicKey getPublicKey(String rsa) throws Exception 
	{
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		byte[] encodedKey = Base64.decodeBase64(rsa);
		PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

		return pubKey;
	}
	/**
	 * RSA 签名检查
	 * @param content 验签的内容
	 * @param sign 签名值
	 * @param rsa 公钥
	 * @param input_charset 字符串类型(UTF-8/GBK)
     * @param type 字符串类型 (SHA1WITHRSA/MD5WITHRSA)
	 * @return
	 * @throws Exception
	 */
    public static boolean verify(String content, String sign, String rsa, String inputCharset, String type) throws Exception{
    	PublicKey pubKey = getPublicKey(rsa);

    	try {
    		java.security.Signature signature = java.security.Signature.getInstance(type);
			signature.initVerify(pubKey);
			signature.update(content.getBytes(inputCharset));
			boolean result = signature.verify(Base64.decodeBase64(sign));

			return result;
    	} catch (Exception e) {
    		ErrorPrint.print(e);
    	}

    	return false;
    }
    /**
     * 加密
     * @param content 待加密数据
     * @param key 加密私钥
     * @param input_charset 字符串类型(UTF-8/GBK)
     * @param type 字符串类型 (SHA1WITHRSA/MD5WITHRSA)
     * @return
     */
    public static String sign(String content, String key, String inputCharset, String type){
        try {
        	PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec(Base64.decodeBase64(key) ); 
        	KeyFactory          keyf        = KeyFactory.getInstance("RSA");
        	PrivateKey          priKey 		= keyf.generatePrivate(priPKCS8);
            java.security.Signature signature = java.security.Signature.getInstance(type);
            signature.initSign(priKey);
            signature.update(content.getBytes(inputCharset) );
            byte[] signed = signature.sign();
            return Base64.encodeBase64String(signed);
        }
        catch (Exception e) {
        	ErrorPrint.print(e);
        }
        return null;
    }
}
