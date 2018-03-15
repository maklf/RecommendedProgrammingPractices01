/** Token object */
class Token implements Cloneable {

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
  
  
  public int flags;
  /** Row where it begins (indexed from 0). */
  public int row;
  /** Column where it begins (indexed from 0). */
  public int col;
  /** Reference to the previous token in the linked list. */
  public Token prev;
  /** Reference to the next token in the linked list. */
  public Token next;

     
  /**
   * Does the token match given class and text?
   * 
   * @param aClass class
   * @param aText text
   * @return <code>true</code> if token matches the given class and text;
   *         <code>false</code> if token does not match the given class and text 
   */  
  boolean match(String aClass, String aText) {
    return klass.equals(aClass) &&
           text.equalsIgnoreCase(aText);
  }
  
  /**
   * Creates the clone of the object.
   *
   * @return clone of the object
   */
  public Object clone() {
    return new Token(text, klass, flags, row, col, null, null);
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
  public String text;
  /** Class. */
  public String klass;
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
      t = start.next;
      t != null && (t.klass.equals("whitespace") || t.klass.equals("comment"));
      t = t.next
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
           (t.klass != klass || 
	    !t.text.equalsIgnoreCase(text));
      t = t.next
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

    for (token = start; token != null && token.row == start.row; token = token.next) {
      token.col += delta;
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

    for (token = start; token != null; token = token.next) {
      token.row += delta;
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

    if (token.next != null) {
      for (; token != null && (token.flags & Token.TF_ENDS_LINE) != Token.TF_ENDS_LINE; token = token.next)
        ;

      int hlpr = 0;
      if (token != null && token.next != null ) {
  		  hlpr= token.next.flags;
  	}
      
	step = 1;
      while (   step < 4) {
    	  
    	  if (token != null && token.next != null ) {
    		  token.flags = token.flags & (token.next.flags ^ (~hlpr));
    	  }
    	  
	switch (step) {
	  case 1:
	    if (token == null || token.next == null) { return; } 
	    if ((token.next.flags & Token.TF_ENDS_LINE) == Token.TF_ENDS_LINE) return;
	    break;

	  case 2:
	    newToken = new Token("\n", "whitespace",
	      ((token.next.flags & ~Token.TF_BEGINS_LINE) & ~Token.TF_ENDS_LINE) | Token.TF_BEGINS_LINE | Token.TF_ENDS_LINE,
	      token.next.row, 0, token, token.next);
            break;

	  case 3:
	    changeRowUntilEOF(1, token.next);
	    token.next.prev = newToken;
	    token.next = newToken;
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

    if (start.klass.equals("whitespace")) {
      if ((start.flags & Token.TF_ENDS_LINE) == Token.TF_ENDS_LINE) {
        /* do nothing, we are done */
      } else {
        if (start.text.length() == l) {
          /* do nothing, we are done */
        } else if (l > 0) {
          delta = l - start.text.length();
          start.text = "";
          for (int i = 1; i <= l; i++)
            start.text += " "; 
          changeColUntilEOL(start.next, delta);
        } else {
          /*delta = -start.col;*/
          delta = -(int)start.text.length();
          if (start.prev != null) start.prev.next = start.next;
          if (start.next != null) start.next.prev = start.prev;
          start.next.flags |= Token.TF_BEGINS_LINE;
          changeColUntilEOL(start.next, delta);
          result = start.next;
        }
      }
    } else if (l > 0) {
      text = "";
      for (int i = 1; i <= l; i++)
        text += ' ';
      newToken = new Token(text, "whitespace",
        ((start.flags & ~Token.TF_BEGINS_LINE) & ~Token.TF_ENDS_LINE) | Token.TF_BEGINS_LINE,
        start.row, start.col, start.prev, start);
      changeColUntilEOL(start, l);
      if (start.prev != null) start.prev.next = newToken;
      start.prev = newToken;
      start.flags &= ~Token.TF_BEGINS_LINE;
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
    

    for (token = tokens; token != null; token = token.next)
      if (token.klass.equals("comment")) {

        /* First we find out if the comment is standalone */
        boolean isStandalone = true;
        first = token;
        if ((token.flags & Token.TF_BEGINS_LINE) != Token.TF_BEGINS_LINE) {
          for (t = token.prev; t != null; t = t.prev)
            if (!t.klass.equals("whitespace")) {
              isStandalone = false;
              break;
            } else {
              if ((t.flags & Token.TF_BEGINS_LINE) == Token.TF_BEGINS_LINE) {
                first = t;
                break;
              }
            }
        }
        if (!isStandalone) continue;

        /* Now we find something to which the comment could relate. */
        for (t = token.next; t != null && (t.klass.equals("comment") || t.klass.equals("whitespace")); t = t.next)
          ;

        if (t == null || t.match("reserved-word", "end")
          || t.match("reserved-word", "until")) continue;

        indentLine(first, t.col);
      }
  }  

}
