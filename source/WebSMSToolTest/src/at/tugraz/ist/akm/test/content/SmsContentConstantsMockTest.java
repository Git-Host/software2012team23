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
package at.tugraz.ist.akm.test.content;

import junit.framework.TestCase;
import at.tugraz.ist.akm.content.SmsContentConstants;

public class SmsContentConstantsMockTest extends TestCase
{

    public void testMock_smsConstants_construct()
    {
        @SuppressWarnings("unused")
        SmsContentConstants smc = new SmsContentConstants();
        @SuppressWarnings("unused")
        SmsContentConstants.Column smcc = new SmsContentConstants.Column();
        @SuppressWarnings("unused")
        SmsContentConstants.Uri smcu = new SmsContentConstants.Uri();
    }

}
