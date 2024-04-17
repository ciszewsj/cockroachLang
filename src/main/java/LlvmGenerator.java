import java.util.Stack;

public class LlvmGenerator {
	private static String headerText = "";
	private static String mainText = "";
	static int reg = 1;
	static int br = 0;

	static Stack<Integer> brstack = new Stack<>();

	static void printf(String id, TYPE type) {
		if (type == TYPE.FLOAT32) {
			mainText += "%" + reg + " = load float, float* %" + id + "\n";
			reg++;
			mainText += "%" + reg + " = fpext float %" + (reg - 1) + " to double\n";
			reg++;
			mainText += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strf, i32 0, i32 0), double %" + (reg - 1) + ")\n";

		} else if (type == TYPE.FLOAT64) {
			mainText += "%" + reg + " = load double, double* %" + id + "\n";
			reg++;
			mainText += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strf, i32 0, i32 0), double %" + (reg - 1) + ")\n";
		} else if (type == TYPE.LONG) {
			mainText += "%" + reg + " = load i64, i64* %" + id + "\n";
			reg++;
			mainText += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @strl, i32 0, i32 0), i64 %" + (reg - 1) + ")\n";
		} else {
			mainText += "%" + reg + " = load i32, i32* %" + id + "\n";
			reg++;
			mainText += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
		}
		reg++;
	}

	static void repeatStart(String id) {
		declare(Integer.toString(reg), TYPE.INT);
		int counter = reg;
		reg++;
		assign(Integer.toString(counter), "0", TYPE.INT);
		br++;
		mainText += "br label %cond" + br + "\n";
		mainText += "cond" + br + ":\n";

		load(Integer.toString(counter), TYPE.INT);
		add("%" + (reg - 1), "1", TYPE.INT);
		assign(Integer.toString(counter), "%" + (reg - 1), TYPE.INT);

		load(id, TYPE.INT);
		mainText += "%" + reg + " = icmp sle i32 %" + (reg - 2) + ", %" + (reg - 1) + "\n";
		reg++;

		mainText += "br i1 %" + (reg - 1) + ", label %true" + br + ", label %false" + br + "\n";
		mainText += "true" + br + ":\n";
		brstack.push(br);
	}

	static void repeatEnd() {
		int b = brstack.pop();
		mainText += "br label %cond" + b + "\n";
		mainText += "false" + b + ":\n";
	}

	static void scan(String id) {
		mainText += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
		reg++;
	}

	static void intToDouble(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = sitofp " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void intToLong(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = sext " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void longToInt(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = trunc " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void doubleToInt(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = fptosi " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void doubleToFloatN(String value) {
		mainText += "%" + reg + " = fptrunc double " + value + " to float\n";
		reg++;
	}

	static void doubleToFloat(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = fptrunc " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void floatToDouble(TYPE type1, TYPE type2) {
		mainText += "%" + reg + " = fpext " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void scanDouble(String id) {
		mainText += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strd, i32 0, i32 0), double* %" + id + ")\n";
		reg++;
	}

	static void ifStart() {
		br++;
		mainText += "br i1 %" + (reg - 1) + ", label %true" + br + ", label %false" + br + "\n";
		mainText += "true" + br + ":\n";
		brstack.push(br);
	}

	static void ifEnd() {
		int b = brstack.pop();
		mainText += "br label %false" + b + "\n";
		mainText += "false" + b + ":\n";
	}

	static void compare(String id1, String id2, TYPE type, EqualsType equalsType) {
		String operator = "";
		String cmp = "";
		switch (equalsType) {
			case LESS:
				switch (type) {
					case FLOAT32:
					case FLOAT64:
						operator = "olt";
						cmp = "fcmp";
						break;
					case LONG:
					case INT:
						operator = "slt";
						cmp = "icmp";
						break;
				}
				break;
			case MORE:
				switch (type) {
					case FLOAT32:
					case FLOAT64:
						operator = "ole";
						cmp = "fcmp";
						break;
					case LONG:
					case INT:
						operator = "sle";
						cmp = "icmp";
						break;
				}
				break;
			case EQUAL:
				switch (type) {
					case FLOAT32:
					case FLOAT64:
						operator = "oeq";
						cmp = "fcmp";
						break;
					case LONG:
					case INT:
						operator = "eq";
						cmp = "icmp";
						break;
				}
				break;
		}

		mainText += "%" + reg + " = " + cmp + " " + operator + " " + type.type + " %" + id1 + ", %" + id2 + "\n";
		reg++;
	}

	static void declare(String id, TYPE type) {
		mainText += "%" + id + " = alloca " + type.type + "\n";
	}

	static void assign(String id, String value, TYPE type) {
		mainText += "store " + type.type + " " + value + ", " + type.type + "* %" + id + "\n";
	}

	static void load(String id, TYPE type) {
		mainText += "%" + reg + " = load " + type.type + ", " + type.type + "* %" + id + "\n";
		reg++;
	}


	static void add(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			mainText += "%" + reg + " = fadd " + type.type + " " + val1 + ", " + val2 + "\n ";
		} else {
			mainText += "%" + reg + " = add " + type.type + " " + val1 + ", " + val2 + "\n ";
		}
		reg++;
	}

	static void substract(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			mainText += "%" + reg + " = fsub " + type.type + " " + val2 + ", " + val1 + "\n";
		} else {
			mainText += "%" + reg + " = sub " + type.type + " " + val2 + ", " + val1 + "\n";
		}
		reg++;
	}

	static void mul(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			mainText += "%" + reg + " = fmul " + type.type + " " + val1 + ", " + val2 + "\n";
		} else {
			mainText += "%" + reg + " = mul " + type.type + " " + val1 + ", " + val2 + "\n";
		}
		reg++;
	}

	static void divide(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			mainText += "%" + reg + " = fdiv " + type.type + " " + val2 + ", " + val1 + "\n";
		} else {
			mainText += "%" + reg + " = sdiv " + type.type + " " + val2 + ", " + val1 + "\n";
		}
		reg++;
	}

	static String generate() {
		String text = "";
		text += "declare i32 @printf(i8*, ...)\n";
		text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
		text += "@strp = constant [4 x i8] c\"%d\\0A\\00\"\n";
		text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
		text += "@strf = constant [4 x i8] c\"%f\\0A\\00\"\n";
		text += "@strd = constant [4 x i8] c\"%lf\\00\"\n";
		text += "@strl = constant [6 x i8] c\"%lld\\0A\\00\"\n";
		text += headerText;
		text += "define i32 @main() nounwind{\n";
		text += mainText;
		text += "ret i32 0 }\n";
		return text;
	}
}
