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

import java.util.BitSet;
import java.util.Vector;
import mouse.utility.BitMatrix;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Paragraph_Terms
//
//-------------------------------------------------------------------------
//
//  Paragraph in GUI_Explorer containing terminal pairs.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Paragraph_Terms extends Paragraph_Lines<Line_Terms>
{
  //=====================================================================
  //
  //  Construct from Conflict.
  //
  //=====================================================================
  Paragraph_Terms(final Conflict c)
    {
      for (TermPair tPair: c.termPairs)
        addLine(new Line_Terms(tPair));
    }

  //=====================================================================
  //
  //  Construct with one pair.
  //
  //=====================================================================
  Paragraph_Terms(final TermPair termPair)
    { addLine(new Line_Terms(termPair)); }

}

