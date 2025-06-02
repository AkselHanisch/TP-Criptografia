package ar.edu.itba.cripto.math;


import java.util.Arrays;

public class Lagrange {
    private final Pair[] shadows;
    private final int m;

    public Lagrange(Pair[] shadows, int m){
        this.shadows = shadows;
        this.m = m;
    }

    public int eval(int x){
        int result = 0;
        for(int i = 0; i < shadows.length; i++){
            result += shadows[i].y() * L(x, i);
        }
        return mod(result);
    }

    private int L(int x, int i){
        int numerator = 1;
        int denominator = 1;
        for(int j = 0; j < shadows.length; j++){
            if (j != i){
                numerator *= (x - shadows[j].x());
                denominator *= (shadows[i].x() - shadows[j].x());
            }
        }
        return numerator * inverseMod(denominator);
    }

    public int[] getCoefficients(){
        int[] coefficients = new int[shadows.length];

        coefficients[0] = eval(0);
        Pair[] prevShadows = shadows;

        for(int i = 1; i < coefficients.length; i++){
            Pair[] newShadows = new Pair[prevShadows.length-1];
            for (int j = 0; j < prevShadows.length-1; j++){
                newShadows[j] = new Pair(
                        prevShadows[j].x(),
                        mod((prevShadows[j].y()-coefficients[i-1]) * inverseMod(prevShadows[j].x()))
                );
            }
            coefficients[i] = new Lagrange(newShadows, 11).eval(0);
            prevShadows = newShadows;
        }

        return coefficients;
    }

    public int mod(int n){
        return ((n % m) + m) % m;
    }

    public int inverseMod(int n){
        n = mod(n);
        for (int x = 1; x < m; x++){
            if ((n * x) % m == 1){
                return x;
            }
        }
        throw new ArithmeticException("No inverse exists");
    }


    public static void main(String[] args){
        Pair[] shadows = new Pair[]{
                new Pair(1, 3),
                new Pair(5, 10),
                new Pair(2, 9),
        };
        Lagrange l = new Lagrange(shadows, 11);
        System.out.println(Arrays.toString(l.getCoefficients()));
    }
}
