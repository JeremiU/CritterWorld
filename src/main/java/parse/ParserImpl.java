package parse;

import ast.*;
import console.Logger;
import exceptions.SyntaxError;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static ast.Cmd.CmdType;
import static ast.ExprSensor.SensorType;
import static parse.Token.NumToken;

public class ParserImpl implements Parser {

    /**
     * Parses a program from the stream of tokens provided by the Tokenizer,
     * consuming tokens representing the program.
     *
     * @return the created AST
     * @throws SyntaxError if the input tokens have invalid syntax
     */
    public static ProgramImpl parseProgram(Tokenizer t) throws SyntaxError {
        ProgramImpl p = new ProgramImpl();

        while (t.peek().getType() != TokenType.EOF) {
            Rule r = parseRule(t);
            consume(t, TokenType.SEMICOLON);
            p.addRule(r);
        }
        return p;
    }

	/**
	 * Parses a rule from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created rule
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Rule parseRule(Tokenizer t) throws SyntaxError {
        Condition con = parseCondition(t);

        consume(t, TokenType.ARR);

        List<Cmd> cmdList = new ArrayList<>();
        cmdList.add(parseCommand(t));

        while (t.peek().getType() != TokenType.SEMICOLON)
            cmdList.add(parseCommand(t));

        return new Rule(con, cmdList);
    }

	/**
	 * Parses a condition from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created condition
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Condition parseCondition(Tokenizer t) throws SyntaxError {
        // Condition could be a binary condition (and/or) between relations
        // or just a relation
        Condition left = parseRelation(t);
        while (t.peek().isBooleanOperation()) {
            left = new ConditionBinary(left,
                    ConditionBinary.BinCondOperator.valueOf(t.next().getType().name()),
                    parseRelation(t));
        }
        return left;
    }

	/**
	 * Parses a relation from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created relation Condition
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Condition parseRelation(Tokenizer t) throws SyntaxError {
        Condition c;
        if (t.peek().getType() == TokenType.LBRACE) {
            consume(t, TokenType.LBRACE);
            c = parseCondition(t);
            c.setBraces(true);
            consume(t, TokenType.RBRACE);
        } else {
            Expr left = parseExpression(t);
            ConditionRelation.RelOperator opr;
            opr = ConditionRelation.RelOperator.valueOf(t.next().getType().name());
            Expr right = parseExpression(t);
            c = new ConditionRelation(left, opr, right);
        }
        return c;
    }

	/**
	 * Parses an expression from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created expression
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Expr parseExpression(Tokenizer t) throws SyntaxError {
        Expr left = parseTerm(t);
        ExprBinary.BinExprOperator opr;

        while (t.peek().isAddOp()) {
            opr = ExprBinary.BinExprOperator.valueOf(t.next().getType().name());

            if (t.peek().getType() == TokenType.MINUS) parseTerm(t);
            Expr right = parseTerm(t);
            left = new ExprBinary(left, opr, right);

        }
        return left;
    }

	/**
	 * Parses a term from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created term expression
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Expr parseTerm(Tokenizer t) throws SyntaxError {
        Expr left = parseFactor(t);
        ExprBinary.BinExprOperator opr;

        while (t.peek().isMulOp()) {
            opr = ExprBinary.BinExprOperator.valueOf(t.next().getType().name());
            Expr right = parseFactor(t);
            left = new ExprBinary(left, opr, right);
        }
        return left;
    }

	/**
	 * Parses a factor, the most basic expression, from the stream of tokens
	 * provided by the Tokenizer, consuming tokens representing the program.
	 *
	 * @return the created factor expression
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Expr parseFactor(Tokenizer t) throws SyntaxError {
		if (t.peek().getType().name().contains("ABV")) return new ExprMem(new ExprNum(TokenType.getMemStr(t))); //syntactic sugar
        return switch (t.peek().getType()) {
            case NUM -> new ExprNum(((NumToken) t.next()).getValue());
            case MEM -> {
                consume(t, TokenType.MEM);
                consume(t, TokenType.LBRACKET);
                ExprMem m = new ExprMem(parseExpression(t));
                consume(t, TokenType.RBRACKET);
                yield m;
            }
            case LPAREN -> {
                consume(t, TokenType.LPAREN);
                Expr e1 = parseExpression(t);
                e1.setParentheses(true);
                consume(t, TokenType.RPAREN);
                yield e1;
            }
            case MINUS -> {
                consume(t, TokenType.MINUS);
                Expr neg = parseFactor(t);
                neg.setNegative(true);
                yield neg;
            }
            case ERROR -> {
                Logger.error("Incorrect Program", "ParserImpl:parseFactor", Logger.FLAG_PARSER);
                throw new SyntaxError(t.lineNumber(),t.peek() + " is an incorrect program");
            }
            default -> {
                ExprSensor s = new ExprSensor(SensorType.valueOf(t.next().getType().name()));

                if (s.getSensorType() != ExprSensor.SensorType.SMELL) {
                    consume(t, TokenType.LBRACKET);
                    Expr e = parseExpression(t);
                    consume(t, TokenType.RBRACKET);
                    s.setIndex(e);
                }
                yield s;
            }
        };
    }

	/**
	 * Parses a command from the stream of tokens provided by the Tokenizer,
	 * consuming tokens representing the program.
	 *
	 * @return the created command (an action or update)
	 * @throws SyntaxError if the input tokens have invalid syntax
	 */
	public static Cmd parseCommand(Tokenizer t) throws SyntaxError {
        Cmd c;

        if (t.peek().getType() == TokenType.MEM) {
            consume(t, TokenType.MEM);

            consume(t, TokenType.LBRACKET);
            Expr me = parseExpression(t);

            consume(t, TokenType.RBRACKET);
            consume(t, TokenType.ASSIGN);

            Expr re = parseExpression(t);
            c = new CmdUpdate(me, re);
        } else if (t.peek().isMemSugar()) {
            ExprNum expr = new ExprNum(TokenType.getMemStr(t));

            consume(t, TokenType.ASSIGN);

            Expr re = parseExpression(t);
            c = new CmdUpdate(expr, re);
        } else {
			if (t.peek().getType() != TokenType.SERVE) {
                c = new Cmd(CmdType.valueOf(t.next().getType().name()));
            } else {
				consume(t, TokenType.SERVE);
                consume(t, TokenType.LBRACKET);
                Expr e = parseExpression(t);
                consume(t, TokenType.RBRACKET);
                c = new CmdServe(e);
            }
        }
        return c;
    }

    /**
     * Consumes a token of the expected type.
     *
     * @throws SyntaxError if the wrong kind of token is encountered.
     */
    public static void consume(Tokenizer t, TokenType tt) throws SyntaxError {
        if (t.peek().getType() != tt)
            throw new SyntaxError(t.lineNumber(), "Expected " + tt + " but got " + t.peek().getType());
		t.next();
    }

	@Override
	public Program parse(Reader r) throws SyntaxError {
		Tokenizer t = new Tokenizer(r);
		return parseProgram(t);
	}
}