import java.io.*;
import java.util.Random;
public class Processor {

    private static Process mem = null; //creation of child process to interact with memory

    //IO objects
    private static BufferedReader input;
    private static PrintWriter output;

    //Register creation
    private static int PC = 0;
    private static int SP = 1000;
    private static int IR = 0, X=0, Y=0, AC=0, timer=0;

    //CPU Settings
    private static final int MEMORY_BOUND = 1000;
    private static boolean userMode = true;
    private static boolean exitFlag = false;
    private static int intTime = 0;

    //Contains all instructions that need another operand
    private static final int[] opcodes = {1,2,3,4,5,7,9,20,21,22,23}; // 1-7 have operand values while 20-23 are expecting addresses

    public static void main(String[] args) throws IOException {
        //Start process and memory read
        mem = Runtime.getRuntime().exec("java MainMemory " + args[0]);
        intTime = Integer.parseInt(args[1]);
        input = new BufferedReader(new InputStreamReader(mem.getInputStream()));
        output = new PrintWriter(new OutputStreamWriter(mem.getOutputStream()));

        //Main Instruction cycle
        while (!exitFlag) {
            doFetchCycle();
        }
        //End program, object deletion
        output.close();
        input.close();
        System.exit(0);
    }
    //Method that fetches instructions that will be executed later
    private static void doFetchCycle() throws IOException{
        boolean isOp = false;
        IR = read(PC);
        for (int opcode : opcodes) {
            if (opcode == IR) { //next opcode is required when processing instruction
                isOp = true;
                break;
            }
        }
        if(isOp){
            int nextOp = read(++PC);
            checkBoundary(nextOp);
            executeInstruction(IR, nextOp);
        }
        else executeInstruction(IR, 0); //run instruction with opcode 0, since there is no opcode
        PC++; //increment program counter
    }
    private static void executeInstruction(int instruction, int nextOp) throws IOException {
        System.out.println("PC: " + PC + "\t" + IR);
        switch(instruction){
            case 1: //Load value into AC
                AC = nextOp;
                break;
            case 2: //Load value from address into AC
                AC = read(nextOp);
                break;
            case 3: //Load the value from the address found in the given address into the AC
                checkBoundary(read(nextOp));
                AC = read((read(nextOp)));
                break;
            case 4: //Load the value at (address+X) into the AC
                checkBoundary(nextOp + X);
                AC = read(nextOp + X);
                break;
            case 5: //Load the value at (address+Y) into the AC
                checkBoundary(nextOp + Y);
                AC = read(nextOp + Y);
                break;
            case 6: //Load value of stack pointer plus X into AC
                checkBoundary(SP + X);
                AC = read(SP + X);
                break;
            case 7: //Write AC to address
                checkBoundary(nextOp);
                write(nextOp, AC);
                break;
            case 8: //Store random number 1-100 into AC
                Random r = new Random();
                AC = r.ints(1, 101).findFirst().getAsInt();
                break;
            case 9: //If port=1, writes AC as an int to the screen. If port=2, writes AC as a char to the screen
                if(nextOp == 1){
                    System.out.print(AC);
                }
                else if(nextOp == 2){
                    System.out.print((char) AC);
                }
                break;
            case 10: //Add X to AC
                AC += X;
                break;
            case 11: //Add Y to AC
                AC += Y;
                break;
            case 12: //Sub X from AC
                AC -= X;
                break;
            case 13: //Sub Y from AC
                AC -= Y;
                break;
            case 14: //set X to AC
                X = AC;
                break;
            case 15: //set AC to X
                AC = X;
                break;
            case 16: //Set Y to AC
                Y = AC;
                break;
            case 17: //Set AC to Y
                AC = Y;
                break;
            case 18: //Set Sp to AC
                SP = AC;
                break;
            case 19: //Set AC to SP
                AC = SP;
                break;
            case 20: //Jump to address of nextOp
                PC = nextOp -1;
                break;
            case 21: //Jump to address if AC is 0
                if(AC == 0){
                    PC = nextOp - 1;
                }
                break;
            case 22: //Jump to address if AC != 0
                if(AC != 0){
                    PC = nextOp - 1;
                }
                break;
            case 23: //Add PC to stack and jump to nextOp
                write(--SP, ++PC);
                PC = nextOp - 1;
                break;
            case 24: //Pop from stack and jump to address
                PC = read(SP) -1;
                SP++;
                break;
            case 25:
                X++;
                break;
            case 26:
                X--;
                break;
            case 27: //Push AC onto SP
                SP--;
                write(SP, AC);
                break;
            case 28: //Pop stack to AC
                AC = read(SP);
                SP++;
                break;
            case 29: //Interrupt - System Mode
                if(userMode){
                    userMode = false;
                    //SYSTEM MODE: Sets all registers to System memory, cascading down from 2000.
                    write(1999, SP);
                    write(1998, ++PC);
                    write(1997, AC);
                    write(1996, X);
                    write(1995, Y);
                    SP = 1995;
                    PC = 1500 - 1; //will increment after execution
                    break;
                }
            case 30: //Return to user mode
                if(SP < 2000){
                    userMode = true;
                    SP = read(1999);
                    PC = read(1998) - 1;
                    AC = read(1997);
                    X = read(1996);
                    Y = read(1995);
                }
                else{
                    System.out.println("Stack is empty");
                    mem.destroy();
                    System.exit(1);
                }
                break;
            case 50: //End program
                output.write("Exit");
                exitFlag = true;
                break;
            default:
                System.err.println("No instruction given");
                System.exit(1);
                break;
        }
        timer++;
        if(timer == intTime){ //When timer hits the set interval to perform interrupt
            //.out.println("*****INTERRUPT*****");
            if(userMode){
                userMode = false;
                //SYSTEM MODE: Sets all registers to System memory, cascading down from 2000.
                write(1999, SP);
                PC++;
                write(1998, PC);
                write(1997, AC);
                write(1996, X);
                write(1995, Y);
                SP = 1995;
                PC = 1000 - 1;
                userMode = false;
            }
            timer = 0;
        }
    }
    //Memory supported Functions
    private static int read(int memadress) throws IOException{
        output.println(memadress);
        output.flush();
        return Integer.parseInt(input.readLine());
    }
    private static void write(int memaddress, int val) {
        output.write(String.format("%d %d\n", memaddress, val));
        output.flush();
    }
    //Check if address is not violating memory boundary
    private static void checkBoundary(int address){
        if(outOfMemoryBounds(address)){
            System.out.println("Memory Index Exception at address " + address);
            System.exit(0);
        }
    }
    private static boolean outOfMemoryBounds(int address){
        if(userMode){
            return address < 0 || address >= MEMORY_BOUND; //User mode memory restricted between 0 and 1000. If kernel mode, can exceed more than user.
        }
        else return false;
    }
}
