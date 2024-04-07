import grammar.CockroachBaseListener;
import grammar.CockroachParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class EventVisitor extends CockroachBaseListener {
	private final Map<String, TYPE> variables = new HashMap<>();
	private final Stack<String> stack = new Stack<>();

	@Override
	public void exitStartRule(CockroachParser.StartRuleContext ctx) {
		System.out.println(LlvmGenerator.generate());
	}

	@Override
	public void exitAssignment(CockroachParser.AssignmentContext ctx) {
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			if (!variables.containsKey(id)) {
				variables.put(id, TYPE.INT);
				LlvmGenerator.declare(id);
			}
			LlvmGenerator.assign(id, stack.pop());
		}
	}

	@Override
	public void exitVariable(CockroachParser.VariableContext ctx) {
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			if (variables.containsKey(id)) {
				LlvmGenerator.load(id);
				stack.push("%" + (LlvmGenerator.reg - 1));
			} else {
				error(ctx.getStart().getLine(), "unknown variable " + id);
			}
		} else if (ctx.INT() != null) {
			stack.push(ctx.INT().getText());

		}
	}

	@Override
	public void exitAdd(CockroachParser.AddContext ctx) {
		LlvmGenerator.add(stack.pop(), stack.pop());
		stack.push("%" + (LlvmGenerator.reg - 1));
	}

	@Override
	public void exitSubstract(CockroachParser.SubstractContext ctx) {
		LlvmGenerator.substract(stack.pop(), stack.pop());
		stack.push("%" + (LlvmGenerator.reg - 1));
	}

	@Override
	public void exitMul(CockroachParser.MulContext ctx) {
		LlvmGenerator.mul(stack.pop(), stack.pop());
		stack.push("%" + (LlvmGenerator.reg - 1));
	}

	@Override
	public void exitDivide(CockroachParser.DivideContext ctx) {
		LlvmGenerator.divide(stack.pop(), stack.pop());
		stack.push("%" + (LlvmGenerator.reg - 1));
	}


	@Override
	public void exitPrint(CockroachParser.PrintContext ctx) {
		String id = ctx.ID().getText();
		if (variables.containsKey(id)) {
			LlvmGenerator.printf(id);
		} else {
			error(ctx.getStart().getLine(), "unknown variable " + id);
		}
	}

	@Override
	public void exitScan(CockroachParser.ScanContext ctx) {
		String id = ctx.ID().getText();
		if (!variables.containsKey(id)) {
			variables.put(id, TYPE.INT);
			LlvmGenerator.declare(id);
		}
		LlvmGenerator.scan(id);
	}

	void error(int line, String msg) {
		System.err.println("Error, line " + line + ", " + msg);
		System.exit(1);
	}
}
