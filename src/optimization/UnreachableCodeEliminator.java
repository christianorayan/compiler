package optimization;

import java.util.*;
import ir.CFG;
import ir.BasicBlock;

public class UnreachableCodeEliminator {

    public static boolean eliminate(CFG cfg) {
        if (cfg.entry == null) return false;

        Set<BasicBlock> reachable = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();

        reachable.add(cfg.entry);
        queue.add(cfg.entry);

        while (!queue.isEmpty()) {
            BasicBlock b = queue.poll();
            for (BasicBlock succ : b.succs) {
                if (!reachable.contains(succ)) {
                    reachable.add(succ);
                    queue.add(succ);
                }
            }
        }

        boolean removed = false;
        Iterator<BasicBlock> it = cfg.blocks.iterator();
        while (it.hasNext()) {
            BasicBlock b = it.next();
            if (!reachable.contains(b)) {
                it.remove();
                removed = true;
            }
        }

        // We should also remove edges from other blocks? Unreachable blocks shouldn't have edges from reachable ones anyway.

        return removed;
    }
}
