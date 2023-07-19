interface Multiplexer {
    
    default int getOutputMux(int flag, int input1, int input2) {
        return flag == 0 ? input1 : input2;
    }
}
