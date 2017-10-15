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
//      Removed abstract method 'NUL'.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class Item
//
//-------------------------------------------------------------------------
//
//  Base class for items shown on an Explorer line.
//  Such item may represent either an expression (Item_Expr)
//  or Tail (Item_Tail).
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

abstract class Item
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  The expression (common for both subclasses).
  //-------------------------------------------------------------------
  Expr e;

  //-------------------------------------------------------------------
  //  Start and end offset in the GUI area (when shown).
  //-------------------------------------------------------------------
  int start = -1;
  int end = -1;


  //=====================================================================
  //
  //  Abstract methods
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Return a copy of this Item.
  //-------------------------------------------------------------------
  abstract Item copy();

  //-------------------------------------------------------------------
  //  Return String representation of this Item.
  //-------------------------------------------------------------------
  abstract String asString();

  //-------------------------------------------------------------------
  //  Return first terminals of the represented Expression or Tail.
  //-------------------------------------------------------------------
  abstract BitSet terms();

  //-------------------------------------------------------------------
  //  Expand this Item.
  //  The result is one or more sequences of Items representing
  //  alternative expansions. They are returned as rows
  //  in a two-dimensional array of Items.
  //-------------------------------------------------------------------
  abstract Item[][] expand();


  //=====================================================================
  //
  //  Two methods required by the containing Line.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Append text represented by this Item to the image of
  //  display area provided as StringBuffer 'display'.
  //  Set 'start' and 'end' to the starting and ending offset
  //  of the text within the image.
  //-------------------------------------------------------------------
  void writeTo(StringBuffer display)
    {
      start = display.length();
      display.append(asString());
      end = display.length();
      display.append(" ");
    }

  //-------------------------------------------------------------------
  //  If 'offset' falls within an element of this Item in the
  //  display area, return an Element object identifying that Item.
  //  Otherwise return null.
  //-------------------------------------------------------------------
  Element find(int offset)
    {
      if (offset<start || offset>=end) return null;
      Element elem = new Element();
      elem.start = start;
      elem.end = end;
      return elem;
    }
}