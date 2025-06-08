package ar.edu.itba.cripto.math;
import java.math.BigInteger;

public class Mod {

    public static long mod(long n, int m){
        return ((n % m) + m) % m;
    }

    public static int inverseMod(int n, int m){
        return BigInteger.valueOf(mod(n, m)).modInverse(BigInteger.valueOf(m)).intValue();
    }


}
