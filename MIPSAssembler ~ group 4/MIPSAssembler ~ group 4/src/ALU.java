interface ALU {

    default int getOutputALU(int ALUOp, int input1, int input2) {
        switch (ALUOp) {
            case 0: // AND (000)
                return input1 & input2;
            case 1: // OR (001)
                return input1 | input2;
            case 2: // ADD (010)
                return input1 + input2;
            case 6: // SUBTRACT (110)
                return input1 - input2;
            case 7: // SLT (111)
                return (input1 < input2) ? 1 : 0;
            case 12: // NOR (1100)
                String afterOr = Integer.toBinaryString(input1 | input2);
                String nor = "";

                
                // first we do or, then we do not of the or, in the end it becomes nor
                
                for (int i = 0; i < afterOr.length(); i++) {
                    if (afterOr.charAt(i) == '1') {
                        nor += "0";
                    } else {
                        nor += "1";
                    }
                }

                return Integer.parseInt(nor);
            default:
                return 0;
        }
    }
}
