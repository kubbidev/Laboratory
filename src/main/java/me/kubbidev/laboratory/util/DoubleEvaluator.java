package me.kubbidev.laboratory.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DoubleEvaluator {
    private static final Map<String, Double> CONSTANTS;
    static {
        Map<String, Double> constants = new HashMap<>();
        constants.put("PI", Math.PI);
        constants.put("Pi", Math.PI);

        constants.put("E", Math.E);
        constants.put("e", Math.E);

        double phi = 0.5 * (1 + Math.sqrt(5));
        constants.put("phi", phi);
        constants.put("Phi", phi);
        constants.put("PHI", phi);
        CONSTANTS = Collections.unmodifiableMap(constants);
    }

    private final String expression;
    private final Map<String, Double> variables = new HashMap<>(CONSTANTS);

    public DoubleEvaluator(String expression) {
        this.expression = expression;
    }

    public void registerVariable(String variable, double value) {
        this.variables.put(variable, value);
    }

    public void registerVariables(Map<String, Double> variables) {
        this.variables.putAll(variables);
    }

    public double eval() throws ArithmeticException {
        return eval(this.expression);
    }

    private double eval(String str) throws ArithmeticException {
        return new Object() {
            int pos = -1;
            int ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            double parse() throws ArithmeticException {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new ArithmeticException("Unexpected: " + (char) ch);
                return x;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() throws ArithmeticException {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() throws ArithmeticException {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() throws ArithmeticException {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new ArithmeticException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (Character.isLetter(ch)) { // variables or functions
                    while (Character.isLetter(ch)) nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (!variables.containsKey(func)) {
                        if (eat('(')) {
                            x = parseExpression();
                            if (!eat(')')) throw new ArithmeticException("Missing ')' after argument to " + func);
                        } else {
                            x = parseFactor();
                        }
                        x = switch (func) {
                            case "sqrt" -> Math.sqrt(x);
                            case "sin" -> Math.sin(Math.toRadians(x));
                            case "cos" -> Math.cos(Math.toRadians(x));
                            case "tan" -> Math.tan(Math.toRadians(x));
                            case "asin" -> Math.asin(Math.toRadians(x));
                            case "acos" -> Math.acos(Math.toRadians(x));
                            case "atan" -> Math.atan(Math.toRadians(x));
                            case "exp" -> Math.exp(x);
                            case "toDegrees" -> Math.toDegrees(x);
                            case "toRadians" -> Math.toRadians(x);
                            case "log" -> Math.log(x);
                            case "log10" -> Math.log10(x);
                            case "abs" -> Math.abs(x);
                            case "ceil" -> Math.ceil(x);
                            case "floor" -> Math.floor(x);
                            case "round" -> Math.round(x);
                            default -> throw new ArithmeticException("Unknown function or variable: " + func);
                        };
                    } else {
                        x = variables.get(func);
                    }
                } else {
                    throw new ArithmeticException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                return x;
            }
        }.parse();
    }
}
