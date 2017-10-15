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
//    160624 Created for mouse.explorer.
//    170901 Moved to mouse.utility.
//
//=========================================================================

package mouse.utility;

import java.util.BitSet;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Bit set iterator.
//
//-------------------------------------------------------------------------
//
//  Example of use for iterating over integers in a BitSet 'bs':
//
//    for (BitIter iter=new BitIter(bs);iter.hasNext();)
//    {
//      int next = iter.next();
//      ...
//    }
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class BitIter
{
  private final BitSet bitset;
  private int next;

  //-------------------------------------------------------------------
  //  Construct the iterator for BitSet 'bs'
  //-------------------------------------------------------------------
  public BitIter(final BitSet bs)
    {
      bitset = bs;
      next = bitset.nextSetBit(0);
    }

  //-------------------------------------------------------------------
  //  Is there next element?
  //-------------------------------------------------------------------
  public boolean hasNext()
    { return next>=0; }

  //-------------------------------------------------------------------
  //  Get next element
  //-------------------------------------------------------------------
  public int next()
    {
      if (next<0) throw new Error("No next bit");
      int result = next;
      next = bitset.nextSetBit(next+1);
      return result;
    }
}

