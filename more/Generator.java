import java.io.*;
import java.util.ArrayList;

public class Generator{
  private int ifCounter=0,condCounter=0,endCounter=0,loopCount=0,endLoopCount=0;
  private PrintWriter writer; // the llvm code stored

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
}
