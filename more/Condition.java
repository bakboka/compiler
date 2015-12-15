import java.util.ArrayList;

public class Condition{
  /*
  Personnal container for condition. It contain all the information about it.
  */
  public boolean matched;
  public ArrayList<String> simpleCond = new ArrayList<String>();
  public int pointer = 0;

  public Condition(){
  }
  public void setMatched(boolean match){
    matched=match;
  }
  public boolean getMatched(){
    return matched;
  }
  public void addCond(String cond){
    simpleCond.add(cond);
  }
  public ArrayList<String> getAllCond(){
    return simpleCond;
  }
  public boolean moreCond(){
    return pointer<simpleCond.size();
  }
  public ArrayList<String> getCond(){
    ArrayList<String> cond = new ArrayList<String>();
    cond.add(simpleCond.get(pointer));
    cond.add(simpleCond.get(pointer+1));
    if(simpleCond.get(pointer).equals("or") || simpleCond.get(pointer).equals("and"))
      pointer+=2;
    else{
      cond.add(simpleCond.get(pointer+2));
      pointer+=3;
    }
    return cond;
  }
  public void resetPointer(){
    pointer = 0;
  }
  public void setPointer(int i){
    pointer = i;
  }
  public void addCond(Condition list){
    int i;
    ArrayList<String> multiCond;
    while (list.moreCond()){
      multiCond = list.getCond();
      i = 0;
      while (i<multiCond.size()){
        simpleCond.add(multiCond.get(i));
        i++;
      }
    }
  }
}
