import grammar.CockroachBaseVisitor;
import grammar.CockroachParser;

public class EventVisitor extends CockroachBaseVisitor<String> {

	@Override
	public String visitStartRule(CockroachParser.StartRuleContext ctx) {
		StringBuilder llvmCode = new StringBuilder();

		llvmCode.append("define i32 @main() {\n");
		llvmCode.append("entry:");

		for (CockroachParser.StatementContext stat : ctx.statement()) {
			llvmCode.append(visit(stat));
		}
		llvmCode.append("ret i32 0\n");
		llvmCode.append("}");
		return llvmCode.toString();
	}

	@Override
	public String visitStatement(CockroachParser.StatementContext ctx) {
		return visit(ctx.assignment());
	}

	@Override
	public String visitAssignment(CockroachParser.AssignmentContext ctx) {
		String variable = ctx.ID().getText();
		int value = Integer.parseInt(ctx.INT().getText());
		String llvmCode = "%" + variable + " = alloca i32\n"; // Deklaracja zmiennej
		llvmCode += "store i32 " + value + ", i32* %" + variable + "\n"; // Przypisanie wartości
		return llvmCode;
	}
}
