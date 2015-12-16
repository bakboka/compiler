import java.io.*;
import java.util.ArrayList;

public class Generator{
  /*
  This class is in charge of the code generation
  */
  private int ifCounter=0,condCounter=0,endCounter=0,loopCount=0,endLoopCount=0;
  private int orCounter=0;
  private PrintWriter writer; // the llvm code stored
  private static ArrayList<String> stack = new ArrayList<String>(); //stack for conditions

  public Generator() throws FileNotFoundException,IOException{
    writer = new PrintWriter("code.ll");
  }
  public void printBegin(ArrayList<Symbol> id){
    /*
    Print the header of the llvm code.
    Print function declaration and
    allocate memory for each variable
    */
    writer.println("declare i32 @putchar(i32)");
		writer.println("declare i32 @getchar()");
    writer.println("define void @main(){");
		writer.println("	entry:");
    for(int i=0;i<id.size();i++){ // allocating memory for all variables
			allocate((String)id.get(i).getValue());
		}
  }
  public void allocate(String name){
    /*
    Print the allocation of memeory for a variable
    */
    writer.println("		%"+name+" = alloca i32");
  }
  public void printEnd(){
    /*
    Print the foot of the llvm code.
    Print the return and close the function
    */
    writer.println("    ret void");
    writer.println("}");
		writer.close();
  }
  public void assign(String stock,String value){
    /*
    Print the storage of a value into a variable
    */
    writer.println("		store i32 "+value+",i32* "+stock);
  }
  public void addition(String stock,String left,String right,boolean sub){
    /*
    Print the addition and substraction code
    */
    if(sub)
      writer.println("    "+stock+" = sub i32 "+left+ ","+right);
    else
      writer.println("    "+stock+" = add i32 "+left+ ","+right);
  }
  public void load(String stock,String var){
    /*
    Print the loading of a variable into a unnamed variable
    */
    writer.println("		"+stock+" = load i32* "+var);
  }
  public void multiply(String stock,String left,String right,boolean division){
    /*
    Print the multiplication and division code
    */
    if(division)
      writer.println("		"+stock+" = div i32 "+left+ ","+right);
    else
      writer.println("		"+stock+" = mul i32 "+left+ ","+right);
  }
  public void cond(String comp, String var1, String var2){
    /*
    Print the condition code
    */
    writer.println("		%cond"+condCounter+" = icmp "+comp+" i32 "+var1+","+var2);
    stack.add("%cond"+condCounter); //adding to the stack
    condCounter+=1;
  }
  public void ifblock(){
    /*
    Print the header for an if block
    */
    jump(false);
    writer.println("	if_true"+ifCounter+":");
    ifCounter+=1;
  }
  public void endIf(){
    /*
    Print the footer for an if block
    */
    if(ifCounter == endCounter){
			writer.println("		br label %end"+(ifCounter-1));
			writer.println("	end"+(ifCounter-1)+":");
		}
  }
  public void elseIf(){
    /*
      Print the footer for an if block in case of an else
    */
    writer.println("		br label %end"+(ifCounter-1));
		writer.println("	else"+(ifCounter-1)+":");
  }
  public void incrementEnd(){
    /*
    increment the number of ending loop
    */
    endCounter+=1;
  }
  public void jump(boolean loop){
    /*
    Print the jump code. Jump to a loop block in case of loop or if block otherwise
    */
    if(loop)
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"loop"+loopCount+", label %endLoop"+loopCount);
    else
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"if_true"+ifCounter+", label %end"+ifCounter);
    stack.remove(stack.size()-1);
  }
  public void or(boolean loop){
    /*
      Print the or blocks. The jump in case of false need to be to a or block
      in order to test more conditions
    */
    if(loop)
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"loop"+loopCount+", label %or"+orCounter);
    else
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"if_true"+ifCounter+", label %or"+orCounter);
    writer.println("  or"+orCounter+":");
    orCounter+=1;
    stack.remove(stack.size()-1);
  }
  public void and(){
    /*
    Print the code condition concerning the and. This function will generate
    condition matching the other one two by two
    */
    writer.println("		%cond"+condCounter+" = icmp eq i1 "+stack.get(stack.size()-1)+","+stack.get(stack.size()-2));
    stack.remove(stack.size()-1);
    stack.remove(stack.size()-1);
    stack.add("%cond"+condCounter);
    condCounter+=1;
  }
  public void beginDo(){
    /*
    Print the header for a do block. This print the jump and the block
    */
    writer.println("		br i1 "+stack.get(stack.size()-1)+", label %loop"+loopCount+", label %endLoop"+loopCount);
    writer.println("	loop"+loopCount+":");
		loopCount+=1;
    stack.remove(stack.size()-1);
  }
  public void od(){
    /*
    Print the footer for a do block. This print the jump and the block
    */
    writer.println("		br i1 "+stack.get(stack.size()-1)+", label %loop"+(loopCount-endLoopCount-1)+", label %endLoop"+(loopCount-endLoopCount-1));
    writer.println("	endLoop"+(loopCount-endLoopCount-1)+":");
    stack.remove(stack.size()-1);
		endLoopCount+=1;
    if(endLoopCount == loopCount) //this means that all the inner loop are over
      endLoopCount = 0; // so we reset the value
  }
  public void read(String var){
    /*
    Print the code for a read
    */
    writer.println("		"+var+" = call i32 @getchar()");
  }
  public void print(String toShow){
    /*
    Print the code for a print
    */
    writer.println("		call i32 @putchar(i32 "+toShow+")");
  }
}
