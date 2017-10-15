//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2016 by Roman R. Redziejowski (www.romanredz.se).
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
//      Made into a subclass of Line_Simple.
//
//=========================================================================

package mouse.explorer;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Line_Conflict
//
//-------------------------------------------------------------------------
//
//  Line showing a Conflict.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Line_Conflict extends Line_Simple
{
  //=====================================================================
  //
  //  Contents of the Line: a Conflict.
  //
  //=====================================================================
  Conflict conflict;


  //===================================================================
  //
  //  Constructor.
  //
  //===================================================================
  Line_Conflict(final Conflict c)
    {
      conflict = c;
      lineText = conflict.expr.name + ":  " + conflict.asString();
    }
}

