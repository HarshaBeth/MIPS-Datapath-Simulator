
interface Instruction {

    default String instructionBinRType(int op, int rs, int rt, int rd, int shamt, int func) {
        String shamtAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(shamt)));
        String rdAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(rd)));
        String rsAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(rs)));
        String rtAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(rt)));
        String funcAfterZeros = String.format("%06d", Integer.parseInt(Integer.toBinaryString(func)));
        String opAfterZeros = String.format("%06d", Integer.parseInt(Integer.toBinaryString(op)));

        return opAfterZeros + rsAfterZeros + rtAfterZeros +
                rdAfterZeros + shamtAfterZeros + funcAfterZeros;
    }

    default String instructionBinIType(int op, int rs, int rt, int constant) {
        String constantAfterZeros = String.format("%016d", Integer.parseInt(Integer.toBinaryString(constant)));
        String rsAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(rs)));
        String rtAfterZeros = String.format("%05d", Integer.parseInt(Integer.toBinaryString(rt)));
        String opAfterZeros = String.format("%06d", Integer.parseInt(Integer.toBinaryString(op)));
        return opAfterZeros + rsAfterZeros + rtAfterZeros + constantAfterZeros;
    }

    default String instructionBinJType(int op, int address) {
        String opAfterZeros = String.format("%06d", Integer.parseInt(Integer.toBinaryString(op)));
        String addressAfterZeros = String.format("%026d", Integer.parseInt(Integer.toBinaryString(address)));
        return opAfterZeros + addressAfterZeros;
    }
}