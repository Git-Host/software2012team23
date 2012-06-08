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

import java.util.Date;

public class VolatileReport {

	private long mTimestamp = 0;

	public VolatileReport(final VolatileReport src) {
		mTimestamp = src.mTimestamp;
	}

	public VolatileReport() {
		update();
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	protected void update() {
		mTimestamp = millisecondNow();
	}

	private long millisecondNow() {
		return (new Date().getTime());
	}

}
