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
//      'start' and 'end' moved to the base class.
//
//=========================================================================

package mouse.explorer;

import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Paragraph_Lines
//
//-------------------------------------------------------------------------
//
//  Paragraph consisting of lines of class L.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Paragraph_Lines<L extends Line> extends Paragraph
{
  //=====================================================================
  //
  //  Contents of the Paragraph: Lines of class L.
  //
  //=====================================================================
  Vector<L> lines = new Vector<L>();


  //=====================================================================
  //
  //  Operations on collection of lines.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Get number of lines.
  //-------------------------------------------------------------------
  int nLines()
    { return lines.size(); }

  //-------------------------------------------------------------------
  //  Get line at position 'i'.
  //-------------------------------------------------------------------
  L line(int i)
    { return lines.elementAt(i); }

  //-------------------------------------------------------------------
  //  Add 'line' at the end.
  //-------------------------------------------------------------------
  void addLine(final L line)
    { lines.add(line); }

  //-------------------------------------------------------------------
  //  Insert 'line' at position 'i'.
  //-------------------------------------------------------------------
  void insertLine(int i, final L line)
    { lines.add(i,line); }

  //-------------------------------------------------------------------
  //  Remove line at position 'i'.
  //-------------------------------------------------------------------
  void removeLine(int i)
    { lines.remove(i); }

  //-------------------------------------------------------------------
  //  Remove all lines.
  //-------------------------------------------------------------------
  void clear()
    { lines = new Vector<L>(); }

  //-------------------------------------------------------------------
  //  Replace line at position 'i' by 'line'.
  //-------------------------------------------------------------------
  void replaceLineBy(int i, final L line)
    {
      lines.remove(i);
      lines.add(i,line);
    }

  //-------------------------------------------------------------------
  //  Replace line at position 'i' by 'lineList'.
  //-------------------------------------------------------------------
  void replaceLineBy(int i, final L[] lineList)
    {
      lines.remove(i);
      int j = i;
      for (L line: lineList)
      {
        lines.add(j,line);
        j++;
      }
    }


  //=====================================================================
  //
  //  Implementation of Paragraph's abstract methods.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Append text represented by this Paragraph to the image of
  //  display area provided as StringBuffer 'display'.
  //  Set 'start' and 'end' to the starting and ending offset
  //  of the text within the image.
  //  Each line of class L must provide a method 'writeTo' that appends
  //  the formatted line text to the image of GUI display area.
  //-------------------------------------------------------------------
  void writeTo(StringBuffer display)
    {
      start = display.length();
      for (L line: lines)
        line.writeTo(display);
      end = display.length();
    }

  //-------------------------------------------------------------------
  //  If 'offset' falls within an element of this Paragraph in the
  //  display area, return an Element object identifying that element.
  //  (Depending on L, the element can be an Item or a Line.)
  //  Otherwise return null.
  //  Each line of class L must provide a method 'find' that checks
  //  if a given offset within the display area belongs to that line
  //  or to its contained element.
  //-------------------------------------------------------------------
  Element find(int offset)
    {
      if (offset<start || offset>=end) return null;
      for (int i=0;i<nLines();i++)
      {
        Element elem = line(i).find(offset);
        if (elem!=null)
        {
          elem.line = i;
          return elem;
        }
      }
      return null;
    }
}

