// Simple class to encapsulate a triple row,col,element in boards.
// The class is immutable
public class RowColElem<T>{
private int row, col;
private T el;
  // Create a RowColElem with the parameter parts
  public RowColElem(int r, int c, T e)
  {
   row = r;
   col = c;
   el = e;
  }

  // Return the row
  public int getRow()
  {
   return row; 
  }

  // Return the column
  public int getCol()
  {
   return col; 
  }

  // Return the element
  public T getElem()
  {
   return el; 
  }

  // Return a pretty string version of the triple formated as
  // (row,col,elem)
  public String toString()
  {
   return "(" + row + "," + col + "," + el.toString() + ")";
  }
  
  public boolean equals(Object obj)
  {
   if(getClass() != obj.getClass())
   {
    return false; 
   }
   RowColElem ob = (RowColElem)obj;
   if(col != ob.col)
     return false;
   if(row != ob.row)
     return false;
   if(!el.equals(ob.el))
     return false;
    return true;
  }

}
