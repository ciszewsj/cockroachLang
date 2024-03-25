import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


import grammar.CockroachLexer;
import grammar.CockroachParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;


public class Main {

	static String inputFilePath = null;
	static String outputFilePath = "./out.ll";

	public static void f(String[] args) {

		for (int i = 0; i < args.length; i += 2) {
			if ("-i".equals(args[i])) {
				inputFilePath = args[i + 1];
			} else if ("-o".equals(args[i])) {
				outputFilePath = args[i + 1];
			} else if ("-h".equals(args[i])) {
				System.out.println("Użycie: java -jar cockroach-lang.jar -i ścieżka_do_pliku -o ścieżka_do_pliku");

			}
		}
	}

	public static void main(String[] args) throws IOException {
		f(args);
		InputStream inputStream = null;
		if (inputFilePath != null) {
			inputStream = Files.newInputStream(Paths.get(inputFilePath));
		} else {
			inputStream = Main.class.getResourceAsStream("/example1.txt");
		}

		assert inputStream != null;
		Lexer lexer = new CockroachLexer(CharStreams.fromStream(inputStream));
		TokenStream tokenStream = new CommonTokenStream(lexer);
		CockroachParser parser = new CockroachParser(tokenStream);
		EventVisitor eval = new EventVisitor();

		FileOutputStream fos = new FileOutputStream(outputFilePath);
		fos.write(eval.visit(parser.startRule()).getBytes());
		fos.close();
	}
}
