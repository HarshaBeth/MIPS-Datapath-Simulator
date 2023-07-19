interface Adder {
    default int addPCby4(int pc) {
        pc += 4;
        return pc;
    }

    default int addPCShift2(int pc, int shiftLeft2) {

        pc += shiftLeft2;

        return pc;
    }
}
