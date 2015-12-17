import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Main {

	private static ArrayList<Symbol> listSym = new ArrayList<Symbol>(); //list of symbol
	private static int curToken = 0;
	private static Generator code; //code generator
	private static ArrayList<String> stack = new ArrayList<String>(); //stack of variables
	private static ArrayList<Symbol> id = new ArrayList<Symbol>(); //table of symbol
	private static int unnamedVariable = 0,andCounter=0;

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
		Parser();
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
	public static void Parser() throws ParseError{
		/*
		Parse the input code. Throw an error if an unexpected token is find

		Arguments : none
		Return : none
		*/
			if(!Goal())
				throw new ParseError(listSym.get(curToken));
	}
	private static boolean Match(LexicalUnit t){
		/*
		try to match the current token with what is expected. Return true if it
		is matched

		Arguments : LexicalUnit token
		Return : boolean
		*/
		if (listSym.get(curToken).getType().equals(t)){
			curToken++;
			return true;
		}
		else
			return false;
	}
	private static void Send_output(int no){
		//System.out.print(no + " ");
	}
	private static void writeCond(Condition cond,boolean loop,boolean repeat){
	/*
	write all the conditions contained by the object Condition

	Arguments : Condition the container, boolean loop, boolean repeat
	Return : none
	*/
	ArrayList<String> temp = new ArrayList<String>();
	int i=0; //counter for the fifo from the stack
	int num=0; //number of "and"
	if(repeat) // if it is a repeat then we need to inverse the stack to get a fifo
		num = cond.getAnd();
	while(cond.moreCond()){ // until no more conditions
		temp = cond.getCond(); // get the condition
		if(temp.get(0).equals("or"))
			code.or(loop,repeat); // print an or block
		else if (temp.get(0).equals("and"))
			code.and(); // generate the and conditions
		else if(!repeat)
			code.cond(temp.get(1),temp.get(0),temp.get(2)); //original condition
		else{ // repeated condition
			writeRepeatedCond(num,i,temp);
		}
		i++;
	}
	cond.resetPointer();
}
	private static void writeRepeatedCond(int num,int i, ArrayList<String> temp){
		/*
		write the conditions that appears at the end of a loop

		Arguments : int num, int i, ArrayList temp
		Return : none
		*/
		if(stack.size()>0){ // we compare a variable
			if(num!=0){ // we need to do a fifo of the variables. The stack have the reverse order
				code.load("%"+unnamedVariable,stack.get(stack.size()-num+i)); //loading the variable to compare
				stack.remove(stack.size()-num+i);
			}
			else{ // normal case ie. one condition
				code.load("%"+unnamedVariable,stack.get(stack.size()-1)); //loading the variable to compare
				stack.remove(stack.size()-1);
			}
			code.cond(temp.get(1),"%"+unnamedVariable,temp.get(2));
			unnamedVariable+=1;
		}
		else // we compare two int
			code.cond(temp.get(1),temp.get(0),temp.get(2));
	}
	private static boolean Goal(){
		Send_output(1);
		if(Program())
		if(Match(LexicalUnit.END_OF_STREAM))
			return true;
		return false;
	}
	private static boolean Program(){
		Send_output(2);
		if(Match(LexicalUnit.BEG))
		if(Code())
		if(Match(LexicalUnit.END))
			return true;
		return false;
	}
	private static boolean Code(){
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
	private static boolean InstList(){
		Send_output(5);
		if(Instruction())
		if(EndList())
			return true;
		return false;
	}
	private static boolean EndList(){
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
	private static boolean Instruction(){
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
	private static boolean Assign(){
		Send_output(14);
		if(Match(LexicalUnit.VARNAME)){
			stack.add("%"+(String)listSym.get(curToken-1).getValue());
			if(Match(LexicalUnit.ASSIGN))
			if(ExprArith(false)){
				code.assign(stack.get(stack.size()-2),stack.get(stack.size()-1));//left the variable, right the result of the expr
				stack.remove(stack.size()-1);
				stack.remove(stack.size()-1);
				return true;
			}
		}
		return false;

	}
	private static boolean ExprArith(boolean loop){
		Send_output(15);
		if(Factor(loop))
		if(Terms())
			return true;
		return false;
	}
	private static boolean Terms(){
		switch(listSym.get(curToken).getType()){
		case PLUS:
		case MINUS:
			Send_output(17);
			ArrayList<Boolean> op = AddSub();
			if(op.get(0))
			if(ExprArith(false)){
				code.addition("%"+unnamedVariable,stack.get(stack.size()-2),stack.get(stack.size()-1),op.get(1));
				stack.remove(stack.size()-1); // poping the variable. It is useless now
				stack.remove(stack.size()-1); // poping the variable. It is useless now
				stack.add("%"+unnamedVariable);
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
	private static boolean A1(boolean loop){
		switch(listSym.get(curToken).getType()){
		case VARNAME:
			Send_output(22);
			if(Match(LexicalUnit.VARNAME)){
				if(loop)
					stack.add("%"+(String)listSym.get(curToken-1).getValue()); //we need to remember to regenerate condition for loops
				code.load("%"+unnamedVariable,"%"+(String)listSym.get(curToken-1).getValue());
				stack.add("%"+unnamedVariable);
				unnamedVariable+=1;
				return true;
			}
			break;
		case NUMBER:
			Send_output(23);
			if(Match(LexicalUnit.NUMBER)){
				stack.add((String)listSym.get(curToken-1).getValue()); // adding the value to the stack
				return true;
			}
			break;
		case LEFT_PARENTHESIS:
			Send_output(24);
			if(Match(LexicalUnit.LEFT_PARENTHESIS))
			if(ExprArith(false))
			if(Match(LexicalUnit.RIGHT_PARENTHESIS))
				return true;
			break;
		case MINUS:
			Send_output(25);
			if(Match(LexicalUnit.MINUS))
			if(A1(loop))
				return true;
		}
		return false;
	}
	private static ArrayList<Boolean> AddSub(){
		ArrayList<Boolean> sub = new ArrayList<Boolean>();
		switch(listSym.get(curToken).getType()){
		case PLUS:
			Send_output(26);
			if(Match(LexicalUnit.PLUS)){
				sub.add(true); // we matched
				sub.add(false);//not a substraction
				return sub;
			}
			break;
		case MINUS:
			Send_output(27);
			if(Match(LexicalUnit.MINUS)){
				sub.add(true);//we matched
				sub.add(true);//substraction
				return sub;
			}
		}
		sub.add(false);//nothing matched
		return sub;
	}
	private static boolean Factor(boolean loop){
		Send_output(18);
		if(A1(loop))
		if(Factors(loop))
			return true;
		return false;
	}
	private static boolean Factors(boolean loop){
		switch(listSym.get(curToken).getType()){
		case TIMES:
		case DIVIDE:
			Send_output(20);
			ArrayList<Boolean> op = MultiDiv();
			if(op.get(0))
			if(Factor(loop)){
				code.multiply("%"+unnamedVariable,stack.get(stack.size()-2),stack.get(stack.size()-1),op.get(1));
				stack.remove(stack.size()-1); // poping the variable. It is useless now
				stack.remove(stack.size()-1); // poping the variable. It is useless now
				stack.add("%"+unnamedVariable);
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
			if(A1(loop))
			if(Factors(loop))
				return true;
		}
		return false;
	}
	private static ArrayList<Boolean> MultiDiv(){
		ArrayList<Boolean> div = new ArrayList<Boolean>();
		switch(listSym.get(curToken).getType()){
		case TIMES:
			Send_output(28);
			if(Match(LexicalUnit.TIMES)){
				div.add(true);//we matched
				div.add(false);//not a division
				return div;
			}
			break;
		case DIVIDE:
			Send_output(29);
			if(Match(LexicalUnit.DIVIDE)){
				div.add(true);//we matched
				div.add(true);//a division
				return div;
			}
		}
		div.add(false); // nothing was matched
		return div;
	}
	private static Condition Cond(boolean loop){
		Condition listCond = new Condition();
		Send_output(30);
		if(B1(listCond,loop))
		if(B2(listCond,loop)){
			listCond.setMatched(true); //we matched
			return listCond;
		}
		listCond.setMatched(false); //nohing matched
		return listCond;
	}
	private static boolean B1(Condition cond,boolean loop){
		switch(listSym.get(curToken).getType()){
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
		case MINUS:
			Send_output(31);
			if(SimpleCond(cond,loop))
				return true;
			break;
		case NOT:
			Send_output(32);
			if(Match(LexicalUnit.NOT))
			if(SimpleCond(cond,loop))
				return true;
		}
		return false;
	}
	private static boolean B2(Condition cond,boolean loop){
		switch(listSym.get(curToken).getType()){
		case OR:
			Send_output(33);
			if(Match(LexicalUnit.OR)){
				Condition subCond = Cond(loop);
				if(subCond.getMatched()){
					cond.addCond(subCond); //adding the previous condition to the container
					cond.addCond("or");
					if(loop) //so we know were to jump next
						cond.addCond("loop");
					else
						cond.addCond("if");
					return true;
				}
			}
			break;
		case AND:
		case VARNAME:
		case NUMBER:
		case LEFT_PARENTHESIS:
		case MINUS:
		case DO:
		case THEN:
			Send_output(34);
			if(And(cond,loop))
				return true;
		}
		return false;
	}
	private static boolean And(Condition oldCond,boolean loop){
		switch(listSym.get(curToken).getType()){
		case AND:
			Send_output(35);
			if(Match(LexicalUnit.AND)){
				Condition cond = Cond(loop);
				if(cond.getMatched()){
					oldCond.addCond(cond);
					oldCond.addCond("and");
					oldCond.addCond(" "); // nothing to add but easier to manage
					return true;
				}
			}
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
	private static boolean SimpleCond(Condition cond,boolean loop){
		Send_output(37);
		if(ExprArith(loop)){
			cond.addCond(stack.get(stack.size()-1)); //add the result of the expr
			stack.remove(stack.size()-1);
			if(Comp()){
				cond.addCond(stack.get(stack.size()-1));//add the comparator
				stack.remove(stack.size()-1);
				if(ExprArith(false)){
					cond.addCond(stack.get(stack.size()-1));//add the result of the expr
					stack.remove(stack.size()-1);
					if(loop)
						andCounter+=1; // one more condition
					return true;
				}
			}
		}
		return false;
	}
	private static boolean If(){
		Send_output(38);
		if(Match(LexicalUnit.IF)){
			Condition cond = Cond(false);//not a loop
			if(cond.getMatched())
			if(Match(LexicalUnit.THEN)){
				writeCond(cond,false,false);//not a loop,not a repeated cond
				code.ifblock();
				if(Code())
				if(EndIf()){
					code.endIf();
					return true;
				}
			}
		}
		return false;
	}
	private static boolean EndIf(){
		switch(listSym.get(curToken).getType()){
		case FI:
			Send_output(39);
			if(Match(LexicalUnit.FI)){
				code.elseIf();
				code.incrementEnd();
				return true;
			}
			break;
		case ELSE:
			Send_output(40);
			if(Match(LexicalUnit.ELSE)){
				code.elseIf();
				if(Code())
				if(Match(LexicalUnit.FI)){
					code.incrementEnd();
					return true;
				}
			}
		}
		return false;
	}
	private static boolean Comp(){
		switch(listSym.get(curToken).getType()){
		case EQUAL:
			Send_output(41);
			if(Match(LexicalUnit.EQUAL)){
				stack.add("eq");
				return true;
			}
			break;
		case GREATER_EQUAL:
			Send_output(42);
			if(Match(LexicalUnit.GREATER_EQUAL)){
				stack.add("sge");
				return true;
			}
			break;
		case GREATER:
			Send_output(43);
			if(Match(LexicalUnit.GREATER)){
				stack.add("sgt");
				return true;
			}
			break;
		case SMALLER_EQUAL:
			Send_output(44);
			if(Match(LexicalUnit.SMALLER_EQUAL)){
				stack.add("sle");
				return true;
			}
			break;
		case SMALLER:
			Send_output(45);
			if(Match(LexicalUnit.SMALLER)){
				stack.add("slt");
				return true;
			}
			break;
		case DIFFERENT:
			Send_output(46);
			if(Match(LexicalUnit.DIFFERENT)){
				stack.add("ne");
				return true;
			}
		}
		return false;
	}
	private static boolean While(){
		Send_output(47);
		if(Match(LexicalUnit.WHILE)){
			Condition cond = Cond(true);//a loop
			if(cond.getMatched())
			if(Match(LexicalUnit.DO)){
				cond.setAnd(andCounter); // keep the number of cond in memory
				andCounter=0; //reset
				writeCond(cond,true,false);//a loop, not a repeated cond
				code.beginDo();
				if(Code())
				if(Match(LexicalUnit.OD)){
					writeCond(cond,true,true);//a loop, repeated cond
					code.od();
					return true;
				}
			}
		}
		return false;
	}
	private static boolean For(){
		Send_output(48);
		Condition cond = new Condition();
		if(Match(LexicalUnit.FOR))
		if(Match(LexicalUnit.VARNAME)){
			stack.add("%"+(String)listSym.get(curToken-1).getValue());
			if(Match(LexicalUnit.FROM))
			if(ExprArith(false)){
				code.assign(stack.get(stack.size()-2),stack.get(stack.size()-1));//store the expr into the var
				stack.remove(stack.size()-1); //only one time because we need to remember the var name
				if(Match(LexicalUnit.BY))
				if(ExprArith(false))
				if(Match(LexicalUnit.TO))
				if(ExprArith(false)){
					beginFor(); //allocate and store variables for the loop
					cond.addCond(stack.get(stack.size()-1));
					stack.remove(stack.size()-1);
					//stack = varname,to,by,unnamed
					cond.addCond("sle");// do if lesser or equal
					cond.addCond(stack.get(stack.size()-1));
					stack.remove(stack.size()-1);
					//stack = varname,to,by
					writeCond(cond,true,false); // a loop, not a repeated cond
					code.beginDo();
					if(Match(LexicalUnit.DO))
					if(Code()){
						endFor(); // write the condition to stay in the loop
						writeCond(cond,true,true); //a loop, a repeated cond
						code.od();
						if(Match(LexicalUnit.OD))
							return true;
					}
				}
			}
		}
		return false;
	}
	private static void beginFor(){
		/*
		allocate and store the variables needed to execute the for loop

		Arguments : none
		Return : none
		*/
		//stack = var
		code.allocate(""+unnamedVariable);//"" to force the conversion to string
		code.assign("%"+unnamedVariable,stack.get(stack.size()-1)); // we store the "to" value
		stack.remove(stack.size()-1);
		unnamedVariable+=1;
		code.allocate(""+unnamedVariable);//"" to force the conversion to string
		code.assign("%"+unnamedVariable,stack.get(stack.size()-1)); // we store the "by" value
		stack.remove(stack.size()-1);
		stack.add("%"+(unnamedVariable-1));//to
		stack.add("%"+unnamedVariable);//by
		unnamedVariable+=1;
		//stack = varname,to,by
		code.load("%"+unnamedVariable,"%"+(unnamedVariable-2)); //load to
		stack.add("%"+unnamedVariable);//last two elements are unnamed and our var
		//stack = varname,to,by,unnamed
		unnamedVariable+=1;
		code.load("%"+unnamedVariable,stack.get(stack.size()-4)); //load var
		stack.add("%"+unnamedVariable);//last two elements are unnamed and our var
		//stack = varname,to,by,unnamed,unnamed
		unnamedVariable+=1;
	}
	private static void endFor(){
		/*
		Load and do the arithmetics necessary to execute the next for loop

		Arguments : none
		Return : none
		*/
		code.load("%"+unnamedVariable,stack.get(stack.size()-1)); // load by
		unnamedVariable+=1;
		stack.remove(stack.size()-1);
		//stack = var, to
		code.load("%"+unnamedVariable,stack.get(stack.size()-2)); // load the new value of var
		unnamedVariable+=1;
		stack.remove(stack.size()-1);
		//stack = var
		code.addition("%"+unnamedVariable,"%"+(unnamedVariable-1),"%"+(unnamedVariable-2),false);// add the by to var
		code.assign(stack.get(stack.size()-1),"%"+unnamedVariable);//store the new value of var
		unnamedVariable+=1;
	}
	private static boolean Print(){
		Send_output(50);
		if(Match(LexicalUnit.PRINT))
		if(Match(LexicalUnit.LEFT_PARENTHESIS))
		if(Match(LexicalUnit.VARNAME)){
			code.load("%"+unnamedVariable,"%"+(String)listSym.get(curToken-1).getValue());
			code.print("%"+unnamedVariable);
			unnamedVariable+=2;
			if(Match(LexicalUnit.RIGHT_PARENTHESIS))
				return true;
		}
		return false;
	}
	private static boolean Read(){
		Send_output(51);
		if(Match(LexicalUnit.READ))
		if(Match(LexicalUnit.LEFT_PARENTHESIS))
		if(Match(LexicalUnit.VARNAME)){
			code.read("%"+unnamedVariable);
			code.assign("%"+(String)listSym.get(curToken-1).getValue(),"%"+unnamedVariable);
			unnamedVariable+=1;
			if(Match(LexicalUnit.RIGHT_PARENTHESIS))
				return true;
		}
		return false;
	}
};
