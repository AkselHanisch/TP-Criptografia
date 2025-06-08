package ar.edu.itba.cripto.math;


public class Lagrange {
    private final Pair[] shadows;
    private final int m;

    public Lagrange(Pair[] shadows, int m){
        this.shadows = shadows;
        this.m = m;
    }

    private int L(int x, int i){
        long numerator = 1;
        long denominator = 1;
        for(int j = 0; j < shadows.length; j++){
            if (j != i){
                numerator = Mod.mod(numerator * (x - shadows[j].x()), m);
                denominator = Mod.mod(denominator * (shadows[i].x() - shadows[j].x()), m);
            }
        }
        return (int) Mod.mod(numerator * Mod.inverseMod((int) denominator, m), m);
    }

    public long eval(int x){
        long result = 0;
        for(int i = 0; i < shadows.length; i++){
            long term = (long) shadows[i].y() * L(x, i);
            result = Mod.mod(result + term, m);
        }
        return result;
    }


    public int[] getCoefficients(){
        int[] coefficients = new int[shadows.length];

        coefficients[0] = (int) eval(0);
        Pair[] prevShadows = shadows;

        for(int i = 1; i < coefficients.length; i++){
            Pair[] newShadows = new Pair[prevShadows.length-1];
            for(int j = 0; j < prevShadows.length-1; j++){
                newShadows[j] = new Pair(
                        prevShadows[j].x(),
                        (int) Mod.mod((long) (prevShadows[j].y() - coefficients[i - 1]) * Mod.inverseMod(prevShadows[j].x(), m), m)
                );
            }

            coefficients[i] = (int) new Lagrange(newShadows, m).eval(0);
            prevShadows = newShadows;
        }

        return coefficients;
    }



}
