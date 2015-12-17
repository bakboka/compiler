import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Main {

	private static ArrayList<Symbol> listSym = new ArrayList<Symbol>(); //list of symbol
	private static Generator code; //code generator
	private static ArrayList<Symbol> id = new ArrayList<Symbol>(); //table of symbol

	public static void main(String[] args) throws FileNotFoundException,IOException,ParseError{
		FileReader reader = new FileReader(args[0]);
		code = new Generator();
		LexicalAnalyzer scanner = new LexicalAnalyzer(reader); //our scanner
		for(Symbol symbol = scanner.nextToken();symbol.getType() != LexicalUnit.END_OF_STREAM;symbol = scanner.nextToken()){ // getting every token
			listSym.add(symbol);
			if(symbol.getType() == LexicalUnit.VARNAME && !checkID(symbol)){ // only add an identifier to the list if it is not already in it
				id.add(symbol); //adding the symbol to the table
			}
		}
		listSym.add(new Symbol(LexicalUnit.END_OF_STREAM,0,0,null)); //lines and column not important
		code.printBegin(id);
		Parser parser = new Parser(code,listSym);
		parser.Parse();
		code.printEnd();
	}
	private static boolean checkID(Symbol symbol){
	/*
		checks if the identifier is already in our list
		returns true if it is
		return false otherwise

		Arguments : Symbol symbol
	*/
		for(int i=0;i<id.size();i++){
			if(id.get(i).getValue().toString().equals(symbol.getValue().toString()))
				return true;
		}
		return false;
	}
};
