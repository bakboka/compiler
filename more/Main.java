import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Main {

	private static ArrayList<Symbol> listSym = new ArrayList<Symbol>();
	private static int curToken = 0;
	private static String code ="";
	private static PrintWriter writer; // the llvm code stored
	private static ArrayList<String> stack = new ArrayList<String>();
	private static int unnamedVariable = 0;
	private static ArrayList<String> stackOfNames = new ArrayList<String>();
	private static ArrayList<String> stackOfVariables = new ArrayList<String>();

	public static void main(String[] args) throws FileNotFoundException,IOException,ParseError{
		writer = new PrintWriter("code.ll");
		FileReader reader = new FileReader(args[0]);
		LexicalAnalyzer scanner = new LexicalAnalyzer(reader); //our scanner
		for(Symbol symbol = scanner.nextToken();symbol.getType() != LexicalUnit.END_OF_STREAM;symbol = scanner.nextToken()){ // getting every token
			listSym.add(symbol);
		}
		listSym.add(new Symbol(LexicalUnit.END_OF_STREAM,0,0,null)); //lines and column not important
		stack.add("define void @project(){");
		stack.add("	entry:");
		Parser();
		stack.add("ret void");
		stack.add("}");
		generateCode();
		writer.close();
	}
	public static void generateCode(){
		for(int i=0;i<stack.size();i++){
			writer.println(stack.get(i));
		}
	}
	public static void Parser() throws ParseError{
			if(Goal())
				System.out.println("Good");
			else
				throw new ParseError(listSym.get(curToken));
	}
	public static boolean Match(LexicalUnit t){
		if (listSym.get(curToken).getType().equals(t)){
			curToken++;
			return true;
		}
		else{
			return false;
		}
	}
	public static void Send_output(int no){
		System.out.print(no + " ");
	}

	public static boolean Goal(){
		Send_output(1);
		if(Program())
		if(Match(LexicalUnit.END_OF_STREAM))
			return true;
		return false;
	}
	public static boolean Program(){
		Send_output(2);
		if(Match(LexicalUnit.BEG))
		if(Code())
		if(Match(LexicalUnit.END))
			return true;
		return false;
	}
	public static boolean Code(){
		switch(listSym.get(curToken).getType()){
		case OD:
		case FI:
		case ELSE:
		case END:
			Send_output(3);
			return true;
		case VARNAME:
		case WHILE:
		case PRINT:
		case IF:
		case READ:
		case FOR:
			Send_output(4);
			if(InstList())
				return true;
		}
		return false;
	}
	public static boolean InstList(){
		Send_output(5);
		if(Instruction())
		if(EndList())
			return true;
		return false;
	}
	public static boolean EndList(){
		switch(listSym.get(curToken).getType()){
		case SEMICOLON:
			Send_output(6);
			if(Match(LexicalUnit.SEMICOLON))
			if(InstList())
				return true;
			break;
		case OD:
		case FI:
		case ELSE:
		case END:
			Send_output(7);
			return true;
		}
		return false;
	}
	public static boolean Instruction(){
		switch(listSym.get(curToken).getType()){
			case VARNAME:
				Send_output(8);
				if(Assign())
					return true;
				break;
			case IF:
				Send_output(9);
				if(If())
					return true;
				break;
			case WHILE:
				Send_output(10);
				if(While())
					return true;
				break;
			case FOR:
				Send_output(11);
				if(For())
					return true;
				break;
			case PRINT:
				Send_output(12);
				if(Print())
					return true;
				break;
			case READ:
				Send_output(13);
				if(Read())
					return true;
			break;
		}
		return false;
	}
	public static boolean Assign(){
		Send_output(14);
		if(Match(LexicalUnit.VARNAME)){
			stackOfVariables.add("%"+(String)listSym.get(curToken-1).getValue());
			if(Match(LexicalUnit.ASSIGN))
			if(ExprArith()){
				stack.add(stackOfVariables.get(stackOfVariables.size()-1)+" = alloca i32");
				stack.add("store i32 "+stackOfNames.get(stackOfNames.size()-1)+",i32* "+stackOfVariables.get(stackOfVariables.size()-1));
				stackOfNames.remove(stackOfNames.size()-1);
				return true;
			}
		}
		return false;

	}
	public static boolean ExprArith(){
		Send_output(15);
		if(Factor())
		if(Terms())
			return true;
		return false;
	}
	public static boolean Terms(){
		switch(listSym.get(curToken).getType()){
		case PLUS:
		case MINUS:
			Send_output(17);
			if(AddSub())
			if(ExprArith()){
				stack.add("%"+unnamedVariable+" = add i32 "+stackOfNames.get(stackOfNames.size()-1)+ ","+stackOfNames.get(stackOfNames.size()-2));
				stackOfNames.remove(stackOfNames.size()-1); // poping the variable. It is useless now
				stackOfNames.remove(stackOfNames.size()-1); // poping the variable. It is useless now
				stackOfNames.add("%"+unnamedVariable);
				unnamedVariable+=1;
				return true;
			}
			break;
		case BY:
		case TO:
		case DO:
		case EQUAL:
		case SMALLER_EQUAL:
		case SMALLER:
		case GREATER_EQUAL:
		case GREATER:
		case DIFFERENT:
		case LEFT_PARENTHESIS:
		case SEMICOLON:
		case OD:
		case FI:
		case ELSE:
		case END:
		case THEN:
		case OR:
		case AND:
			Send_output(16);
			return true;
		}
		return false;
	}
	public static boolean A1(){
		switch(listSym.get(curToken).getType()){
		case VARNAME:
			Send_output(22);
			if(Match(LexicalUnit.VARNAME)){
				stack.add("%"+unnamedVariable+" = load i32* %"+(String)listSym.get(curToken-1).getValue());
				stackOfNames.add("%"+unnamedVariable);
				unnamedVariable+=1;
				return true;
			}
			break;
		case NUMBER:
			Send_output(23);
			if(Match(LexicalUnit.NUMBER)){
				stackOfNames.add((String)listSym.get(curToken-1).getValue()); // adding the value to the stack
				return true;
			}
			break;
		case LEFT_PARENTHESIS:
			Send_output(24);
			if(Match(LexicalUnit.LEFT_PARENTHESIS))
			if(ExprArith())
			if(Match(LexicalUnit.RIGHT_PARENTHESIS))
				return true;
			break;
		case MINUS:
			Send_output(25);
			if(Match(LexicalUnit.MINUS))
			if(A1())
				return true;
		}
		return false;
	}
	public static boolean AddSub(){
		switch(listSym.get(curToken).getType()){
		case PLUS:
			Send_output(26);
			if(Match(LexicalUnit.PLUS))
				return true;
			break;
		case MINUS:
			Send_output(27);
			if(Match(LexicalUnit.MINUS))
				return true;
		}
		return false;
	}
	public static boolean Factor(){
		Send_output(18);
		if(A1())
		if(Factors())
			return true;
		return false;
	}
	public static boolean Factors(){
		switch(listSym.get(curToken).getType()){
		case TIMES:
		case DIVIDE:
			Send_output(20);
			if(MultiDiv())
			if(Factor()){
				stack.add("%"+unnamedVariable+" = mul i32 "+stackOfNames.get(stackOfNames.size()-1)+ ","+stackOfNames.get(stackOfNames.size()-2));
				stackOfNames.remove(stackOfNames.size()-1); // poping the variable. It is useless now
				stackOfNames.remove(stackOfNames.size()-1); // poping the variable. It is useless now
				stackOfNames.add("%"+unnamedVariable);
				unnamedVariable+=1;
				return true;
			}
			break;
		case PLUS:
		case MINUS:
		case BY:
		case TO:
		case DO:
		case EQUAL:
		case SMALLER_EQUAL:
		case SMALLER:
		case GREATER_EQUAL:
		case GREATER:
		case DIFFERENT:
		case RIGHT_PARENTHESIS:
		case AND:
		case SEMICOLON:
		case OD:
		case FI:
		case ELSE:
		case END:
		case OR:
		case THEN:
			Send_output(19);
			return true;
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
			Send_output(21);
			if(A1())
			if(Factors())
				return true;
		}
		return false;
	}
	public static boolean MultiDiv(){
		switch(listSym.get(curToken).getType()){
		case TIMES:
			Send_output(28);
			if(Match(LexicalUnit.TIMES))
				return true;
			break;
		case DIVIDE:
			Send_output(29);
			if(Match(LexicalUnit.DIVIDE))
				return true;
		}
		return false;
	}
	public static boolean Cond(){
		Send_output(30);
		if(B1())
		if(B2())
			return true;
		return false;
	}
	public static boolean B1(){
		switch(listSym.get(curToken).getType()){
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
		case MINUS:
			Send_output(31);
			if(SimpleCond())
				return true;
			break;
		case NOT:
			Send_output(32);
			if(Match(LexicalUnit.NOT))
			if(SimpleCond())
				return true;
		}
		return false;
	}
	public static boolean B2(){
		switch(listSym.get(curToken).getType()){
		case OR:
			Send_output(33);
			if(Match(LexicalUnit.OR))
			if(Cond())
				return true;
			break;
		case AND:
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
		case MINUS:
		case DO:
		case THEN:
			Send_output(34);
			if(And())
				return true;
		}
		return false;
	}
	public static boolean And(){
		switch(listSym.get(curToken).getType()){
		case AND:
			Send_output(35);
			if(Match(LexicalUnit.AND))
			if(Cond())
				return true;
			break;
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
		case MINUS:
		case DO:
		case THEN:
			Send_output(36);
			return true;
		}
		return false;
	}
	public static boolean SimpleCond(){
		Send_output(37);
		if(ExprArith())
		if(Comp())
		if(ExprArith())
			return true;
		return false;
	}
	public static boolean If(){
		Send_output(38);
		if(Match(LexicalUnit.IF))
		if(Cond())
		if(Match(LexicalUnit.THEN))
		if(Code())
		if(EndIf())
			return true;
		return false;
	}
	public static boolean EndIf(){
		switch(listSym.get(curToken).getType()){
		case FI:
			Send_output(39);
			if(Match(LexicalUnit.FI))
				return true;
			break;
		case ELSE:
			Send_output(40);
			if(Match(LexicalUnit.ELSE))
			if(Code())
			if(Match(LexicalUnit.FI))
				return true;
		}
		return false;
	}
	public static boolean Comp(){
		switch(listSym.get(curToken).getType()){
		case EQUAL:
			Send_output(41);
			if(Match(LexicalUnit.EQUAL))
				return true;
			break;
		case GREATER_EQUAL:
			Send_output(42);
			if(Match(LexicalUnit.GREATER_EQUAL))
				return true;
			break;
		case GREATER:
			Send_output(43);
			if(Match(LexicalUnit.GREATER))
				return true;
			break;
		case SMALLER_EQUAL:
			Send_output(44);
			if(Match(LexicalUnit.SMALLER_EQUAL))
				return true;
			break;
		case SMALLER:
			Send_output(45);
			if(Match(LexicalUnit.SMALLER))
				return true;
			break;
		case DIFFERENT:
			Send_output(46);
			if(Match(LexicalUnit.DIFFERENT))
				return true;
		}
		return false;
	}
	public static boolean While(){
		Send_output(47);
		if(Match(LexicalUnit.WHILE))
		if(Cond())
		if(Match(LexicalUnit.DO))
		if(Code())
		if(Match(LexicalUnit.OD))
			return true;
		return false;
	}
	public static boolean For(){
		Send_output(48);
		if(Match(LexicalUnit.FOR))
		if(Match(LexicalUnit.VARNAME))
		if(Match(LexicalUnit.FROM))
		if(ExprArith())
		if(Match(LexicalUnit.BY))
		if(ExprArith())
		if(Match(LexicalUnit.TO))
		if(ExprArith())
		if(Match(LexicalUnit.DO))
		if(Code())
		if(Match(LexicalUnit.OD))
			return true;
		return false;
	}
	public static boolean Print(){
		Send_output(50);
		if(Match(LexicalUnit.PRINT))
		if(Match(LexicalUnit.LEFT_PARENTHESIS))
		if(Match(LexicalUnit.VARNAME))
		if(Match(LexicalUnit.RIGHT_PARENTHESIS)){
			stack.add("%"+unnamedVariable+" = load i32* "+stackOfVariables.get(stackOfVariables.size()-1));
			stack.add("call @println(i32 %"+unnamedVariable+")");
			unnamedVariable+=1;
			return true;
		}
		return false;
	}
	public static boolean Read(){
		Send_output(51);
		if(Match(LexicalUnit.READ))
		if(Match(LexicalUnit.LEFT_PARENTHESIS))
		if(Match(LexicalUnit.VARNAME))
		if(Match(LexicalUnit.RIGHT_PARENTHESIS))
			return true;
		return false;
	}
};
