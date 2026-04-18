package ir;

import java.util.*;

public class BasicBlock {
    public int id;
    public List<Instruction> instructions = new ArrayList<>();
    public List<BasicBlock> preds = new ArrayList<>();
    public List<BasicBlock> succs = new ArrayList<>();

    // For global dataflow analysis
    public Set<String> use = new HashSet<>();
    public Set<String> def = new HashSet<>();
    public Set<String> in = new HashSet<>();
    public Set<String> out = new HashSet<>();

    public BasicBlock(int id) {
        this.id = id;
    }

    public void computeUseDef() {
        use.clear();
        def.clear();
        for (Instruction inst : instructions) {
            // use = use U (inst.use - def)
            Set<String> instUse = new HashSet<>(inst.use);
            instUse.removeAll(def);
            use.addAll(instUse);

            // def = def U inst.def
            def.addAll(inst.def);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Block ").append(id).append(":\n");
        for (Instruction inst : instructions) {
            sb.append("  ").append(inst.toString()).append("\n");
        }
        sb.append("  Preds: ");
        for (BasicBlock b : preds) sb.append(b.id).append(" ");
        sb.append("\n  Succs: ");
        for (BasicBlock b : succs) sb.append(b.id).append(" ");
        sb.append("\n  In: ").append(in);
        sb.append("\n  Out: ").append(out);
        return sb.toString();
    }
}
