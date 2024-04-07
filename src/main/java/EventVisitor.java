import grammar.CockroachBaseListener;
import grammar.CockroachParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class EventVisitor extends CockroachBaseListener {
	private final Map<String, TYPE> variables = new HashMap<>();
	private final Stack<String> stack = new Stack<>();
	private final Stack<TYPE> types = new Stack<>();

	@Override
	public void exitStartRule(CockroachParser.StartRuleContext ctx) {
		System.out.println(LlvmGenerator.generate());
	}

	@Override
	public void exitAssignment(CockroachParser.AssignmentContext ctx) {
		TYPE type = types.pop();
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			if (!variables.containsKey(id)) {
				variables.put(id, type);
				LlvmGenerator.declare(id, type);
			}
			LlvmGenerator.assign(id, stack.pop(), variables.get(id));
		}
	}

	@Override
	public void exitVariable(CockroachParser.VariableContext ctx) {
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			if (variables.containsKey(id)) {
				LlvmGenerator.load(id, variables.get(id));
				stack.push("%" + (LlvmGenerator.reg - 1));
				types.push(variables.get(id));
			} else {
				error(ctx.getStart().getLine(), "unknown variable " + id);
			}
		} else if (ctx.INT() != null) {
			stack.push(ctx.INT().getText());
			types.push(TYPE.INT);
		} else if (ctx.DOUBLE() != null) {
			stack.push(ctx.DOUBLE().getText());
			types.push(TYPE.FLOAT64);
		} else if (ctx.LONG() != null) {
			stack.push(ctx.LONG().getText().replace("l", ""));
			types.push(TYPE.LONG);
		} else if (ctx.FLOAT() != null) {
			stack.push(ctx.FLOAT().getText().replace("f", ""));
			types.push(TYPE.FLOAT32);
		}
	}

	@Override
	public void exitAdd(CockroachParser.AddContext ctx) {
		TYPE type1 = types.pop();
		TYPE type2 = types.pop();
		if (type1 != type2) {
			error(ctx.getStart().getLine(), "types mismatch");
		}
		LlvmGenerator.add(stack.pop(), stack.pop(), type1);
		stack.push("%" + (LlvmGenerator.reg - 1));
		types.push(type1);
	}

	@Override
	public void exitSubstract(CockroachParser.SubstractContext ctx) {
		TYPE type1 = types.pop();
		TYPE type2 = types.pop();
		if (type1 != type2) {
			error(ctx.getStart().getLine(), "types mismatch");
		}
		LlvmGenerator.substract(stack.pop(), stack.pop(), type1);
		stack.push("%" + (LlvmGenerator.reg - 1));
		types.push(type1);
	}

	@Override
	public void exitMul(CockroachParser.MulContext ctx) {
		TYPE type1 = types.pop();
		TYPE type2 = types.pop();
		if (type1 != type2) {
			error(ctx.getStart().getLine(), "types mismatch");
		}
		LlvmGenerator.mul(stack.pop(), stack.pop(), type1);
		stack.push("%" + (LlvmGenerator.reg - 1));
		types.push(type1);
	}

	@Override
	public void exitDivide(CockroachParser.DivideContext ctx) {
		TYPE type1 = types.pop();
		TYPE type2 = types.pop();
		if (type1 != type2) {
			error(ctx.getStart().getLine(), "types mismatch");
		}
		LlvmGenerator.divide(stack.pop(), stack.pop(), type1);
		stack.push("%" + (LlvmGenerator.reg - 1));
		types.push(type1);
	}


	@Override
	public void exitPrint(CockroachParser.PrintContext ctx) {
		String id = ctx.ID().getText();
		if (variables.containsKey(id)) {
			LlvmGenerator.printf(id, variables.get(id));
		} else {
			error(ctx.getStart().getLine(), "unknown variable " + id);
		}
	}

	@Override
	public void exitScan(CockroachParser.ScanContext ctx) {
		String id = ctx.ID().getText();
		if (!variables.containsKey(id)) {
			variables.put(id, TYPE.INT);
			LlvmGenerator.declare(id, variables.get(id));
		}
		LlvmGenerator.scan(id, variables.get(id));
	}


	void error(int line, String msg) {
		System.err.println("Error, line " + line + ", " + msg);
		System.exit(1);
	}
}
