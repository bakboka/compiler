class ParseError extends Exception{
  /**
   * handles the error when parsing
   */
  public ParseError(Symbol expected){
    /**
     * Generate an error explaining what went wrong
     *
     * @param expected the symbol expected by the parser
     */
    System.out.println();
    System.out.println("Unexpected symbol at line : "+expected.getLine()+" column : "+expected.getColumn());
    System.out.println("Expected symbol : "+expected.getType());
  }
}
