package ar.edu.itba.cripto;

import ar.edu.itba.cripto.exception.InvalidParameterException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class Arguments {

    public ExecMode execMode;

    public String secretPath;

    public int k;
    public Integer n;
    public String dir = "./";


    public enum ExecMode {
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

    public Arguments(String[] args) throws InvalidParameterException {
        if (args.length <= 5 || !args[1].equals("-secret") || !args[3].equals("-k") || args.length > 9){
            throw new InvalidParameterException();
        }

        execMode = ExecMode.fromArg(args[0]);
        secretPath = args[2];
        try {
            k = Integer.parseInt(args[4]);
        } catch (NumberFormatException e){
            throw new InvalidParameterException();
        }

        for(int i = 5; i <= args.length-2; i+=2){
            switch (args[i]){
                case "-n":
                    try {
                        n = Integer.parseInt(args[i+1]);
                    } catch (NumberFormatException e){
                        throw new InvalidParameterException();
                    }
                    break;
                case "-dir":
                    dir = args[i+1];
                    break;
                default:
                    throw new InvalidParameterException();
            }
        }

        if (k < 2 || k > 10){
            System.out.println("Error: k must be in the range of [2, 10]");
            System.exit(1);
        }

        if (execMode == ExecMode.DISTRIBUTE && !Files.exists(Path.of(secretPath))){
            System.out.println("Error: secret image does not exist");
        }

        if (!Files.exists(Path.of(dir)) || !Files.isDirectory(Path.of(dir))){
            System.out.println("Error: Invalid directory path");
            System.exit(1);
        }

        if (n != null) {
            if (execMode != ExecMode.DISTRIBUTE){
                System.out.println("Error: -n must only be used on distribute mode");
                System.exit(1);
            }
            if (n < 2){
                System.out.println("Error: n must be at least 2");
                System.exit(1);
            }
            if (k > n){
                System.out.println("Error: k must be lesser or equal to n");
                System.exit(1);
            }
        } else if (execMode == ExecMode.DISTRIBUTE) {
            try (Stream<Path> files = Files.list(Path.of(dir))){
                n = (int) files.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".bmp"))
                        .count();
            } catch (IOException e){
                System.out.println("Error reading directory: " + e.getMessage());
                System.exit(1);
            }
            if (n < 2){
                System.out.println("Error: n must be at least 2");
                System.exit(1);
            }
            if (k > n){
                System.out.println("Error: k must be lesser or equal to n");
                System.exit(1);
            }
        }



    }
}
