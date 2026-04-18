package main;

import java.util.*;
import java.util.stream.Collectors;
import parser.Parser;
import optimization.Optimizer;
import ir.Instruction;
import ir.CFG;
import ir.BasicBlock;

public class WebMain {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> input = new ArrayList<>();
        while (scanner.hasNextLine()) {
            input.add(scanner.nextLine());
        }
        scanner.close();

        if (input.isEmpty()) {
            System.out.println("{}");
            return;
        }

        try {
            List<Instruction> program = parser.PolyglotParser.parse(input);
            List<Instruction> optimized = Optimizer.optimize(program);

            CFG initialCfg = CFG.build(program);
            analysis.Liveness.analyze(initialCfg);

            CFG finalCfg = CFG.build(optimized);
            analysis.Liveness.analyze(finalCfg);

            // Output JSON
            System.out.println("{");
            System.out.println("  \"original\": " + toJson(program) + ",");
            System.out.println("  \"optimized\": " + toJson(optimized) + ",");
            System.out.println("  \"initialCfg\": " + cfgToJson(initialCfg) + ",");
            System.out.println("  \"finalCfg\": " + cfgToJson(finalCfg));
            System.out.println("}");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    private static String toJson(List<Instruction> instructions) {
        return "[" + instructions.stream()
                .map(inst -> "\"" + inst.toString().replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(", ")) + "]";
    }

    private static String cfgToJson(CFG cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"blocks\": [");
        for (int i = 0; i < cfg.blocks.size(); i++) {
            BasicBlock b = cfg.blocks.get(i);
            sb.append("{");
            sb.append("\"id\": ").append(b.id).append(",");
            sb.append("\"instructions\": ").append(toJson(b.instructions)).append(",");
            sb.append("\"preds\": [" + b.preds.stream().map(p -> String.valueOf(p.id)).collect(Collectors.joining(", ")) + "],");
            sb.append("\"succs\": [" + b.succs.stream().map(p -> String.valueOf(p.id)).collect(Collectors.joining(", ")) + "],");
            sb.append("\"in\": [" + b.in.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")) + "],");
            sb.append("\"out\": [" + b.out.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")) + "]");
            sb.append("}");
            if (i < cfg.blocks.size() - 1) sb.append(",");
        }
        sb.append("] }");
        return sb.toString();
    }
}
