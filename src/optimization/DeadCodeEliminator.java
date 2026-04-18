package optimization;

import java.util.*;
import ir.CFG;
import ir.BasicBlock;
import ir.Instruction;
import analysis.Liveness;

public class DeadCodeEliminator {

    public static boolean eliminate(CFG cfg) {
        Map<Instruction, Set<String>> instOut = Liveness.analyze(cfg);
        boolean removed = false;

        for (BasicBlock block : cfg.blocks) {
            for (int i = block.instructions.size() - 1; i >= 0; i--) {
                Instruction inst = block.instructions.get(i);

                if (inst.result != null && !inst.isLabel && !inst.isReturn) {
                    Set<String> out = instOut.get(inst);

                    if (out != null && !out.contains(inst.result)) {
                        block.instructions.remove(i);
                        removed = true;
                    }
                }
            }
        }

        return removed;
    }
}