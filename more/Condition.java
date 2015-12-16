import java.util.ArrayList;

public class Condition{
  /*
  Personnal container for condition. It contain all the information about it.
  */
  public boolean matched;
  public ArrayList<String> simpleCond = new ArrayList<String>();
  public int pointer = 0; //cursor for the simpleCond

  public Condition(){
    /*
    Constructor. Nothing to see here move along
    */
  }
  public void setMatched(boolean match){
    /*
    Set the value for the boolean matched

    Arguments : boolean
    Return : none
    */
    matched=match;
  }
  public boolean getMatched(){
    /*
    return the value of the boolean matched

    Arguments : none
    Return : boolean
    */
    return matched;
  }
  public void addCond(String cond){
    /*
    add a part of a condition to the container. This part will be a variable or
    a comparator operator

    Arguments : String
    Return : none
    */
    simpleCond.add(cond);
  }
  public ArrayList<String> getAllCond(){
    /*
    returns all the conditions

    Arguments : none
    Return : ArrayList<String>
    */
    return simpleCond;
  }
  public boolean moreCond(){
    /*
    return true if there are more conditions in the container
    return false otherwise

    Arguments : none
    Return : boolean
    */
    return pointer<simpleCond.size();
  }
  public ArrayList<String> getCond(){
    /*
    return all the element necessary to generate a condition into an ArrayList
    of string.

    Arguments : none
    Return : ArrayList<String>
    */
    ArrayList<String> cond = new ArrayList<String>();
    cond.add(simpleCond.get(pointer));
    cond.add(simpleCond.get(pointer+1)); // there is always at least two things to catch
    if(simpleCond.get(pointer).equals("or") || simpleCond.get(pointer).equals("and"))
      pointer+=2;
    else{
      cond.add(simpleCond.get(pointer+2));
      pointer+=3;
    }
    return cond;
  }
  public void resetPointer(){
    /*
    reset the value of the pointer to zero

    Arguments : none
    Return : none
    */
    pointer = 0;
  }
  public void setPointer(int i){
    /*
    set the value of the pointer

    Arguments : interger
    Return : none
    */
    pointer = i;
  }
  public void addCond(Condition list){
    /*
    add to the container all the conditions contained in the object Condition

    Arguments : Condition
    Return : none
    */
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
