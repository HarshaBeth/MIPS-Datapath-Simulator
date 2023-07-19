import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class MIPSAssembler implements Adder, Control, Instruction, Registers, Multiplexer, ALU {
    private static final int ZERO_REG = 0;
    final static int ADDRESS_SIZE = 200;

    public static final Map<String, Integer> registers;
    public Map<String, Integer> controlSignals;
    public Map<Integer, Integer> registerVals;

    static {
        Map<String, Integer> registersHash = new HashMap<>();
        registersHash.put("$zero", 0);
        registersHash.put("$at", 1);
        registersHash.put("$v0", 2);
        registersHash.put("$v1", 3);
        registersHash.put("$a0", 4);
        registersHash.put("$a1", 5);
        registersHash.put("$a2", 6);
        registersHash.put("$a3", 7);
        registersHash.put("$t0", 8);
        registersHash.put("$t1", 9);
        registersHash.put("$t2", 10);
        registersHash.put("$t3", 11);
        registersHash.put("$t4", 12);
        registersHash.put("$t5", 13);
        registersHash.put("$t6", 14);
        registersHash.put("$t7", 15);
        registersHash.put("$s0", 16);
        registersHash.put("$s1", 17);
        registersHash.put("$s2", 18);
        registersHash.put("$s3", 19);
        registersHash.put("$s4", 20);
        registersHash.put("$s5", 21);
        registersHash.put("$s6", 22);
        registersHash.put("$s7", 23);
        registersHash.put("$t8", 24);
        registersHash.put("$t9", 25);
        registersHash.put("$k0", 26);
        registersHash.put("$k1", 27);
        registersHash.put("$gp", 28);
        registersHash.put("$sp", 29);
        registersHash.put("$fp", 30);
        registersHash.put("$ra", 31);

        // Make the map unmodifiable
        registers = Collections.unmodifiableMap(registersHash);
    }

    public MIPSAssembler() {
        initializedControlSignals();
        registersValue();
    }

    public void run() {
        int pc, op, clockCycle = 0;
        String instructionBin = null;
        int[] address = new int[ADDRESS_SIZE];

        // * this acts as the memory (the instruction memory is stored at the current address)
        String[] addressValues32Bin = new String[ADDRESS_SIZE];
        addressValues32Bin[4] = "00000001001010100110100000100000"; // * test value
        addressValues32Bin[3] = "00000001001010101000100000101010"; // * test value

        int[] addressOutputs = new int[ADDRESS_SIZE];
        Arrays.fill(addressOutputs, 0);

        // * the values that are stored in their memory locations
        addressOutputs[1] = 50; // * test value
        addressOutputs[2] = 21; // * test value
        addressOutputs[3] = 128; // * test value
        addressOutputs[4] = 35; // * test value

        int indexCounter = 0;

        // * Get the program and the starting address
        pc = Integer.parseInt(JOptionPane.showInputDialog("Enter starting address: "));
        int startingAddress = pc;
        address[indexCounter] = pc;

        // * do while so the program keeps running until the user wants to stop
        char flag = 'y';
        while (flag == 'y') {
            // each instruction is 1 clock cycle
            clockCycle++;

            // * print all the registers values
            String registerValsString = "";
            for (Map.Entry<Integer, Integer> entry : registerVals.entrySet()) {
                registerValsString += entry.getKey() + " : " + entry.getValue() + "\n";
            }
            JOptionPane.showMessageDialog(null, "Register Values: " + "\n" + registerValsString);

            String[] program = JOptionPane.showInputDialog("Enter the program: ").split(" ");

            // * Add 4 to the PC
            pc = addPCby4(pc);
            JOptionPane.showMessageDialog(null, "PC gets added by 4, new PC will be: " + pc);

            // * Calculate the addresses based on the PC
            for (int i = 1; i < address.length; i++) {
                address[i] = address[0] + (4 * i);
            }

            // * Get the instruction
            String instruction = program[0];
                
            // * START CPU HERE
            // ? This is where R Type starts
            if (instruction.equals("add") || instruction.equals("and") || instruction.equals("or")
                    || instruction.equals("nor") || instruction.equals("slt")) {

                int rs, rt, rd;
                
                rs = registers.get(program[2]);
                rt = registers.get(program[3]);
                rd = registers.get(program[1]);

                // * used for instruction box
                int shamt = 0, func = 0;
                op = 0;

                // * all MUXs are here
                int regDestMux, ALUSrcMux, MemtoRegMux;

                // * used for the register box
                int readData1, readData2;

                // * tracks the registers used for instruction
                int[] RegsUsed = new int[3];

                // * used for ALU
                int ALUOp, ALUOutput;

                // * Instruction Box (This is the only part that varies in R Type)

                // 000000 10110 01011 00000 111000
                switch (instruction) {
                    case "add":
                        func = 32;
                        break;
                    case "and":
                        func = 36;
                        break;
                    case "or":
                        func = 37;
                        break;
                    case "nor":
                        func = 39;
                        break;
                    case "slt":
                        func = 42;
                        break;
                    default:
                        break;
                }

                // * instruction Box
                instructionBin = instructionBinRType(op, rs, rt, rd, shamt, func);
                // * In the first address is where the 32 bits is saved
                addressValues32Bin[indexCounter] = instructionBin;

                JOptionPane.showMessageDialog(null, "The read address of the instruction: " + address[indexCounter]);
                JOptionPane.showMessageDialog(null, "The 32 bits binary of the instruction: " + instructionBin);
                indexCounter++;

                // * get the control signals
                controlSignals = getControlOutput(instructionBin);

                // * First MUX
                regDestMux = getOutputMux(controlSignals.get("regDest"), rt, rd);
                JOptionPane.showMessageDialog(null, "Value of RegDst is " + controlSignals.get("regDest") + "\n"
                        + "The register ID output of RegDst MUX is " + regDestMux);

                // * Registers Box(ReadData)
                RegsUsed = Reg(rs, rt, rd, 0, 0); // This line reads the registers (inputs are IDs)
                JOptionPane.showMessageDialog(null, "Read reg 1 is ID: " + RegsUsed[0] + "\n" + "Read reg 2 is ID: "
                        + RegsUsed[1] + "\n" + "Write reg is ID: " + RegsUsed[2]);

                // * Content of the registers will be read here
                readData1 = (RegsUsed[0] == 0) ? ZERO_REG : registerVals.get(RegsUsed[0]);
                readData2 = (RegsUsed[1] == 0) ? ZERO_REG : registerVals.get(RegsUsed[1]);

                JOptionPane.showMessageDialog(null, "The data in reg1: " + readData1 + "\n" + "The data in reg2: "
                        + readData2);

                // * Second MUX
                ALUSrcMux = getOutputMux(controlSignals.get("ALUSrc"), readData2, 0);
                JOptionPane.showMessageDialog(null, "Value of ALUSrc is " + controlSignals.get("ALUSrc") + "\n"
                        + "The output of ALUSrc MUX is " + ALUSrcMux);

                // * ALU Box
                ALUOp = controlSignals.get("ALUOp");
                 ALUOutput= getOutputALU(ALUOp, readData1, readData2);
                JOptionPane.showMessageDialog(null, "ALUOp is " + ALUOp + ", which means '" + program[0] + "'" + "\n"
                        + "Value of the ALUOutput is " + ALUOutput);

                // * Third MUX
                MemtoRegMux = controlSignals.get("MemtoReg");
                JOptionPane.showMessageDialog(null, "Value of MemtoReg MUX is " + MemtoRegMux);

                // * Write in the WriteReg
                RegsUsed = Reg(rs, rt, rd, ALUOutput, 1);
                registerVals.put(rd, RegsUsed[2]);

                JOptionPane.showMessageDialog(null,
                        "The value written in register with ID " + rd + " is " + registerVals.get(rd));

            }
            // ? This is where I Type starts
            else if (instruction.equals("addi") || instruction.equals("andi") || instruction.equals("ori")) {
                // * all MUXs are here
                int regDestMux, ALUSrcMux, MemtoRegMux;

                // * used for the register box
                int readData1;

                // * tracks the registers used for instruction
                int[] RegsUsed = new int[3];

                // * used for ALU
                int ALUOp, ALUOutput;

                op = 0;
                int rs = registers.get(program[2]);
                int rt = registers.get(program[1]);
                int rd = 0;
                int constant = Integer.parseInt(program[3]);

                switch (instruction) {
                    case "addi":
                        op = 8;
                        break;
                    case "andi":
                        op = 12;
                        break;
                    case "ori":
                        op = 13;
                        break;
                    default:
                        break;
                }

                // * Instruction Box
                instructionBin = instructionBinIType(op, rs, rt, constant);
                // * In the first address is where the 32 bits is saved
                addressValues32Bin[indexCounter] = instructionBin;

                JOptionPane.showMessageDialog(null, "The read address of the instruction: " + address[indexCounter]);
                JOptionPane.showMessageDialog(null, "The 32 bits binary of the instruction: " + instructionBin);
                indexCounter++;

                // * get the control signals
                controlSignals = getControlOutput(instructionBin);

                // * First MUX
                regDestMux = getOutputMux(controlSignals.get("regDest"), rt, rd);
                JOptionPane.showMessageDialog(null, "Value of RegDst is " + controlSignals.get("regDest") + "\n"
                        + "The output of RegDst MUX is the register ID " + regDestMux);

                // * Registers Box(ReadData)
                // * This line reads the registers (inputs are IDs)
                RegsUsed = Reg(rs, 0, rt, 0, 0);
                JOptionPane.showMessageDialog(null,
                        "Read reg 1 is ID: " + RegsUsed[0] + "\n" + "Write reg is ID: " + RegsUsed[2]);

                // *Content of the registers will be read here
                readData1 = (RegsUsed[0] == 0) ? ZERO_REG : registerVals.get(RegsUsed[0]);
                JOptionPane.showMessageDialog(null, "The data in reg1: " + readData1);

                // * sign extend
                String signExtension = String.format("%016d", Integer.parseInt(Integer.toBinaryString(constant)));
                JOptionPane.showMessageDialog(null, "The constant after sign extension: " + signExtension);

                // * Second MUX
                ALUSrcMux = getOutputMux(controlSignals.get("ALUSrc"), 0, constant);
                JOptionPane.showMessageDialog(null, "Value of ALUSrc is " + controlSignals.get("ALUSrc") + "\n"
                        + "The output of ALUSrc MUX is " + ALUSrcMux);

                // * ALU Box
                ALUOp = controlSignals.get("ALUOp");
                ALUOutput = getOutputALU(ALUOp, readData1, constant);
                JOptionPane.showMessageDialog(null, "ALUOp is " + ALUOp + ", which means '" + program[0] + "'" + "\n"
                        + "Value of the ALUOutput is " + ALUOutput);

                // * Third MUX
                MemtoRegMux = controlSignals.get("MemtoReg");
                JOptionPane.showMessageDialog(null, "Value of MemtoReg MUX is " + MemtoRegMux);

                // * Write in the WriteReg
                RegsUsed = Reg(rs, 0, rt, ALUOutput, 1);
                registerVals.put(rt, RegsUsed[2]);
                JOptionPane.showMessageDialog(null,
                        "The value written in register with ID " + rt + " is " + registerVals.get(rt));

            }
            // ? lw and sw instructions
            else if (instruction.equals("lw") || instruction.equals("sw")) {

                // * used for the register box
                int rt = registers.get(program[1]);
                int rs = registers.get(program[3]);
                int constant = Integer.parseInt(program[2]);

                // * all MUXs are here
                int regDestMux, ALUSrcMux, MemtoRegMux;

                // * tracks the registers used for instruction
                int[] RegsUsed = new int[3];

                int readData1;
                boolean TorF = false;

                op = (instruction.equals("lw")) ? 35 : 43;

                // * instruction box
                instructionBin = instructionBinIType(op, rs, rt, constant);
                addressValues32Bin[indexCounter] = instructionBin;

                JOptionPane.showMessageDialog(null, "The read address of the instruction: " + address[indexCounter]);
                JOptionPane.showMessageDialog(null, "The 32 bits binary of the instruction: " + instructionBin);
                indexCounter++;

                // * getting the control signals
                controlSignals = getControlOutput(instructionBin);

                // * First MUX
                regDestMux = getOutputMux(controlSignals.get("regDest"), rt, 0);
                JOptionPane.showMessageDialog(null, "Value of RegDst is " + controlSignals.get("regDest") + "\n"
                        + "The register ID output of RegDst MUX is " + regDestMux);

                // * Registers Box(ReadData)
                // * This line reads the registers (inputs are IDs)
                RegsUsed = Reg(rs, 0, rt, 0, 0);
                JOptionPane.showMessageDialog(null,
                        "Read reg 1 is ID: " + RegsUsed[0] + "\n"
                                + (instruction.equals("lw") ? "Write reg is ID: " : "Read reg 2 is ID: ")
                                + RegsUsed[2]);

                // * Content of the registers will be read here
                readData1 = (instruction.equals("lw")) ? registerVals.get(RegsUsed[0]) : registerVals.get(RegsUsed[2]);

                JOptionPane.showMessageDialog(null,
                        (instruction.equals("lw") ? "The data in ReadReg1: "
                                : "The data in ReadReg1 to be stored in memory: ")
                                + readData1);

                // * sign extend
                String signExtension = String.format("%016d", Integer.parseInt(Integer.toBinaryString(constant)));
                JOptionPane.showMessageDialog(null, "The constant after sign extension: " + signExtension);

                // * Second MUX
                ALUSrcMux = getOutputMux(controlSignals.get("ALUSrc"), 0, constant);
                JOptionPane.showMessageDialog(null, "Value of ALUSrc is " + controlSignals.get("ALUSrc") + "\n"
                        + "The output of ALUSrc MUX is " + ALUSrcMux);

                // * checks where register is in memory
                String usedReg = instruction.equals("lw") ? program[3] : program[3];
                for (int i = (addressValues32Bin.length - 1); i >= 0; i--) {
                    if (addressValues32Bin[i] == null || address[i] == (pc - 4)
                            || addressValues32Bin[i].length() != 32) {
                        continue;
                    }
                    // * checks if the instruction is r type or i type
                    boolean isRType = addressValues32Bin[i].substring(0, 6).equals("000000");
                    boolean isIType = !isRType && (!addressValues32Bin[i].substring(0, 6).equals("000010")
                            && !addressValues32Bin[i].substring(0, 6).equals("000011")
                            && !addressValues32Bin[i].substring(0, 6).equals("001000"));

                    // * we check where the last time t3 was used as destination and take the
                    // * address of that instant
                    if ((isRType && addressValues32Bin[i].substring(16, 21).equals(String.format("%05d",
                            Integer.parseInt(Integer.toBinaryString(registers.get(usedReg))))))
                            || (isIType && addressValues32Bin[i].substring(11, 16).equals(String.format("%05d",
                                    Integer.parseInt(Integer.toBinaryString(registers.get(usedReg))))))) {
                        indexCounter = i;
                        TorF = true;
                        break;
                    }
                }

                if (!TorF) {
                    JOptionPane.showMessageDialog(null, "DOES NOT EXIST IN MEMORY...");
                    System.exit(0);
                }

                int rsAddress = address[indexCounter];

                JOptionPane.showMessageDialog(null,
                        "The address of register " + usedReg + " in memory is address " + rsAddress);

                // * adding the offset to the address of rs
                int offset = constant / 4;
                indexCounter += offset;

                if (instruction.equals("lw")) {
                    JOptionPane.showMessageDialog(null,
                            "The value in address " + rsAddress + " plus the offset " + constant
                                    + " is: " + addressOutputs[indexCounter]);

                    // * Third and final MUX
                    MemtoRegMux = controlSignals.get("MemtoReg");
                    JOptionPane.showMessageDialog(null, "Value of MemtoReg MUX is " + MemtoRegMux);

                    // * Write in the WriteReg
                    RegsUsed = Reg(rs, 0, rt, addressOutputs[indexCounter], 1);
                    registerVals.put(rt, RegsUsed[2]);

                    JOptionPane.showMessageDialog(null,
                            "The value written in register with ID " + rt + " is: " + registerVals.get(rt));
                } else if (instruction.equals("sw")) {
                    JOptionPane.showMessageDialog(null, "The value in register " + rt + " is: " + registerVals.get(rt));

                    // * Write in the memory location
                    addressOutputs[indexCounter] = readData1;

                    JOptionPane.showMessageDialog(null,
                            "The value written in memory location " + rsAddress + " plus offset " + constant + " is: "
                                    + registerVals.get(rt));
                }

            }
            // ? This is where j Type starts
            else if (instruction.equals("j") || instruction.equals("jal") || instruction.equals("jr")) {
                op = 0;
                int jAddress = 0;

                switch (instruction) {
                    case "j":
                        op = 2;
                        break;
                    case "jal":
                        op = 3;
                        registerVals.put(31, pc); // to save the return address

                        break;
                    case "jr":
                        // if the instruction is jr, then the address is in a register
                        String regName = program[1];
                        int regKey = registers.get(regName);
                        jAddress = registerVals.get(regKey);
                        op = 8;

                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Invalid instruction");
                        break;
                }

                // * if the instruction is not jr, then the address is in the instruction
                if (op != 8) {
                    String jLabel = program[1];
                    jAddress = Integer
                            .parseInt(JOptionPane
                                    .showInputDialog("Enter the address you want to jump to (" + jLabel + "): "));
                }

                // * convert to word address (4 bytes)
                jAddress = jAddress / 4;

                instructionBin = instructionBinJType(op, jAddress);
                // * In the first address is where the 32 bits is saved
                addressValues32Bin[indexCounter] = instructionBin;

                JOptionPane.showMessageDialog(null, "The 32 bits binary of the instruction: " + instructionBin);
                indexCounter = jAddress - startingAddress / 4;

                // * sign extend
                String signExtension = String.format("%016d", Integer.parseInt(Integer.toBinaryString(jAddress)));
                JOptionPane.showMessageDialog(null, "The address after sign extension: " + signExtension);

                // * shift left 2
                String shiftLeft2 = signExtension.substring(2) + "00";
                JOptionPane.showMessageDialog(null, "The address after shift left 2: " + shiftLeft2);

                // * target adder
                pc = Integer.parseInt(shiftLeft2, 2);
                JOptionPane.showMessageDialog(null, "the new program counter (PC) is: " + pc);

                // * if the instruction is jal, then the return address is saved in register
                if (op == 3) {
                    JOptionPane.showMessageDialog(null,
                            "The value of the return register ($ra) is : " + registerVals.get(31));
                }
            }
            // ? This is where Branch Type starts
            else if (instruction.equals("beq")) {
                // * all MUXs are here
                int regDestMux, ALUSrcMux;
                // * used for the register box
                int readData1, readData2;
                // * tracks the registers used for instruction
                int[] RegsUsed = new int[3];
                // * used for ALU
                int ALUOp, ALUOutput;
                // * zero flag
                boolean zeroFlag = false;

                int rs = registers.get(program[2]);
                int rt = registers.get(program[1]);
                int rd = 0;
                String offsetLabel = program[3];
                op = 4;
                int offset = Integer
                        .parseInt(JOptionPane
                                .showInputDialog("Enter the offset you want to branch to (" + offsetLabel + "): "));

                // * get the instruction binary
                instructionBin = instructionBinIType(op, rs, rt, offset);
                addressValues32Bin[indexCounter] = instructionBin;

                JOptionPane.showMessageDialog(null, "The 32 bits binary of the instruction: " + instructionBin);
                indexCounter++;

                // * get the control signals
                controlSignals = getControlOutput(instructionBin);

                // * First MUX
                regDestMux = getOutputMux(controlSignals.get("regDest"), rt, rd);
                JOptionPane.showMessageDialog(null, "Value of RegDst is " + controlSignals.get("regDest") + "\n"
                        + "The register ID output of RegDst MUX is " + regDestMux);

                // * Registers Box(ReadData)
                RegsUsed = Reg(rs, rt, 0, 0, 0); // This line reads the registers (inputs are IDs)
                JOptionPane.showMessageDialog(null,
                        "Read reg 1 is ID: " + RegsUsed[0] + "\n" + "Read reg 2 is ID: " + RegsUsed[1]);

                // *Content of the registers will be read here
                readData1 = (RegsUsed[0] == 0) ? ZERO_REG : registerVals.get(RegsUsed[0]);
                readData2 = (RegsUsed[1] == 0) ? ZERO_REG : registerVals.get(RegsUsed[1]);

                JOptionPane.showMessageDialog(null, "The data in reg1: " + readData1 + "\n" + "The data in reg2: "
                        + readData2);

                // * sign extend
                String signExtension = String.format("%016d", Integer.parseInt(Integer.toBinaryString(offset)));
                JOptionPane.showMessageDialog(null, "The offset after sign extension: " + signExtension);

                // * shift left 2
                String shiftLeft2 = signExtension.substring(2) + "00";
                JOptionPane.showMessageDialog(null, "The offset after shift left 2: " + shiftLeft2);

                // * Second MUX
                ALUSrcMux = getOutputMux(controlSignals.get("ALUSrc"), readData2, offset);
                JOptionPane.showMessageDialog(null, "Value of ALUSrc is " + controlSignals.get("ALUSrc") + "\n"
                        + "The output of ALUSrc MUX is " + ALUSrcMux);

                // * ALU Box
                ALUOp = controlSignals.get("ALUOp");
                ALUOutput = getOutputALU(ALUOp, readData1, ALUSrcMux);
                JOptionPane.showMessageDialog(null, "ALUOp is " + ALUOp + ", which means '" + program[0] + "'" + "\n"
                        + "Value of the ALUOutput is " + ALUOutput);

                // * check zero flag
                if (ALUOutput == 0) {
                    zeroFlag = true;
                }
                JOptionPane.showMessageDialog(null, "The zero flag is " + zeroFlag);

                // * target adder
                int targetAdderPC = pc + Integer.parseInt(shiftLeft2, 2);
                JOptionPane.showMessageDialog(null, "The target address is: " + targetAdderPC);

                // * target adder MUX
                if (zeroFlag) {
                    pc = getOutputMux(controlSignals.get("Branch"), pc, targetAdderPC);
                }
                JOptionPane.showMessageDialog(null, "The new program counter (PC) is: " + pc);
            }
            flag = JOptionPane.showInputDialog("Do you want to continue? (y/n)").charAt(0);
        }
        String registerValsString = "";
        for (Map.Entry<Integer, Integer> entry : registerVals.entrySet()) {
            registerValsString += entry.getKey() + " : " + entry.getValue() + "\n";
        }
        JOptionPane.showMessageDialog(null, "Register Values: " + "\n" + registerValsString);
        JOptionPane.showMessageDialog(null, "The clock cycle is: " + clockCycle);
    }

    public void initializedControlSignals() {
        controlSignals = new HashMap<>();
        controlSignals.put("RegDest", 0);
        controlSignals.put("Branch", 0);
        controlSignals.put("MemRead", 0);
        controlSignals.put("MemtoReg", 0);
        controlSignals.put("MemWrite", 0);
        controlSignals.put("ALUSrc", 0);
        controlSignals.put("RegWrite", 0);
    }

    public void registersValue() {
        registerVals = new HashMap<>();
        registerVals.put(1, 0);
        registerVals.put(2, 0);
        registerVals.put(3, 0);
        registerVals.put(4, 0);
        registerVals.put(5, 0);
        registerVals.put(6, 0);
        registerVals.put(7, 0);
        registerVals.put(8, 8);
        registerVals.put(9, 9);
        registerVals.put(10, 300);
        registerVals.put(11, 11);
        registerVals.put(12, 0);
        registerVals.put(13, 13);
        registerVals.put(14, 0);
        registerVals.put(15, 0);
        registerVals.put(16, 0);
        registerVals.put(17, 0);
        registerVals.put(18, 0);
        registerVals.put(19, 0);
        registerVals.put(20, 0);
        registerVals.put(21, 0);
        registerVals.put(22, 0);
        registerVals.put(23, 0);
        registerVals.put(24, 0);
        registerVals.put(25, 0);
        registerVals.put(26, 0);
        registerVals.put(27, 0);
        registerVals.put(28, 0);
        registerVals.put(29, 0);
        registerVals.put(30, 0);
        registerVals.put(31, 400);
    }
}