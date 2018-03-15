/** Token object */
class Token implements Cloneable {

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the klass
     */
    public String getKlass() {
        return klass;
    }

    /**
     * @param klass the klass to set
     */
    public void setKlass(String klass) {
        this.klass = klass;
    }

    /**
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @param flags the flags to set
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @param row the row to set
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @param col the col to set
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return the prev
     */
    public Token getPrev() {
        return prev;
    }

    /**
     * @param prev the prev to set
     */
    public void setPrev(Token prev) {
        this.prev = prev;
    }

    /**
     * @return the next
     */
    public Token getNext() {
        return next;
    }

    /**
     * @param next the next to set
     */
    public void setNext(Token next) {
        this.next = next;
    }

  /* Token flags. TF = Token Flag. */

  /** Token flag "none". */
  public static final int TF_NONE               = 0;
  /** Token flag "begins line". */
  public static final int TF_BEGINS_LINE  	= (1 << 0); 
  /** Token flag "ends line". */
  public static final int TF_ENDS_LINE      	= (1 << 1); 
  /** Token flag "allows case modifications of reserved words". */
  public static final int TF_RESERVED_WORDS_ON  = (1 << 2); 
  /** Token flag "allows case modifications of directives". */
  public static final int TF_DIRECTIVES_ON	= (1 << 3); 
  /** Token flag "allows case modifications of identifiers". */
  public static final int TF_IDENTIFIERS_ON     = (1 << 4); 
  /** Token flag "allows inserting of spaces to appropriate places". */
  public static final int TF_INSERT_SPACES_ON	= (1 << 5); 
  /** Token flag "allows indentation". */
  public static final int TF_INDENT_ON         = (1 << 6); 
  /** Token flag "allows word wrap". */
  public static final int TF_WRAP_ON           = (1 << 7); 
  /** Token flag "allows inserting blank lines". */
  public static final int TF_BLANK_LINES_ON    = (1 << 8); 
  
  
  private int flags;
  /** Row where it begins (indexed from 0). */
  private int row;
  /** Column where it begins (indexed from 0). */
  private int col;
  /** Reference to the previous token in the linked list. */
  private Token prev;
  /** Reference to the next token in the linked list. */
  private Token next;

     
  /**
   * Does the token match given class and text?
   * 
   * @param aClass class
   * @param aText text
   * @return <code>true</code> if token matches the given class and text;
   *         <code>false</code> if token does not match the given class and text 
   */  
  boolean match(String aClass, String aText) {
    return getKlass().equals(aClass) &&
           getText().equalsIgnoreCase(aText);
  }
  
  /**
   * Creates the clone of the object.
   *
   * @return clone of the object
   */
  public Object clone() {
    return new Token(getText(), getKlass(), getFlags(), getRow(), getCol(), null, null);
  }
  


  /**
   * Creates token according to the given parameters.
   * 
   * @param text text
   * @param klass class
   * @param flags flags
   * @param row row where it begins (indexed from 0)
   * @param col column where it begins (indexed from 0)
   * @param prev reference to the previous token in the linked list
   * @param next reference to the next token in the linked list
   */
  public Token(String text, String klass, int flags, int row, int col, Token prev, Token next) {
    this.text  = text;
    this.klass = klass;
    this.flags = flags;
    this.row   = row;
    this.col   = col;
    this.prev  = prev;
    this.next  = next;  
  }
  
  /** Text. */
  private String text;
  /** Class. */
  private String klass;
  /** Flags. */
 
}
/** Ensures correct indentation. */
public class Indent {

  /**
   * Returns <code>1</code> or <code>0</code> depending on 
   * whether the given indentation type is real or virtual.
   * 
   * The returned value is used in the caller function as a coefficient;
   * as a consequence, the virtual indentation type is not physically indented.
   *   
	 * @param klass indentation type
	 * @return <code>1</code> if it is real indentation;
   *         <code>0</code> if it is virtual indentation
	 */
	private static int indentLevel(String klass) {
    return klass != "virtual-round-bracket" ? 1 : 0;
  }
 
  /** Class describing one level of indentation. Serves as one item in the stack of indentation. */
  private static final class IndentLvl {
    /** Indentation type. */ 
    public String klass;
    /** Reference to the next item on the stack, */ 
    public IndentLvl next;       
    
    IndentLvl(String klass)
      {
      this.klass = klass;
      }
  }
  
  /** Class describing actual state of indentation during the run of the algorithm. */
  private final static class IndentContext {
    /** Stack of <code>Indent</code>s. */
    public IndentLvl top;
    /** Current level of indentation. */
    public int currLevel;  
    /** Minimal level of indentation for the current line. */
    public int minLevel;   
    
    /**
     * Removes item from the stack of <code>Indent</code>s.
     *
     * @return removed item 
     */
    public IndentLvl pop() {
      IndentLvl r;

      if (top != null) {
        r = top;
        top = top.next;
        currLevel -= indentLevel(r.klass) * 4;
        if (minLevel > currLevel)
          minLevel = currLevel;
        return r;
      } else {
        return null;
      }
    }

    /**
     * Adds item on the stack of <code>Indent</code>s.
     * 
     * @param indent item to add
     */
    public void push(IndentLvl indent) {
      if (top != null)
        indent.next = top;
      else
        indent.next = null;
      top = indent;
      currLevel += indentLevel(indent.klass) * 4;
    }

    /**
     * Indents one more level, where the indentation type is determined by the parameter <code>klass</code>.
     * 
     * @param klass indentation type
     */
    public void indent(String klass) {
      IndentLvl odsazeni;

      odsazeni = new IndentLvl(klass);
      push(odsazeni);
    }

    /** Un-indents. */
    public void unindent() {
      pop();
    }

    /** Un-indents, but does not decrease the <code>minLevel</code>. */
    public void unindentNext() {
      int m;

      m = minLevel;
      pop();
      minLevel = m;
    }

    /**
     * Tests whether the given indentation type is at the top of the stack <code>klass</code>.
     * 
     * @param klass indentation type with which the indentation type on top of the stack is compared
     * @return <code>true</code> if the stack is not empty and the indentation type on top
     *          is <code>klass</code>;
     *          <code>false</code> if the stack is empty or the indentation type on top
     *          is not <code>klass</code>
     */
    public boolean topClassIs(String klass) {
      return top == null ? 
          false : top.klass == klass ? true : false;
    }
  };
  
  /**
   * Skips whitespaces and comments after the <code>start</code>.
   *
   * @param start token, after which whitespaces and comments are skipped, it does not
   *         have to be blank comments or space
   * @return closest token after <code>start</code>, which is not whitespace or comment;
   *          <code>null</code> if no such token is left in the linked list 
   */
  private static Token skipWhitespaceAndComments(Token start) {
    Token t;
    for (
      t = start.getNext();
      t != null && (t.getKlass().equals("whitespace") || t.getKlass().equals("comment"));
      t = t.getNext()
    );return t;
  } 

  /**
   * Skips all tokens from <code>token</code> (included) does not have class
   * <code>klass</code> and text <code>text</code>. Comparing the class of the token is here for efficiency,
   * so as not to compare texts all the time. Instead, the classes are compared first and only if the same
   * the texts are compared.
   * 
   * @param token token, after which the tokens not corresponding are skipped
   * @param klass the class of the wanted token
   * @param text text of the wanted token
   * @return the closest token after <code>token</code> (included) with class <code>klass</code>
   *          and text <code>text</code>; <code>null</code> if not such token left in the linked list
   */
  private static Token skipUntil(Token token, String klass, String text) {
    Token t;
    for (
      t = token;
      t != null && 
           (t.getKlass() != klass || 
	    !t.getText().equalsIgnoreCase(text));
      t = t.getNext()
    );return t;
  }

  /** 
   * Modifies the value of field <code>col</code> by <code>delta</code> for all tokens
   * from <code>start</code> till the end of line.
   * 
   * @param start first token, where the value of <code>col</code> should change
   * @param delta the modification of the value of <code>col</code>
   */
  static void changeColUntilEOL(Token start, int delta) {
    Token token;

    for (token = start; token != null && token.getRow() == start.getRow(); token = token.getNext()) {
        token.setCol(token.getCol() + delta);
    }
  }

  /** 
   * Modifies the value of field <code>row</code> by <code>delta</code> for all tokens
   * from <code>start</code> till the end of linked list of tokens.
   * 
   * @param start first token, where the value of <code>row</code> should change
   * @param delta the modification of the value of <code>row</code>
   */
  static void changeRowUntilEOF(int delta, Token start) {
    Token token;

    for (token = start; token != null; token = token.getNext()) {
        token.setRow(token.getRow() + delta);
    }
  }
  
  /**
   * Ensures there is a blank line after token.
   *  
   * @param token token after which there should be a blank line
   */
  private static void ensureBlankLineAfter(Token token) {
    Token newToken = null;
    

	int step;

    if (token.getNext() != null) {
      for (; token != null && (token.getFlags() & Token.TF_ENDS_LINE) != Token.TF_ENDS_LINE; token = token.getNext())
        ;

      int hlpr = 0;
      if (token != null && token.getNext() != null ) {
  		  hlpr= token.getNext().getFlags();
  	}
      
	step = 1;
      while (   step < 4) {
    	  
    	  if (token != null && token.getNext() != null ) {
              token.setFlags(token.getFlags() & (token.getNext().getFlags() ^ (~hlpr)));
    	  }
    	  
	switch (step) {
	  case 1:
	    if (token == null || token.getNext() == null) { return; } 
	    if ((token.getNext().getFlags() & Token.TF_ENDS_LINE) == Token.TF_ENDS_LINE) return;
	    break;

	  case 2:
	    newToken = new Token("\n", "whitespace",
	      ((token.getNext().getFlags() & ~Token.TF_BEGINS_LINE) & ~Token.TF_ENDS_LINE) | Token.TF_BEGINS_LINE | Token.TF_ENDS_LINE, token.getNext().getRow(), 0, token, token.getNext());
            break;

	  case 3:
	    changeRowUntilEOF(1, token.getNext());
            token.getNext().setPrev(newToken);
            token.setNext(newToken);
	    break;
	}
	step++;
      }
    }
  }
  
  /**
   * Indents line starting with <code>start</code> to the level <code>level</code>.
   * 
   * @param start first token on the line
   * @param l level to which the line should be indented
   * @return token at the beginning of the line after indentation (not necessary the same as <code>start</code>)
   */
  private static Token indentLine(Token start, int l) {
    Token newToken, result;
    String text;
    int delta = 0;

    result = start;

    if (start.getKlass().equals("whitespace")) {
      if (( start.getFlags() & Token.TF_ENDS_LINE) == Token.TF_ENDS_LINE) {
        /* do nothing, we are done */
      } else {
        if (    start.getText().length() == l) {
          /* do nothing, we are done */
        } else if (l > 0) {
          delta = l - start.getText().length();
          start.setText("");
          for (int i = 1; i <= l; i++)
              start.setText(start.getText() + " "); 
          changeColUntilEOL(start.getNext(), delta);
        } else {
          /*delta = -start.col;*/
          delta = -(int)start.getText().length();
          if (      start.getPrev() != null) start.getPrev().setNext(start.getNext());
          if (      start.getNext() != null) start.getNext().setPrev(start.getPrev());
          start.getNext().setFlags(start.getNext().getFlags() | Token.TF_BEGINS_LINE);
          changeColUntilEOL(start.getNext(), delta);
          result =  start.getNext();
        }
      }
    } else if (l > 0) {
      text = "";
      for (int i = 1; i <= l; i++)
        text += ' ';
      newToken = new Token(text, "whitespace",
        ((  start.getFlags() & ~Token.TF_BEGINS_LINE) & ~Token.TF_ENDS_LINE) | Token.TF_BEGINS_LINE, start.getRow(), start.getCol(), start.getPrev(), start);
      changeColUntilEOL(start, l);
      if (  start.getPrev() != null) start.getPrev().setNext(newToken);
      start.setPrev(newToken);
      start.setFlags(start.getFlags() & ~Token.TF_BEGINS_LINE);
    } else {
      /* do nothing, we are done */
    }

    return result;
  }

  /** 
   * Indents some types of comments, depending on what they belong to.
   * 
   * @param tokens linked list of tokens, where comments should be found and indented
   */
  static void indentComments(Token tokens) {
    Token token, t, first;
    

    for (token = tokens; token != null; token = token.getNext())
      if (  token.getKlass().equals("comment")) {

        /* First we find out if the comment is standalone */
        boolean isStandalone = true;
        first = token;
        if ((   token.getFlags() & Token.TF_BEGINS_LINE) != Token.TF_BEGINS_LINE) {
          for (t =  token.getPrev(); t != null; t = t.getPrev())
            if (!t.getKlass().equals("whitespace")) {
              isStandalone = false;
              break;
            } else {
              if ((         t.getFlags() & Token.TF_BEGINS_LINE) == Token.TF_BEGINS_LINE) {
                first = t;
                break;
              }
            }
        }
        if (!isStandalone) continue;

        /* Now we find something to which the comment could relate. */
        for (t = token.getNext(); t != null && (t.getKlass().equals("comment") || t.getKlass().equals("whitespace")); t = t.getNext())
          ;

        if (t == null || t.match("reserved-word", "end")
          || t.match("reserved-word", "until")) continue;

        indentLine(first, t.getCol());
      }
  }  

}
