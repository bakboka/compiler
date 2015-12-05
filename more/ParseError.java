class ParseError extends Exception{
  public ParseError(Symbol expected){
    System.out.println();
    System.out.println("Unexpected symbol : "+expected.getType()+" at line : "+expected.getLine());
  }
}
