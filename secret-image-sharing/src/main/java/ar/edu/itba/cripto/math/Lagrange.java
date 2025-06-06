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
        return Mod.mod(result, m);
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
        return numerator * Mod.inverseMod(denominator, m);
    }



    public int[] getCoefficients(){
        int[] coefficients = new int[shadows.length];

        coefficients[0] = eval(0);
        Pair[] prevShadows = shadows;

        for(int i = 1; i < coefficients.length; i++){
            Pair[] newShadows = new Pair[prevShadows.length-1];
            for(int j = 0; j < prevShadows.length-1; j++){
                newShadows[j] = new Pair(
                        prevShadows[j].x(),
                        Mod.mod((prevShadows[j].y() - coefficients[i-1]) * Mod.inverseMod(prevShadows[j].x(), m), m)
                );
            }

            coefficients[i] = new Lagrange(newShadows, m).eval(0);
            prevShadows = newShadows;
        }

        return coefficients;
    }


    public static void main(String[] args){
        Pair[] shadows = new Pair[]{
                new Pair(1, ((byte)-27) & 0xFF),
                new Pair(2, 7),
                new Pair(3, 85),
                new Pair(4, 0),
                new Pair(5, 14),
                new Pair(6, ((byte)-83) & 0xFF),
                new Pair(7, 45),
                new Pair(8, 1),
        };
        Lagrange l = new Lagrange(shadows, 257);
        System.out.println(Arrays.toString(l.getCoefficients()));
    }
}
