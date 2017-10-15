//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2017 by Roman R. Redziejowski (www.romanredz.se).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//-------------------------------------------------------------------------
//
//  Change log
//    170301 Created.
//    170901 Version 1.9.1
//      Removed use of 'Item.NUL'.
//      Replaced 'copy' by copy constructor.
//      Moved 'prefix' and 'strip' from Paragraph_Explorer.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Line_Items
//
//-------------------------------------------------------------------------
//
//  Line containing a Sequence expression being explored.
//  It is represented by a sequence of Items.
//  The Explorer operation "strip" removes the first Item from the line.
//  The operation may be repeated several times. The removed Items are
//  displayed as a prefix followed by " -- ". The prefix is not regarded
//  as part of the ine and is not selectable.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Line_Items extends Line
{
  //=====================================================================
  //
  //  Contents of the Line.
  //
  //=====================================================================
  Vector<Item> items = new Vector<Item>(); // The Items
  String prefix = "";                      // The prefix


  //===================================================================
  //
  //  Constructors.
  //
  //===================================================================
  //-------------------------------------------------------------------
  //  Default constructor.
  //-------------------------------------------------------------------
  Line_Items() {}

  //-------------------------------------------------------------------
  //  Construct Line containing expression 'e' with 'Tail(E)'.
  //  One of these items may be missing, indicated by null.
  //-------------------------------------------------------------------
  Line_Items(Expr e, Expr E)
    {
      if (e!=null) addItem(new Item_Expr(e));
      if (E!=null) addItem(new Item_Tail(E));
      expandSeq(); // Flatten any nested sequences
    }

  //-------------------------------------------------------------------
  //  Deep copy.
  //-------------------------------------------------------------------
  Line_Items(Line_Items from)
    {
      for (Item item: from.items)
        items.add(item.copy());
      prefix = from.prefix;
    }


  //=====================================================================
  //
  //  Operations on collection of items.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Get number of Items.
  //-------------------------------------------------------------------
  int nItems()
    { return items.size(); }

  //-------------------------------------------------------------------
  //  Get item at position 'i'.
  //-------------------------------------------------------------------
  Item item(int i)
    { return items.elementAt(i); }

  //-------------------------------------------------------------------
  //  Add 'item' at the end.
  //-------------------------------------------------------------------
  void addItem(final Item item)
    { items.add(item); }

  //-------------------------------------------------------------------
  //  Insert 'item' at position 'i'.
  //-------------------------------------------------------------------
  void insertItem(int i, final Item item)
    { items.add(i,item); }

  //-------------------------------------------------------------------
  //  Remove item at position 'i'.
  //-------------------------------------------------------------------
  void removeItem(int i)
    { items.remove(i); }

  //-------------------------------------------------------------------
  //  Replace item at position 'i' by 'item'.
  //-------------------------------------------------------------------
  void replaceItemBy(int i, final Item item)
    {
      items.remove(i);
      items.add(i,item);
    }
  //-------------------------------------------------------------------
  //  Replace item at position 'i' by Items for 'exprList'.
  //-------------------------------------------------------------------
  void replaceItemBy(int i, final Expr[] exprList)
    {
      items.remove(i);
      int j = i;
      for (Expr e: exprList)
      {
        items.add(j,new Item_Expr(e));
        j++;
      }
    }


  //=====================================================================
  //
  //  Return set of (indexes of) first terminals for expression
  //  represented by this Line.
  //
  //=====================================================================
  BitSet terms()
    {
      BitSet result = new BitSet();
      for (Item item: items)
      {
        result.or(item.terms());
        if (!item.e.nul || item.e.end) break;
      }
      return result;
    }


  //=====================================================================
  //
  //  Return String representation of this Line.
  //
  //=====================================================================
  String asString()
    {
      StringBuffer sb = new StringBuffer();
      for (Item it: items)
         sb.append(it.asString()+" ");
      return sb.toString();
    }


  //=====================================================================
  //
  //  Eliminate unnecessary nesting of Sequences: replace items
  //  representing Expr.Sequence by sequence of Items.
  //
  //=====================================================================
  private void expandSeq()
    {
      for (int i=nItems()-1;i>=0;i--)
      {
        if (item(i) instanceof Item_Expr
            && item(i).e instanceof Expr.Sequence)
        {
          Expr.Sequence seq = (Expr.Sequence)item(i).e;
          replaceItemBy(i,seq.args);
        }
      }
    }


  //=====================================================================
  //
  //  Explorer actions.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Expand the i-th item, returning an array of one or more lines.
  //  Return null if the item cannot be expanded.
  //-------------------------------------------------------------------
  Line_Items[] expand(int i)
    {
      Vector<Line_Items> newLines = new Vector<Line_Items>();

      Item[][] expItem = item(i).expand();
      if (expItem==null) return null;

      // For each row in expansion of the Item
      for (int k=0;k<expItem.length;k++)
      {
        // Create new copy of this Line
        Line_Items newLine = new Line_Items(this);

        // In the copy, replace the item
        // with corresponding row of the expansion
        Item[] row = expItem[k];
        newLine.removeItem(i);
        int j = i;
        for (Item item: row)
        {
          newLine.insertItem(j,item);
          j++;
        }
        newLine.expandSeq();   // Flatten any nested sequences
        newLines.add(newLine); // Add to result
      }
      return newLines.toArray(new Line_Items[0]);
    }

  //-------------------------------------------------------------------
  //  Strip.
  //-------------------------------------------------------------------
  void strip()
    {
      if (nItems()==0) return;
      prefix = prefix + item(0).asString() + " ";
      removeItem(0);
    }


  //=====================================================================
  //
  //  Implementation of Line's abstract methods.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Append text represented by this Line to the image of
  //  display area provided as StringBuffer 'display'.
  //  Set 'start' and 'end' to the starting and ending offset
  //  of the text within the image.
  //  Each Item must provide a method 'writeTo' that appends
  //  the formatted Item text to the image of display area.
  //-------------------------------------------------------------------
  void writeTo(StringBuffer display)
    {
      if (prefix.length()>0)
        display.append(prefix + "-- "); // Nonempty prefix ends with " "
      start = display.length();
      for (Item item: items)
        item.writeTo(display);
      end = display.length();
      display.append("\n");
    }

  //-------------------------------------------------------------------
  //  If 'offset' falls within an Item belonging to this Line in the
  //  display area, return an Element object identifying the Item.
  //  Otherwise return null.
  //  Each Item must provide a method 'find' that checks if a given
  //  offset within the display is within that Item.
  //-------------------------------------------------------------------
  Element find(int offset)
    {
      if (offset<start || offset>=end) return null;
      for (int i=0;i<nItems();i++)
      {
        Element elem = item(i).find(offset);
        if (elem!=null)
        {
          elem.item = i;
          return elem;
        }
      }
      return null;
    }

}

