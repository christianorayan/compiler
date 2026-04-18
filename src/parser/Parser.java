package parser;

import java.util.*;
import ir.Instruction;

public class Parser {

    public static List<Instruction> parse(List<String> lines) {
        List<Instruction> program = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.endsWith(":")) {
                program.add(new Instruction(line, null, null, null));
            } else if (line.startsWith("goto ")) {
                String target = line.substring(5).trim();
                program.add(new Instruction(null, target, null, "goto"));
            } else if (line.startsWith("if ")) {
                // "if x goto L1"
                String[] parts = line.split(" ");
                String cond = parts[1];
                String target = parts[3];
                program.add(new Instruction(null, cond, target, "ifgoto"));
            } else if (line.startsWith("return")) {
                String val = null;
                if (line.length() > 6) {
                    val = line.substring(6).trim();
                }
                program.add(new Instruction("return", val, null, null));
            } else if (line.contains("=")) {
                String[] parts = line.split("=");
                String result = parts[0].trim();
                String expr = parts[1].trim();

                String op = null;
                if (expr.contains("+")) op = "\\+";
                else if (expr.contains("-")) op = "-";
                else if (expr.contains("*")) op = "\\*";
                else if (expr.contains("/")) op = "/";

                if (op != null) {
                    String[] exprParts = expr.split(op);
                    String actualOp = op.replace("\\", "");
                    program.add(new Instruction(result, exprParts[0].trim(), exprParts[1].trim(), actualOp));
                } else {
                    program.add(new Instruction(result, expr, null, null));
                }
            }
        }

        return program;
    }
}