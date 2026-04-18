package ir;

import java.util.*;

public class CFG {
    public List<BasicBlock> blocks = new ArrayList<>();
    public BasicBlock entry;

    public static CFG build(List<Instruction> program) {
        CFG cfg = new CFG();
        if (program.isEmpty()) return cfg;

        Set<Integer> leaders = new HashSet<>();
        leaders.add(0);

        Map<String, Integer> labelToIndex = new HashMap<>();
        for (int i = 0; i < program.size(); i++) {
            Instruction inst = program.get(i);
            if (inst.isLabel) {
                leaders.add(i);
                labelToIndex.put(inst.result, i);
            }
            if (inst.isGoto || inst.isIfGoto || inst.isReturn) {
                if (i + 1 < program.size()) {
                    leaders.add(i + 1);
                }
            }
        }

        for (int i = 0; i < program.size(); i++) {
            Instruction inst = program.get(i);
            if (inst.isGoto || inst.isIfGoto) {
                String target = inst.isGoto ? inst.arg1 : inst.arg2;
                if (labelToIndex.containsKey(target)) {
                    leaders.add(labelToIndex.get(target));
                }
            }
        }

        List<Integer> leaderList = new ArrayList<>(leaders);
        Collections.sort(leaderList);

        Map<Integer, BasicBlock> startToBlock = new HashMap<>();
        int blockId = 1;

        for (int i = 0; i < leaderList.size(); i++) {
            int start = leaderList.get(i);
            int end = (i == leaderList.size() - 1) ? program.size() : leaderList.get(i + 1);

            BasicBlock b = new BasicBlock(blockId++);
            for (int j = start; j < end; j++) {
                b.instructions.add(program.get(j));
            }
            b.computeUseDef();
            cfg.blocks.add(b);
            startToBlock.put(start, b);
        }

        if (!cfg.blocks.isEmpty()) cfg.entry = cfg.blocks.get(0);

        for (int i = 0; i < leaderList.size(); i++) {
            int start = leaderList.get(i);
            BasicBlock b = startToBlock.get(start);
            if (b.instructions.isEmpty()) continue;

            Instruction last = b.instructions.get(b.instructions.size() - 1);

            if (!last.isGoto && !last.isReturn) {
                if (i + 1 < leaderList.size()) {
                    BasicBlock next = startToBlock.get(leaderList.get(i + 1));
                    b.succs.add(next);
                    next.preds.add(b);
                }
            }

            if (last.isGoto || last.isIfGoto) {
                String target = last.isGoto ? last.arg1 : last.arg2;
                if (labelToIndex.containsKey(target)) {
                    BasicBlock targetBlock = startToBlock.get(labelToIndex.get(target));
                    if (targetBlock != null && !b.succs.contains(targetBlock)) {
                        b.succs.add(targetBlock);
                        targetBlock.preds.add(b);
                    }
                }
            }
        }

        return cfg;
    }
    
    public void print() {
        for (BasicBlock block : blocks) {
            System.out.println(block);
        }
    }
}
