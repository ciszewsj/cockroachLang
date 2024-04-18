import java.util.List;
import java.util.Map;
import java.util.Stack;

public class LlvmGenerator {
	private static String headerText = "";
	private static String mainText = "";
	private static String structDeclarations = "";
	private static String buffer = "";
	static int reg = 1;
	static int mainReg = 1;
	static int br = 0;

	static Stack<Integer> brstack = new Stack<>();


	static void loadStructField(String id, String structName, int position) {
		buffer += "%" + reg + " = getelementptr %" + structName + ", %" + structName + "* %" + id + ", i32 0, i32 " + position + "\n";
		reg++;
	}


	static void printf(TYPE type) {
		if (type == TYPE.FLOAT32) {
			buffer += "%" + reg + " = fpext float %" + (reg - 1) + " to double\n";
			reg++;
			buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strf, i32 0, i32 0), double %" + (reg - 1) + ")\n";

		} else if (type == TYPE.FLOAT64) {
			buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strf, i32 0, i32 0), double %" + (reg - 1) + ")\n";
		} else if (type == TYPE.LONG) {
			buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([6 x i8], [6 x i8]* @strl, i32 0, i32 0), i64 %" + (reg - 1) + ")\n";
		} else {
			buffer += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
		}
		reg++;
	}

	static void repeatStart(String id, boolean global) {
		declare(Integer.toString(reg), TYPE.INT, false);
		int counter = reg;
		reg++;
		assign(Integer.toString(counter), "0", TYPE.INT, false);
		br++;
		buffer += "br label %cond" + br + "\n";
		buffer += "cond" + br + ":\n";

		load(Integer.toString(counter), TYPE.INT, false, false);
		add("%" + (reg - 1), "1", TYPE.INT);
		assign(Integer.toString(counter), "%" + (reg - 1), TYPE.INT, false);

		load(id, TYPE.INT, global, false);
		buffer += "%" + reg + " = icmp sle i32 %" + (reg - 2) + ", %" + (reg - 1) + "\n";
		reg++;

		buffer += "br i1 %" + (reg - 1) + ", label %true" + br + ", label %false" + br + "\n";
		buffer += "true" + br + ":\n";
		brstack.push(br);
	}

	static void repeatEnd() {
		int b = brstack.pop();
		buffer += "br label %cond" + b + "\n";
		buffer += "false" + b + ":\n";
	}

	static void scan(String id) {
		buffer += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
		reg++;
	}

	static void intToDouble(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = sitofp " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void intToLong(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = sext " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void longToInt(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = trunc " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void doubleToInt(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = fptosi " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void doubleToFloatN(String value) {
		buffer += "%" + reg + " = fptrunc double " + value + " to float\n";
		reg++;
	}

	static void doubleToFloat(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = fptrunc " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void floatToDouble(TYPE type1, TYPE type2) {
		buffer += "%" + reg + " = fpext " + type2.type + " %" + (reg - 1) + " to " + type1.type + "\n";
		reg++;
	}

	static void scanDouble(String id) {
		buffer += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strd, i32 0, i32 0), double* %" + id + ")\n";
		reg++;
	}

	static void ifStart() {
		br++;
		buffer += "br i1 %" + (reg - 1) + ", label %true" + br + ", label %false" + br + "\n";
		buffer += "true" + br + ":\n";
		brstack.push(br);
	}

	static void ifEnd() {
		int b = brstack.pop();
		buffer += "br label %false" + b + "\n";
		buffer += "false" + b + ":\n";
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
						operator = "ogt";
						cmp = "fcmp";
						break;
					case LONG:
					case INT:
						operator = "sgt";
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

		buffer += "%" + reg + " = " + cmp + " " + operator + " " + type.type + " %" + id1 + ", %" + id2 + "\n";
		reg++;
	}

	static void declare(String id, TYPE type, boolean global) {
		String value = "";
		if (type == TYPE.INT || type == TYPE.LONG) {
			value = "0";
		} else {
			value = "0.0";
		}
		if (global) {
			headerText += "@" + id + " = global " + type.type + " " + value + "\n";
		} else {
			buffer += "%" + id + " = alloca " + type.type + "\n";
		}
	}

	static void assign(String id, String value, TYPE type, boolean global) {
		if (global) {
			buffer += "store " + type.type + " " + value + ", " + type.type + "* @" + id + "\n";
		} else {
			buffer += "store " + type.type + " " + value + ", " + type.type + "* %" + id + "\n";
		}
	}

	static void load(String id, TYPE type, boolean global, boolean function) {
		if (function) {
			buffer += "%" + reg + " = call " + type.type + " @" + id + "()\n";
		} else if (global) {
			buffer += "%" + reg + " = load " + type.type + ", " + type.type + "* @" + id + "\n";
		} else {
			buffer += "%" + reg + " = load " + type.type + ", " + type.type + "* %" + id + "\n";
		}
		reg++;
	}

	static void functionStart(String id, TYPE type) {
		mainText += buffer;
		mainReg = reg;
		buffer = "define " + type.type + " @" + id + "() nounwind {\n";
		reg = 1;
	}

	static void functionEnd(TYPE type) {
		buffer += "ret " + type.type + " %" + (reg - 1) + "\n";
		buffer += "}\n";
		headerText += buffer;
		buffer = "";
		reg = mainReg;
	}

	static void add(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			buffer += "%" + reg + " = fadd " + type.type + " " + val1 + ", " + val2 + "\n ";
		} else {
			buffer += "%" + reg + " = add " + type.type + " " + val1 + ", " + val2 + "\n ";
		}
		reg++;
	}

	static void substract(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			buffer += "%" + reg + " = fsub " + type.type + " " + val2 + ", " + val1 + "\n";
		} else {
			buffer += "%" + reg + " = sub " + type.type + " " + val2 + ", " + val1 + "\n";
		}
		reg++;
	}

	static void mul(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			buffer += "%" + reg + " = fmul " + type.type + " " + val1 + ", " + val2 + "\n";
		} else {
			buffer += "%" + reg + " = mul " + type.type + " " + val1 + ", " + val2 + "\n";
		}
		reg++;
	}

	static void divide(String val1, String val2, TYPE type) {
		if (type == TYPE.FLOAT32 || type == TYPE.FLOAT64) {
			buffer += "%" + reg + " = fdiv " + type.type + " " + val2 + ", " + val1 + "\n";
		} else {
			buffer += "%" + reg + " = sdiv " + type.type + " " + val2 + ", " + val1 + "\n";
		}
		reg++;
	}

	static void declareStruct(String id, List<TYPE> types) {
		structDeclarations += "%" + id + "= type{";
		boolean first = true;
		for (TYPE type : types) {
			if (!first) {
				structDeclarations += ", ";
			}
			structDeclarations += type.type;
			first = false;
		}
		structDeclarations += "}\n";
	}

	static void defineStruct(String id, String structId, Map<String, TYPE> vars) {
		buffer += "%" + id + " = alloca %" + structId + "\n";
		int i = 0;
		for (String key : vars.keySet()) {
			buffer += "%" + reg + " = getelementptr %" + structId + ", %" + structId + "* %" + id + ", i32 0, i32 " + i + "\n";
			buffer += "store " + vars.get(key).type + " " + key + ", " + vars.get(key).type + "* %" + reg + "\n";
			reg++;
			i++;
		}
	}

	static void closeMain() {
		mainText += buffer;
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
		text += structDeclarations;
		text += headerText;
		text += "define i32 @main() nounwind{\n";
		text += mainText;
		text += "ret i32 0 }\n";
		return text;
	}
}
