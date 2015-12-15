import java.io.*;
import java.util.ArrayList;

public class Generator{
  private int ifCounter=0,condCounter=0,endCounter=0,loopCount=0,endLoopCount=0;
  private PrintWriter writer; // the llvm code stored
  private static ArrayList<String> stack = new ArrayList<String>(); //stack for things

  public Generator() throws FileNotFoundException,IOException{
    writer = new PrintWriter("code.ll");
  }
  public void printBegin(ArrayList<Symbol> id){
    writer.println("declare i32 @putchar(i32)");
		writer.println("declare i32 @getchar()");
    writer.println("define void @project(){");
		writer.println("	entry:");
    for(int i=0;i<id.size();i++){ // allocating memory for all variables
			writer.println("		%"+id.get(i).getValue()+" = alloca i32");
		}
  }
  public void printEnd(){
    writer.println("    ret void");
    writer.println("}");
		writer.close();
  }
  public void assign(String stock,String value){
    writer.println("		store i32 "+value+",i32* "+stock);
  }
  public void addition(String stock,String left,String right,boolean sub){
    if(sub)
      writer.println("    "+stock+" = sub i32 "+left+ ","+right);
    else
      writer.println("    "+stock+" = add i32 "+left+ ","+right);
  }
  public void load(String stock,String var){
    writer.println("		"+stock+" = load i32* %"+var);
  }
  public void multiply(String stock,String left,String right,boolean division){
    if(division)
      writer.println("		"+stock+" = div i32 "+left+ ","+right);
    else
      writer.println("		"+stock+" = mul i32 "+left+ ","+right);
  }
  public void cond(String comp, String var1, String var2){
    writer.println("		%cond"+condCounter+" = icmp "+comp+" i32 "+var1+","+var2);
    stack.add("%cond"+condCounter);
    condCounter+=1;
  }
  public void ifblock(){
    jump(false);
    writer.println("	if_true"+ifCounter+":");
    ifCounter+=1;
  }
  public void endIf(){
    if(ifCounter == endCounter){
			writer.println("		br label %end"+(ifCounter-1));
			writer.println("	end"+(ifCounter-1)+":");
		}
  }
  public void elseIf(){
    writer.println("		br label %end"+(ifCounter-1));
		writer.println("	else"+(ifCounter-1)+":");
  }
  public void incrementEnd(){
    endCounter+=1;
  }
  public void jump(boolean loop){
    if(loop)
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"loop"+loopCount+", label %endLoop"+loopCount);
    else
      writer.println("		br i1 "+stack.get(stack.size()-1)+", label %"+"if_true"+ifCounter+", label %end"+ifCounter);
    stack.remove(stack.size()-1);
  }
  public void and(){
    writer.println("		%cond"+condCounter+" = icmp eq i1 "+stack.get(stack.size()-1)+","+stack.get(stack.size()-2));
    stack.remove(stack.size()-1);
    stack.remove(stack.size()-1);
    stack.add("%cond"+condCounter);
    condCounter+=1;
  }
  public void beginDo(){
    writer.println("		br i1 "+stack.get(stack.size()-1)+", label %loop"+loopCount+", label %endLoop"+loopCount);
    writer.println("	loop"+loopCount+":");
		loopCount+=1;
    stack.remove(stack.size()-1);
  }
  public void od(){
    writer.println("		br i1 "+stack.get(stack.size()-1)+", label %loop"+loopCount+", label %endLoop"+loopCount);
    writer.println("	endLoop"+(loopCount-endLoopCount-1)+":");
		endLoopCount+=1;
    stack.remove(stack.size()-1);
  }
}
