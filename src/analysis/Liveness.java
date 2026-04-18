package analysis;

import java.util.*;
import ir.CFG;
import ir.BasicBlock;
import ir.Instruction;

public class Liveness {

    // Returns a map of Instruction -> its OUT liveness set
    public static Map<Instruction, Set<String>> analyze(CFG cfg) {
        boolean changed;

        for (BasicBlock block : cfg.blocks) {
            block.in.clear();
            block.out.clear();
        }

        do {
            changed = false;

            for (int i = cfg.blocks.size() - 1; i >= 0; i--) {
                BasicBlock block = cfg.blocks.get(i);

                Set<String> newOut = new HashSet<>();
                for (BasicBlock succ : block.succs) {
                    newOut.addAll(succ.in);
                }
                block.out = newOut;

                Set<String> newIn = new HashSet<>(block.use);
                Set<String> temp = new HashSet<>(block.out);
                temp.removeAll(block.def);
                newIn.addAll(temp);

                if (!block.in.equals(newIn)) {
                    block.in = newIn;
                    changed = true;
                }
            }
        } while (changed);

        Map<Instruction, Set<String>> instOut = new HashMap<>();

        for (BasicBlock block : cfg.blocks) {
            Set<String> currentOut = new HashSet<>(block.out);
            for (int i = block.instructions.size() - 1; i >= 0; i--) {
                Instruction inst = block.instructions.get(i);
                instOut.put(inst, new HashSet<>(currentOut));

                Set<String> currentIn = new HashSet<>(inst.use);
                Set<String> temp = new HashSet<>(currentOut);
                temp.removeAll(inst.def);
                currentIn.addAll(temp);
                
                currentOut = currentIn;
            }
        }

        return instOut;
    }
}