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
			LlvmGenerator.doubleToFloatN(ctx.FLOAT().getText().replace("f", ""));
			stack.push("%" + (LlvmGenerator.reg - 1));
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
		} else if (variables.get(id) != TYPE.INT) {
			error(ctx.getStart().getLine(), "Could not change type of " + id + " dynamically");
		}
		LlvmGenerator.scan(id);
	}

	@Override
	public void exitConvert(CockroachParser.ConvertContext ctx) {

		TYPE type1 = types.pop();
		TYPE type2 = types.pop();
		TYPE type3 = null;
		String id = ctx.ID().getText();
		if (variables.containsKey(id)) {
			type3 = variables.get(id);
			LlvmGenerator.load(id, variables.get(id));
		} else {
			error(ctx.getStart().getLine(), "unknown variable " + id);
		}

		if (type2 != type3) {
			error(ctx.getStart().getLine(), "types mismatch during convert");
		}
		switch (type2) {
			case INT:
			case LONG:
				switch (type1) {
					case FLOAT32:
					case FLOAT64:
						LlvmGenerator.intToDouble(type1, type2);
						break;
					case INT:
						LlvmGenerator.longToInt(type1, type2);
						break;
					case LONG:
						LlvmGenerator.intToLong(type1, type2);
						break;
				}
				break;
			case FLOAT32:
			case FLOAT64:
				switch (type1) {
					case FLOAT64:
						LlvmGenerator.floatToDouble(type1, type2);
						break;
					case FLOAT32:
						LlvmGenerator.doubleToFloat(type1, type2);
						break;
					case INT:
					case LONG:
						LlvmGenerator.doubleToInt(type1, type2);
						break;
				}
				break;
		}
		stack.push("%" + (LlvmGenerator.reg - 1));
		types.push(type1);
	}

	@Override
	public void exitConvertSymbol(CockroachParser.ConvertSymbolContext ctx) {
		if (ctx.DTOF() != null) {
			types.push(TYPE.FLOAT64);
			types.push(TYPE.FLOAT32);
		} else if (ctx.FTOD() != null) {
			types.push(TYPE.FLOAT32);
			types.push(TYPE.FLOAT64);
		} else if (ctx.DTOI() != null) {
			types.push(TYPE.FLOAT64);
			types.push(TYPE.INT);
		} else if (ctx.ITOD() != null) {
			types.push(TYPE.INT);
			types.push(TYPE.FLOAT64);
		} else if (ctx.DTOL() != null) {
			types.push(TYPE.FLOAT64);
			types.push(TYPE.LONG);
		} else if (ctx.LTOD() != null) {
			types.push(TYPE.LONG);
			types.push(TYPE.FLOAT64);
		} else if (ctx.FTOI() != null) {
			types.push(TYPE.FLOAT32);
			types.push(TYPE.INT);
		} else if (ctx.ITOL() != null) {
			types.push(TYPE.INT);
			types.push(TYPE.LONG);
		} else if (ctx.LTOI() != null) {
			types.push(TYPE.LONG);
			types.push(TYPE.INT);
		} else if (ctx.FTOL() != null) {
			types.push(TYPE.FLOAT32);
			types.push(TYPE.LONG);
		} else if (ctx.LTOF() != null) {
			types.push(TYPE.LONG);
			types.push(TYPE.FLOAT32);
		} else if (ctx.ITOF() != null) {
			types.push(TYPE.INT);
			types.push(TYPE.FLOAT32);
		}
	}

	@Override
	public void enterScand(CockroachParser.ScandContext ctx) {
		String id = ctx.ID().getText();
		if (!variables.containsKey(id)) {
			variables.put(id, TYPE.FLOAT64);
			LlvmGenerator.declare(id, variables.get(id));
		} else if (variables.get(id) != TYPE.FLOAT64) {
			error(ctx.getStart().getLine(), "Could not change type of " + id + " dynamically");
		}
		LlvmGenerator.scanDouble(id);
	}

	void error(int line, String msg) {
		System.err.println("Error, line " + line + ", " + msg);
		System.exit(1);
	}

	@Override
	public void exitRepeatheader(CockroachParser.RepeatheaderContext ctx) {
		String id = ctx.ID().getText();
		if (variables.containsKey(id) && variables.get(id) == TYPE.INT) {
			LlvmGenerator.repeatStart(ctx.ID().getText());
		} else {
			error(ctx.getStart().getLine(), "Wrong value of repeat should be declared INT");
		}
	}

	@Override
	public void exitRepeatbody(CockroachParser.RepeatbodyContext ctx) {
		if (ctx.getParent() instanceof CockroachParser.RepeatstatementContext) {
			LlvmGenerator.repeatEnd();
		}
	}

	@Override
	public void enterIfbody(CockroachParser.IfbodyContext ctx) {
		LlvmGenerator.ifStart();
	}

	@Override
	public void exitIfbody(CockroachParser.IfbodyContext ctx) {
		LlvmGenerator.ifEnd();
	}

	@Override
	public void exitCompare(CockroachParser.CompareContext ctx) {
		String id1 = ctx.ID().get(0).toString();
		String id2 = ctx.ID().get(1).toString();
		TYPE type1 = null;
		TYPE type2 = null;

		if (variables.containsKey(id1)) {
			type1 = variables.get(id1);
			LlvmGenerator.load(id1, variables.get(id1));
		} else {
			error(ctx.getStart().getLine(), "unknown variable " + id1);
		}

		if (variables.containsKey(id2)) {
			type2 = variables.get(id2);
			LlvmGenerator.load(id2, variables.get(id2));
		} else {
			error(ctx.getStart().getLine(), "unknown variable " + id2);
		}

		if (type1 != type2) {
			error(ctx.getStart().getLine(), "types mismatch during convert");
		}
		if (ctx.operator().EQUALS() != null) {
			LlvmGenerator.compare(Integer.toString(LlvmGenerator.reg - 2),
					Integer.toString(LlvmGenerator.reg - 1),
					type1,
					EqualsType.EQUAL);
		} else if (ctx.operator().MORES() != null) {
			LlvmGenerator.compare(Integer.toString(LlvmGenerator.reg - 2),
					Integer.toString(LlvmGenerator.reg - 1),
					type1,
					EqualsType.MORE);
		} else if (ctx.operator().LESSS() != null) {
			LlvmGenerator.compare(Integer.toString(LlvmGenerator.reg - 2),
					Integer.toString(LlvmGenerator.reg - 1),
					type1,
					EqualsType.LESS);
		}
		super.exitCompare(ctx);
	}
}
