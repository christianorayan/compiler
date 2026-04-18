package optimization;
import java.util.*;
import ir.CFG;
import ir.BasicBlock;
import ir.Instruction;

public class Optimizer {

    public static List<Instruction> optimize(List<Instruction> program) {

        boolean changed;

        do {
            changed = false;

            changed |= ConstantFolding.optimize(program);
            
            CFG cfg = CFG.build(program);
            boolean dceChanged = DeadCodeEliminator.eliminate(cfg);
            boolean uceChanged = UnreachableCodeEliminator.eliminate(cfg);

            if (dceChanged || uceChanged) {
                program = flatten(cfg);
                changed = true;
            }

        } while (changed);
        
        return program;
    }

    private static List<Instruction> flatten(CFG cfg) {
        List<Instruction> flat = new ArrayList<>();
        for(BasicBlock b : cfg.blocks) {
            flat.addAll(b.instructions);
        }
        return flat;
    }
}