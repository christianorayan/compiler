package main;

import java.util.*;
import parser.Parser;
import optimization.Optimizer;
import ir.Instruction;
import ir.CFG;

public class Main {

    public static void main(String[] args) {

        List<String> input = List.of(
                "x = 10",
                "y = 20",
                "L1:",            // Loop start
                "z = x + y",      // Used later
                "a = 5",          // DEAD
                "b = z * 2",      // DEAD
                "if x goto L2",   // Branch
                "goto L3",
                "L2:",
                "x = x - 1",
                "goto L1",        // Back edge
                "L3:",            
                "return z",
                "L4:",            // UNREACHABLE
                "w = 100",
                "return w"
        );

        List<Instruction> program = Parser.parse(input);

        System.out.println("===== ORIGINAL CODE =====");
        program.forEach(System.out::println);

        System.out.println("\n===== INITIAL CFG =====");
        CFG initialCfg = CFG.build(program);
        analysis.Liveness.analyze(initialCfg);
        initialCfg.print();

        List<Instruction> optimized = Optimizer.optimize(program);

        System.out.println("\n===== OPTIMIZED CODE =====");
        optimized.forEach(System.out::println);
        
        System.out.println("\n===== FINAL CFG =====");
        CFG finalCfg = CFG.build(optimized);
        analysis.Liveness.analyze(finalCfg);
        finalCfg.print();
    }
}