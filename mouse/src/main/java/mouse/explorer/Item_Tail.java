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
//      Replaced relation 'follow' by 'Expr.firstTailTerms'.
//      Removed abstract method 'NUL'.
//      Changed for the redesigned Tail class.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Item_Tail
//
//-------------------------------------------------------------------------
//
//  Item shown on Explorer line, representing 'Tail(e)'.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Item_Tail extends Item
{
  //=====================================================================
  //
  //  Constructors.
  //
  //=====================================================================
  Item_Tail(Expr e)
    { this.e = e; }


  //=====================================================================
  //
  //  Implementation of Item's abstract methods.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Return a copy of this Item.
  //-------------------------------------------------------------------
  Item_Tail copy()
    { return new Item_Tail(e); }

  //-------------------------------------------------------------------
  //  Return String representation of this Item.
  //-------------------------------------------------------------------
  String asString()
    { return("Tail("+e.name+")"); }

  //-------------------------------------------------------------------
  //  Return first terminals of 'Tail(e)'.
  //-------------------------------------------------------------------
  BitSet terms()
    { return e.firstTailTerms; }

  //-------------------------------------------------------------------
  //  Expand this Item.
  //  The result is one or more sequences of Items representing
  //  alternative expansions. They are returned as rows
  //  in a two-dimensional array of Items.
  //-------------------------------------------------------------------
  Item[][] expand()
   {
      Tail theTail = e.tail.expandUnique();

      Item[][] result = new Item[theTail.size()][];

      int i = 0;
      for (Tail.Strand strand: theTail)
      {
        Vector<Item> row = new Vector<Item>();
        for (Expr p: strand.seq)
          row.add(new Item_Expr(p));
        if (!strand.E.end)
          row.add(new Item_Tail(strand.E));
        result[i] = row.toArray(new Item[0]);
        i++;
      }

      return result;
   }
}