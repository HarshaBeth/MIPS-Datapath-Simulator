import java.util.HashMap;
import java.util.Map;

public interface Control {

	default Map<String, Integer> getControlOutput(String instruction) {
		String opCode = instruction.substring(0, 6);
		String func = instruction.substring(26, 32);

		Map<String, Integer> controlSignals = new HashMap<>();

		switch (opCode) {
			case "000000":
				controlSignals.put("regDest", 1);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				
				if (func.equals("100100")) { // and &
					controlSignals.put("ALUOp", 0);
				} else if (func.equals("100101")) { // or |
					controlSignals.put("ALUOp", 1);
				} else if (func.equals("100000")) { // add +
					controlSignals.put("ALUOp", 2);
				} else if (func.equals("100111")) { // nor ~
					controlSignals.put("ALUOp", 12);
				} else if (func.equals("101010")) { // slt <
					controlSignals.put("ALUOp", 7);
				}

				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 0);
				controlSignals.put("RegWrite", 1);
				break;
			case "001000": // addi
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 2);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 1);
				break;
			case "001100": // andi
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 0);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 1);
				break;
			case "001101": // ori
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 1);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 1);
				break;
			case "100011": // lw
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 1);
				controlSignals.put("MemtoReg", 1);
				controlSignals.put("ALUOp", 2);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 1);
				break;
			case "101011": // sw
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 2);
				controlSignals.put("MemWrite", 1);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 0);
				break;
			case "000100": // beq
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 1);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 6);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 0);
				controlSignals.put("RegWrite", 0);
				break;
			case "000010": // j
				controlSignals.put("regDest", 0);
				controlSignals.put("Branch", 0);
				controlSignals.put("MemRead", 0);
				controlSignals.put("MemtoReg", 0);
				controlSignals.put("ALUOp", 6);
				controlSignals.put("MemWrite", 0);
				controlSignals.put("ALUSrc", 1);
				controlSignals.put("RegWrite", 0);
				break;
			default:
				throw new IllegalArgumentException("Invalid opcode");
		}
		return controlSignals;
	}
}
