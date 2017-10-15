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
//
//=========================================================================

package mouse.explorer;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Line
//
//-------------------------------------------------------------------------
//
//  Abstract base class for different kinds of lines.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

abstract class Line
{
  //=====================================================================
  //
  //  Starting and ending offset of the Line
  //  in the display area of containing GUI.
  //
  //=====================================================================
  int start = -1;
  int end = -1;


  //=====================================================================
  //
  //  Two methods required by the containing Paragraph.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Append text represented by this Line to the image of
  //  display area provided as StringBuffer 'display'.
  //  Set 'start' and 'end' to the starting and ending offset
  //  of the text within the image.
  //-------------------------------------------------------------------
  abstract void writeTo(StringBuffer display);

  //-------------------------------------------------------------------
  //  If 'offset' falls within an element of this Line in the
  //  display area, return an Element object identifying that element.
  //  (Depending on the Line subclass, the element can be an Item
  //  or this Line.)
  //  Otherwise return null.
  //-------------------------------------------------------------------
  abstract Element find(int offset);
}

