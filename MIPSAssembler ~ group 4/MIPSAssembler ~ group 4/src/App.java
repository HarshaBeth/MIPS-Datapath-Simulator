public class App {
    public static void main(String[] args) throws Exception {
        // add $t0 $t1 $t2
        // beq $t0 $t1 exit
        // lw $t3 0 $t5
        // rt const rs
        MIPSAssembler assembler = new MIPSAssembler();
            assembler.run();
    }
}
