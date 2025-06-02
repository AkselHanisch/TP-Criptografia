package ar.edu.itba.cripto;

import ar.edu.itba.cripto.exception.InvalidParameterException;

import java.util.Arrays;

public class Main {

    private ExecMode execMode;
    private String imagePath, dirPath;
    private int minimumShadows, totalShadows;

    private enum ExecMode {
        DISTRIBUTE("-d"),
        RESTORE("-r");

        private final String arg;

        ExecMode(String arg){
            this.arg = arg;
        }

        public static ExecMode fromArg(String arg){
            return Arrays.stream(ExecMode.values())
                    .filter(a -> arg.equals(a.arg))
                    .findAny().orElse(null);
        }

    }

    public static void main(String[] args){

        try {
            Main main = new Main(args);

            System.out.println(main.execMode);
            System.out.println(main.minimumShadows);
            System.out.println(main.totalShadows);
            System.out.println(main.imagePath);
            System.out.println(main.dirPath);
            //TODO...

        } catch (InvalidParameterException e){
            printHelpMessage("secret-image-sharing");
            System.exit(1);
        }
    }

    public Main(String[] args) throws InvalidParameterException{
        for (int i = 0; i < args.length; i++){
            boolean last = (i == args.length - 1);
            switch (args[i]){
                case "-d": case "-r":
                    if (execMode == null){
                        execMode = ExecMode.fromArg(args[i]);
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-secret":
                    if (imagePath == null && !last){
                        imagePath = args[++i];
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-k":
                    if (minimumShadows == 0 && !last){
                        minimumShadows = Integer.parseInt(args[++i]);
                        if (minimumShadows < 2 || minimumShadows > 10 || (totalShadows != 0 && minimumShadows > totalShadows)){
                            throw new InvalidParameterException();
                        }
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-n":
                    if  (totalShadows != 0 || last){
                        throw new InvalidParameterException();
                    }
                    totalShadows = Integer.parseInt(args[++i]);
                    if (totalShadows < 2 || minimumShadows > totalShadows){
                        throw new InvalidParameterException();
                    }
                    break;
                case "-dir":
                    if (dirPath != "./" || last){
                        throw new InvalidParameterException();
                    }
                    dirPath = args[++i];
                    break;
                default:
                    throw new InvalidParameterException();
            }
        }
        if (execMode == null || imagePath == null || minimumShadows == 0){
            throw new InvalidParameterException();
        }
    }

    public static void printHelpMessage(String name){
        System.out.println("Usage: " + name + " [-d | -r] -secret image -k number [-n number] [-dir directory]");
    }
}
