package at.tugraz.ist.akm.keystore;

public class CertificateDefaultAttributes
{

    public static final String ALIAS_NAME="at.tugraz.ist.akm.WebSMSTool";
    public static final String ISSUER = "C=AT,ST=Styria,L=Graz,O=TU Graz,OU=IST,CN=AKM Team 23";
    public static final String SUBJECT = "C=AT,ST=Styria,L=Graz,O=TU Graz,OU=IST,CN=AKM Team 23";
    private static final int VALID_DURATION_YEARS_FROM_NOW = 1;
    private static final int VALID_DURATION_DAYS_BEFORE_NOW = 1;
    public static final long VALID_DURATION_FROM_NOW_MILLISECONDS = 1000L * 60 * 60 * 24 * 365 * VALID_DURATION_YEARS_FROM_NOW;
    public static final long VALID_DURATION_BEFORE_NOW_MILLISECONDS = 1000L * 60 * 60 * 24 * VALID_DURATION_DAYS_BEFORE_NOW;
    
    public static final String ENCRYPTION_ALGORITHM = "SHA1WithRSAEncryption";
    
    public static final String KEYPAIR_GENERATOR="RSA";
    public static final int KEYPAIR_LENGTH_BITS= 1024;
    
}
