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
//    170831 Created for Version 1.9.1.
//
//=========================================================================

package mouse.explorer;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Line_Simple
//
//-------------------------------------------------------------------------
//
//  Base class for unstructured lines:
//  Line_Conflict, Line_Expr, Line_Tail, Line_Terms.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

abstract class Line_Simple extends Line
{
  //=====================================================================
  //
  //  Text to be shown in the GUI display area.
  //
  //=====================================================================
  String lineText;


  //=====================================================================
  //
  //  Implementation of Line's abstract methods.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Append 'lineText' to the image of display area provided
  //  as StringBuffer 'display'.
  //  Set 'start' and 'end' to the starting and ending offset
  //  of the text within the image.
  //-------------------------------------------------------------------
  void writeTo(StringBuffer display)
    {
      start = display.length();
      display.append(lineText);
      end = display.length();
      display.append("\n");
    }

  //-------------------------------------------------------------------
  //  If 'offset' falls within the line text in the display area,
  //  return an Element object identifying the Line.
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

