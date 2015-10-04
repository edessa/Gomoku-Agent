import java.util.*;

public class DenseBoard<T> implements ExpandableBoard<T>
{
  ArrayList<ArrayList<T>> board = new ArrayList<ArrayList<T>>(); //Gomuku board to be used throughout entire project
  int minXBound, minYBound, maxXBound, maxYBound; //Bounds for the grid
  int sequenceLength = 0; //Current longest sequence length
  T fill; //Fill element
  Stack<RowColElem<T>> undo = new Stack<RowColElem<T>>(); //Undo tracker (stack)
  Stack<RowColElem<T>> redo = new Stack<RowColElem<T>>(); //Redo tracker (stack)
  Stack<List<RowColElem<T>>> sequence = new Stack<List<RowColElem<T>>>(); //Sequence tracker (stack of lists)
  Stack<List<RowColElem<T>>> redSeq = new Stack<List<RowColElem<T>>>(); //Redo sequence tracker(stack of lists)
  
  boolean twoD = false; //Boolean variable for determining whether a 2d array is calling set or user is calling set
  
  // Workhorse constructor, create initial space indicated by min/max
  // row/col. Initially any get() should return the fillElem
  // specified. Set up all internal data structures to facilitate
  // longest sequence retrieval, undo/redo capabilities.  The fillElem
  // cannot be null: passing null for this parameter will result in a
  // RuntimeException with the message "Cannot set elements to null"
  //
  // Runtime: O(R * C)
  //   R; number of rows which is maxRow-minRow+1
  //   C; number of cols whcih is maxCol-minCol+1
  public DenseBoard(int minRow, int maxRow, int minCol, int maxCol, T fillElem)
  {
    minXBound = minCol;
    minYBound = minRow;
    maxXBound = maxCol;
    maxYBound = maxRow;
    fill = fillElem;
    
    for(int i = 0; i <= (maxRow-minRow); i++)
    {
      ArrayList<T> board1 = new ArrayList<T>();
      for(int j = 0; j <= (maxCol-minCol); j++)
      {
        board1.add(null);
      }
      board.add(board1); 
    }
  }
  
  // Convenience 1-arg constructor, creates a single cell board with
  // given fill element. The initial extent of the board is a single
  // element at 0,0.  May wish to call the first constructor in this
  // one to minimize code duplication.
  public DenseBoard(T fillElem)
  {
    minXBound = 0;
    minYBound = 0;
    maxXBound = 0;
    maxYBound = 0;
    fill = fillElem;
    board.add(new ArrayList<T>());
    board.get(0).add(null);
  }
  
  // Convenience 2-arg constructor, creates a board with given fill
  // element and copies elements from T 2-D array. Assumes upper left
  // is coordinate 0,0 and lower right is size of 2-D array
  public DenseBoard(T[][] x, T fillElem)
  {
    minXBound = 0;
    minYBound = 0;
    maxXBound = x[0].length-1;
    maxYBound = x.length-1;
    fill = fillElem;
    twoD = true;
    
     for(int i = 0; i <= maxYBound; i++)
    {
      ArrayList<T> bound2 = new ArrayList<T>();
      for(int j = 0; j <= maxXBound; j++)
      {
          bound2.add(null);
         // bound2.add(x[i][j]);
        //   board.get(i).add(x[i][j]); 
      }
      board.add(bound2);
    }
     
    for(int i = 0; i <= maxYBound; i++)
    {
      for(int j = 0; j <= maxXBound; j++)
      {
        if(!x[i][j].equals(fillElem))
          set(i,j, x[i][j]);
         // bound2.add(x[i][j]);
        //   board.get(i).add(x[i][j]); 
      }
    }
    twoD = false;
  }
  
  // Access the extent of the board: all explicitly set elements are
  // within the boards established by these four methods.
  //
  // Target complexity: O(1)
  public int getMinRow()
  {
    return minYBound; 
  }
  
  public int getMaxRow()
  {
    return maxYBound;
  }
  
  public int getMinCol()
  {
    return minXBound; 
  }
  
  public int getMaxCol()
  {
    return maxXBound; 
  }
  
  // Retrieve the fill element for the board.
  //
  // Target complexity: O(1)
  public T getFillElem()
  {
    return fill; 
  }
  
  // Change the fill element for the board. To make this efficient,
  // only change an internal field which dictates what should be
  // returned when an element that has not been explicitly set is
  // requested via a call to get().
  //
  // Target complexity: O(1)
  public void setFillElem(T f)
  {
    fill = f; 
  }
  
  // Retrieve the longest sequence present on the board. If there is a
  // tie, the earliest longest sequence to appear on the board should
  // be returned.  The list returned should be independent of any
  // internal board data structures so that the list can be changed
  // and not affect the board.  This implies a copy of any internal
  // board lists should be made and returned.
  //
  // Target Complexity: O(L) (worst case)
  // L: length of the longest sequence
  
  //There is a sequence stack that tracks any operation performed on the board, and
  //pushes/pops the new (or same) longest sequence depending on an undo/redo/set call. The longest sequence
  //is only looked for tile-by-tile-wise whenever a set(row, col, x) is called, using the row,col parameter
  //in order to determine changes to the new longest sequence.
  public List< RowColElem<T> > getLongestSequence()
  {
    if(sequence.size() == 0)
      return new ArrayList<RowColElem<T>>();
    List<RowColElem<T>> k = sequence.peek(); //Current longest sequence present
    List shallowCopy = new ArrayList(k); //Mere shallow copy of above
    return shallowCopy; 
  }
  
  // Retrieve an element at virtual row/col specified. Performs boards
  // checking and necessary internal translation to retrieve from
  // physical location. Any row/col may be requested. If it is beyond
  // the extent of the board determined by min/max row/col, the fill
  // element is returned.  If the element has not been explicitly set,
  // the fill element is returned.
  // 
  // Complexity: O(1)
  public T get(int row, int col)
  {
    if(row > maxYBound || col > maxXBound || row < minYBound || col < minXBound)
      return fill;
    T elem = board.get(row-minYBound).get(col-minXBound); //Element to be returned
    if(elem == null)
      return fill;
    return elem;
  }
  
  // Append a row to the bottom of the board increasing the maximum
  // row by one
  // 
  // Target Complexity: O(C) (amortized)
  // C: the number of columns
  
  //Runtime complexity is O(C) because the method creates a new arraylist, adding C 
  //elements to the arraylist, and adding the arraylist to the end of the board. 
  //Adding takes constant time so O(C*1) = O(C)
  public void addRowBottom()
  {
    ArrayList<T> toFill = new ArrayList<T>(); //Temporary array used to add row
    for(int k = 0; k < board.get(0).size(); k++)
    {
      toFill.add(null);
    }
    board.add(toFill);
    
  }
  
  // Append a column to the right edge of the board increasing the
  // maximum column by one
  //
  // Target Complexity: O(R) (amortized)
  // R: the number of rows 
  
  //Runtime complexity is O(R) because the method traverses through the end of each row of the board and
  //adds 1 element. O(R*1) = O(R)
  public void addColRight()
  {
    for(int i = 0; i < board.size(); i++)
    {
      board.get(i).add(null); 
    } 
  }
  
  // Set give element at row/col position to be x. Expand the board if
  // needed to create space for the position.  Update internals to
  // reflect that the set may have created a new longest sequence.
  // Also update internals to allow undoSet() to be used and disable
  // redoSet() until a set has been undone.  Once an element is set,
  // it cannot be set again; attempts to do so raise a runtime
  // exception with the message: "Element 4 -2 already set to XX"
  // where the row/col indices and string representation of the
  // element are adjusted to match the call made.  Setting an element
  // to the fill element of board has no effect on the board.  It is
  // not allowed to set elements of the board to be null. Attempting
  // to do so will generate a RuntimeException with the message
  // "Cannot set elements to null"
  //
  // Target Complexity:
  //   If expansion is requried, same complexity as expandToInclude()
  //   If expansion is not required, O(L)
  //     L: the length of the longest sequence on the board
  public void set(int row, int col, T x)
  {
    if(x == null)
      throw new RuntimeException("Cannot set elements to null");
    
    if(!get(row, col).equals(fill))
      throw new RuntimeException("Element " + row + " " + col + " already set to " + get(row, col));
    
    if(x.equals(fill))
      return;
    
    int increment = expandToInclude(row, col); //Number of elements expanded
    board.get(row-minYBound).set(col-minXBound, x);
    if(!twoD)
    undo.push(new RowColElem(row, col, x));
    
    while(redo.size() > 0)
      redo.pop();
    
    boolean pos = true;
    //Below are x, y, up diagonal and down diagonal lists used to keep track of sequence
    List<RowColElem<T>> xSeq = new ArrayList<RowColElem<T>>();
    List<RowColElem<T>> ySeq = new ArrayList<RowColElem<T>>();
    List<RowColElem<T>> dUSeq = new ArrayList<RowColElem<T>>();
    List<RowColElem<T>> dDSeq = new ArrayList<RowColElem<T>>();
    
    //below are sequence length trackers
    int seqX = 0;
    int seqY = 0;
    int seqD = 0;
    int seqDD = 0;
    
    //i is used to keep increment/decrement sequence cells for checking, pos denotes whether or not sequence continues
    int i = 0;
    while(pos)
    {
      if(get(row+i,col-i).equals(x))
      {
        seqDD++;
        dDSeq.add(new RowColElem(row+i, col-i, x));
        i--;
      }
      else
        pos = false;
    }
    pos = true;
    i = 1;
    while(pos)
    {
      if(get(row+i, col-i).equals(x))
      {
        seqDD++;
        dDSeq.add(new RowColElem(row+i, col-i, x));
        i++;
      }
      else
        pos = false;
    }
    i = 0;
    pos = true;
    while(pos)
    {
      if(get(row, col+i).equals(x))
      {
        seqX++;
        xSeq.add(new RowColElem(row, col+i, x));
        i--;
      }
      else
        pos = false;
    }
    pos = true;
    i = 1;
    while(pos)
    {
      if(get(row,col+i).equals(x))
      {
        seqX++;
        xSeq.add(new RowColElem(row, col+i, x));
        i++;
      }
      else
        pos = false;
    }
    pos = true;
    i = 0;
    while(pos)
    {
      if(get(row+i, col).equals(x))
      {
        seqY++;
        ySeq.add(new RowColElem(row+i, col, x));
        i--;
      }
      else
        pos = false;
    }
    pos = true;
    i = 1;
    while(pos)
    {
      if(get(row+i, col).equals(x))
      {
        seqY++;
        ySeq.add(new RowColElem(row+i, col, x));
        i++;
      }
      else
        pos = false;
    }
    pos = true;
    i = 0;
    while(pos)
    {
      if(get(row+i,col+i).equals(x))
      {
        seqD++;
        dUSeq.add(new RowColElem(row+i, col+i, x));
        i--;
      }
      else
        pos = false;
    }
    pos = true;
    i = 1;
    while(pos)
    {
      if(get(row+i,col+i).equals(x))
      {
        seqD++;
        dUSeq.add(new RowColElem(row+i, col+i, x));
        i++;
      }
      else
        pos = false;
    }
    
    if(Math.max(Math.max(Math.max(seqD, seqX), seqY), seqDD) > sequenceLength)
    {
      sequenceLength = Math.max(Math.max(Math.max(seqD, seqX), seqDD), seqY); 
        if(sequenceLength == seqD)
    {
      sequence.push(dUSeq);
    }
    else if(sequenceLength == seqX)
    {
      sequence.push(xSeq);
    }
    else if(sequenceLength == seqDD)
    {
      sequence.push(dDSeq);
    }
    else if(sequenceLength == seqY)
    {
      sequence.push(ySeq);
    }
    }
    
    else
    {
      sequence.push(sequence.peek());
    }
  }  
  
  
  // Return how many rows the board has in memory which should
  // correspond to the difference between maxRow and minRow. This
  // method is not part of the ExpandableBoard interface.
  //
  // No target complexity.
  public int getPhysicalRows()
  {
    return (maxYBound - minYBound)+1; 
  }
  
  // Return how many columns the board has in memory which should
  // correspond to the difference between maxCol and minCol. This
  // method is not part of the ExpandableBoard interface.
  // 
  // Target complexity: O(1)
  public int getPhysicalCols()
  {
    return (maxXBound - minXBound)+1; 
  }
  
  // Ensure that there is enough internal storage allocate so that no
  // expansion will occur if set(row,col,x) is called. Expand internal
  // space for the board if needed.  Move existing elements internally
  // if required but this method should not affect the virtual row/col
  // at which existing elements exist: if an X is at (1,2) before
  // expandToInclude(-1,10) is called, a call to get(1,2) should
  // return X after expanding.
  //
  // This method should change min/max row/col if the expansion
  // increases the size of the board.
  // 
  // The method should return the number of new cells of memory N
  // which are created by it.  
  // 
  // Target Complexity: 
  //   Expansion right/down: O(N)       (amortized)
  //   Expansion left/up:    O(N + R*C) (amortized)
  //     N: new elements created which is the return value of the function
  //     R: number of rows
  //     C: number of columns
  public int expandToInclude(int row, int col)
  {
    int initArea = (1+(maxYBound - minYBound)) * (1+(maxXBound - minXBound)); //initial area of grid
    boolean up = false; //boolean up and left determine whether or not there is left/up expansion
    boolean left = false;
    if(row < minYBound || row > maxYBound)
    {
      int k;
      if(row < minYBound)
      {
        k = minYBound - row;
        minYBound = row;
        up = true;
      }
      else
      {
        k = row - maxYBound;
        maxYBound = row;
      }
      for(int i = 0; i < k; i++)
      {
        addRowBottom(); 
      }
      if(up)
      {
        ArrayList<ArrayList<T>> copy = new ArrayList<ArrayList<T>>();
        
        for(int f = 0; f < board.size(); f++)
        {
          ArrayList<T> copyB = new ArrayList<T>();
          for(int d = 0; d < board.get(0).size(); d++)
          {
            copyB.add(board.get(f).get(d));
          }
          copy.add(copyB);
        }
        
        for(int t = 0; t < board.size(); t++)
        {
          for(int e = 0; e < board.get(0).size(); e++)
          {
            if(t < k)
              board.get(t).set(e, null);
            else
              board.get(t).set(e, copy.get(t-k).get(e));
            
          }
        }
        
      }
      
    }
    
    if(col < minXBound || col > maxXBound)
    {
      int j;
      if(col < minXBound)
      {
        j = minXBound - col;
        minXBound = col;
        left = true;
      }
      else
      {
        j = col - maxXBound;
        maxXBound = col;
      }
      for(int i = 0; i < j; i++)
      {
        addColRight(); 
      }
      
       if(left)
      {
        ArrayList<ArrayList<T>> copy = new ArrayList<ArrayList<T>>();
        
        for(int f = 0; f < board.size(); f++)
        {
          ArrayList<T> copyB = new ArrayList<T>();
          for(int d = 0; d < board.get(0).size(); d++)
          {
            copyB.add(board.get(f).get(d));
          }
          copy.add(copyB);
        }
        
        for(int t = 0; t < board.size(); t++)
        {
          for(int e = 0; e < board.get(0).size(); e++)
          {
            if(e < j)
              board.get(t).set(e, null);
            else
              board.get(t).set(e, copy.get(t).get(e-j));
            
          }
        }
        
      }
    } 
    int finalArea = (1+(maxYBound - minYBound)) * (1+(maxXBound - minXBound));
    return (finalArea-initArea);
  }
  
  // Undo an explicit set(row,col,x) operation by changing an element
  // to its previous state.  Repeated calls to undoSet() can be made
  // to restore the board to an earlier state.  Each call to undoSet()
  // enables a call to redoSet() to be made to move forward in the
  // history of the board state. Calls to undoSet() do not change the
  // extent of boards: they do not shrink to a smaller size once grown
  // even after an undo call.  If there are no sets to undo, this
  // method throws a runtime exception with the message
  // "Undo history is empty"
  //
  // Target Complexity: O(1) (worst case)
  
  //Runtime complexity is O(1) because there are only 3 operations taking place here:
  //Pushing, popping off the undo/redo stack and setting values in an arraylist. Because each are all
  //constant-time operations, the undoSet() runtime is O(1) or constant.
  public void undoSet()
  {
    if(undo.size() == 0)
      throw new RuntimeException("Undo history is empty");
    RowColElem<T> ne = undo.pop();
    redSeq.push(sequence.pop());
    redo.push(ne); 
    board.get(ne.getRow()-minYBound).set(ne.getCol()-minXBound, null);
  }
  
  // Redo a set that was undone via undoSet().  Every call to
  // undoSet() moves backward in the history of the board state and
  // enables a corresponding call to redoSet() which will move forward
  // in the history.  At any point, a call to set(row,col,x) will
  // erase 'future' history that can be redone via redoSet().  If
  // there are no moves that can be redone because of a call to set()
  // or undoSet() has not been called, this method generates a
  // RuntimeException with the message "Redo history is empty".
  //
  // Target Complexity: O(1)
  
  //Exactly like before, because popping off redo stack, and popping onto undo/redo stack
  //all take constant time (as well as Arraylist.set(...)/Arraylist.get(...)), the entire method
  //takes constant time.
  public void redoSet()
  {
    if(redo.size() == 0)
      throw new RuntimeException("Redo history is empty");
    RowColElem<T> xt = redo.pop();
    undo.push(xt);
    sequence.push(redSeq.pop());
    board.get(xt.getRow()-minYBound).set(xt.getCol()-minXBound, xt.getElem());
  }
  
  // toString() - create a pretty representation of board.
  //
  // Examples:
  //   |  1|  2|  3|
  //   +---+---+---+
  // 1 |   |   |   |
  //   +---+---+---+
  // 2 |   |   |   |
  //   +---+---+---+
  // 3 |   |   |   |
  //   +---+---+---+
  //
  //    | -4| -3| -2| -1|  0|  1|  2|  3|
  //    +---+---+---+---+---+---+---+---+
  // -2 |  A|   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  // -1 |   |   |  B|   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //  0 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+ 
  //  1 |   |   |   |   |   |  A|   |   |
  //    +---+---+---+---+---+---+---+---+
  //  2 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //  3 |   |   |   |   |   |   |   |   |
  //    +---+---+---+---+---+---+---+---+
  //
  // Target Complexity: O(R*C)
  //   R: number of rows
  //   C: number of columns
  // 
  // Note: to adhere to this runtime complexity, normal string
  // concatenation cannot be used; instead a StringBuilder should be
  // employed.
  public String toString()
  {
    StringBuilder lines = new StringBuilder(); //+---+---+.....
    StringBuilder top = new StringBuilder(); //Top row
    top.append("    ");
    lines.append("  ");
    for(int k = 0; k < board.get(0).size(); k++)
    {
      top.append("|" + String.format("%3s",(k + minXBound)));
      lines.append("+---");
    }
    top.append("|");
    lines.append("+");
    StringBuilder toStr = new StringBuilder();
    toStr.append(top);
    toStr.append("\n");
    toStr.append("  ");
    toStr.append(lines);
    toStr.append("\n");
    for(int i = 0; i < board.size(); i++){
      toStr.append(String.format("%3s", (i + minYBound)) + " |");
      for(int j = 0; j < board.get(i).size(); j++){
        toStr.append(String.format("%3s", get((i+minYBound), (j+minXBound))) + "|");
      }
      toStr.append("\n");
      toStr.append("  ");
      toStr.append(lines);
      toStr.append("\n");
    }
    return toStr.toString();
    
    
  }
  
}
