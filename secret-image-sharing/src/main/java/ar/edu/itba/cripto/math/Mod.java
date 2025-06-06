package ar.edu.itba.cripto.math;

public class Mod {

    public static int mod(int n, int m){
        return ((n % m) + m) % m;
    }

    public static int inverseMod(int n, int m){
        n = mod(n, m);
        for (int x = 1; x < m; x++){
            if ((n * x) % m == 1){
                return x;
            }
        }
        throw new ArithmeticException("No inverse exists");
    }

    public static int modPow(int base, int exponent, int m) {
        if (m == 1) return 0;
        int result = 1;
        base = base % m;
        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result = (result * base) % m;
            }
            base = (base * base) % m;
            exponent >>= 1;
        }
        return result;
    }

}
