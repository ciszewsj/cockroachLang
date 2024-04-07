public class LlvmGenerator {
	private static String headerText = "";
	private static String mainText = "";
	static int reg = 1;

	static void printf(String id) {
		mainText += "%" + reg + " = load i32, i32* %" + id + "\n";
		reg++;
		mainText += "%" + reg + " = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @strp, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
		reg++;
	}

	static void scan(String id) {
		mainText += "%" + reg + " = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @strs, i32 0, i32 0), i32* %" + id + ")\n";
		reg++;
	}


	static void declare(String id) {
		mainText += "%" + id + " = alloca i32\n";
	}

	static void assign(String id, String value) {
		mainText += "store i32 " + value + ", i32* %" + id + "\n";
	}

	static void load(String id) {
		mainText += "%" + reg + " = load i32, i32* %" + id + "\n";
		reg++;
	}


	static void add(String val1, String val2) {
		mainText += "%" + reg + " = add i32 " + val1 + ", " + val2 + "\n";
		reg++;
	}

	static void substract(String val1, String val2) {
		mainText += "%" + reg + " = sub i32 " + val2 + ", " + val1 + "\n";
		reg++;
	}

	static void mul(String val1, String val2) {
		mainText += "%" + reg + " = mul i32 " + val1 + ", " + val2 + "\n";
		reg++;
	}

	static void divide(String val1, String val2) {
		mainText += "%" + reg + " = sdiv i32 " + val2 + ", " + val1 + "\n";
		reg++;
	}

	static String generate() {
		String text = "";
		text += "declare i32 @printf(i8*, ...)\n";
		text += "declare i32 @__isoc99_scanf(i8*, ...)\n";
		text += "@strp = constant [4 x i8] c\"%d\\0A\\00\"\n";
		text += "@strs = constant [3 x i8] c\"%d\\00\"\n";
		text += headerText;
		text += "define i32 @main() nounwind{\n";
		text += mainText;
		text += "ret i32 0 }\n";
		return text;
	}
}
