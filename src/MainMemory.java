import java.io.*;
import java.util.Scanner;

public class MainMemory {
    private static int[] data = new int[2000];
    private static BufferedReader readFile, readInstruction;
    private static Scanner file;
    public static void main(String[] args) {
        try{
            readFile = new BufferedReader(new FileReader(args[0])); //creates scanner pointing to file in argument 0
            file = new Scanner(new File(args[0]));
        }catch(FileNotFoundException e) {
            System.out.println("File error");
            System.exit(1);
        }
        int i = 0;
        String line;
            /*
            while((line = readFile.readLine()) != null){
                try {
                    data[index++] = Integer.parseInt(line);
                }
                catch(NumberFormatException n){ //when there is a period
                    index = Integer.parseInt(line.substring(1)); //sets load address to the integer after the period.
                }
            }*/
        while(file.hasNextLine()){
            String next = file.nextLine();
            //Searches for address such as .1000 or .500
            if(next.matches("(\\.)(\\d+).*")){
                i = Integer.parseInt(next.replaceFirst(".*?(\\d+).*", "$1"));
            }
            //Searches for values or instructions
            else if(next.matches(".*?(\\d+).*")) {
                write(i++,next);
            }
        }
        Scanner readInstructions = new Scanner(System.in);
        while (readInstructions.hasNextLine()){
            String ins = readInstructions.nextLine();
            int address;
            if(ins.equals("Exit")){
                readInstructions.close();
                System.exit(0);
            }
            if (ins.matches("(\\d+) (\\d+).*")) {
                Scanner inst = new Scanner(ins);
                address = Integer.parseInt(inst.next());
                write(address, inst.next());
                inst.close();
            } else {
                address = Integer.parseInt(ins);
                System.out.println(read(address));
            }
        }
        readInstructions.close();
    }
    private static int read(int address){
        return data[address];
    }
    private static void write(int address, String val){
        data[address] = Integer.parseInt(val.replaceFirst(".*?(\\d+).*", "$1"));
    }
}
