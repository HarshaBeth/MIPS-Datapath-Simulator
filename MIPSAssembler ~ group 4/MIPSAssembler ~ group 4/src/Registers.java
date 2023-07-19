
interface Registers {
	// Regwrite is the register, writeData is the data
	default int[] Reg(int readReg1, int readReg2, int RegWrite, int writeData, int flag) {
							
		int[] registers = new int[3];

		if (flag == 0) {	//reading
			registers[0] = readReg1;
			registers[1] = readReg2;
			registers[2] = RegWrite;
		} else {			//write
			RegWrite = writeData;
			registers[2] = RegWrite;
		}

		return registers;
	}
}
