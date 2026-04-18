package parser;

import java.util.*;
import ir.Instruction;

public class PolyglotParser {

    public static List<Instruction> parse(List<String> lines) {
        List<Instruction> program = new ArrayList<>();
        boolean isPython = detectPython(lines);

        if (isPython) {
            return parsePython(lines);
        } else {
            return parseCStyle(lines);
        }
    }

    private static boolean detectPython(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.endsWith(":") && (trimmed.startsWith("if ") || trimmed.startsWith("def ") || trimmed.startsWith("while "))) {
                return true;
            }
        }
        return false;
    }

    private static List<Instruction> parseCStyle(List<String> lines) {
        List<Instruction> program = new ArrayList<>();
        int labelCounter = 1;

        for (String line : lines) {
            line = line.trim();
            // Remove comments and noise
            line = line.split("//")[0].split("#")[0].trim();
            if (line.isEmpty() || line.equals("{") || line.equals("}")) continue;
            
            // Remove type keywords
            line = line.replaceAll("^(int|float|double|var|let|const|String|boolean)\\s+", "");
            // Remove trailing semicolon
            if (line.endsWith(";")) line = line.substring(0, line.length() - 1).trim();

            if (line.endsWith(":")) {
                program.add(new Instruction(line, null, null, null));
            } else if (line.startsWith("goto ")) {
                String target = line.substring(5).trim();
                program.add(new Instruction(null, target, null, "goto"));
            } else if (line.startsWith("if ")) {
                // Handle "if (cond) { goto L1; }" or "if (cond) goto L1;"
                String content = line.substring(3).trim();
                if (content.startsWith("(")) {
                    int endParen = content.indexOf(")");
                    if (endParen != -1) {
                        String cond = content.substring(1, endParen).trim();
                        String rest = content.substring(endParen + 1).trim();
                        if (rest.startsWith("goto ")) {
                            String target = rest.substring(5).trim();
                            program.add(new Instruction(null, cond, target, "ifgoto"));
                        } else {
                            // Simple heuristic for "if cond goto"
                            program.add(new Instruction(null, cond, rest, "ifgoto"));
                        }
                    }
                } else {
                    // Fallback to previous parser logic
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4 && parts[2].equals("goto")) {
                        program.add(new Instruction(null, parts[1], parts[3], "ifgoto"));
                    }
                }
            } else if (line.startsWith("return")) {
                String val = line.length() > 6 ? line.substring(6).trim() : null;
                program.add(new Instruction("return", val, null, null));
            } else if (line.contains("=")) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String result = parts[0].trim();
                    String expr = parts[1].trim();
                    program.addAll(parseExpression(result, expr));
                }
            }
        }
        return program;
    }

    private static List<Instruction> parsePython(List<String> lines) {
        // Simple Python mapping: flat it for now but handle colons
        List<String> normalizedLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            
            // Translate Pythonisms to IR-like syntax
            if (trimmed.startsWith("def ")) continue; // Skip function dec
            if (trimmed.endsWith(":")) trimmed = trimmed.substring(0, trimmed.length() - 1);
            
            normalizedLines.add(trimmed);
        }
        return parseCStyle(normalizedLines);
    }

    private static List<Instruction> parseExpression(String result, String expr) {
        List<Instruction> instructions = new ArrayList<>();
        String op = null;
        if (expr.contains("+")) op = "\\+";
        else if (expr.contains("-")) op = "-";
        else if (expr.contains("*")) op = "\\*";
        else if (expr.contains("/")) op = "/";

        if (op != null) {
            String[] exprParts = expr.split(op);
            String actualOp = op.replace("\\", "");
            if (exprParts.length == 2) {
                instructions.add(new Instruction(result, exprParts[0].trim(), exprParts[1].trim(), actualOp));
            }
        } else {
            instructions.add(new Instruction(result, expr, null, null));
        }
        return instructions;
    }
}
