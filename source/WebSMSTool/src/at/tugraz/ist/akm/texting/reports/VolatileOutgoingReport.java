/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.tugraz.ist.akm.texting.reports;

public class VolatileOutgoingReport extends VolatileReport
{
    private int mNumPending = 0;
    private int mNumErroneous = 0;


    public VolatileOutgoingReport(final VolatileOutgoingReport src)
    {
        super(src);
        mNumPending = src.mNumPending;
        mNumErroneous = src.mNumErroneous;
    }


    public VolatileOutgoingReport()
    {
    }


    public int getNumPending()
    {
        return mNumPending;
    }


    public void setNumPending(int numPending)
    {
        this.mNumPending = numPending;
        super.update();
    }


    public int getNumErroneous()
    {
        return mNumErroneous;
    }


    public void setNumErroneous(int numErroneous)
    {
        this.mNumErroneous = numErroneous;
        super.update();
    }
}
