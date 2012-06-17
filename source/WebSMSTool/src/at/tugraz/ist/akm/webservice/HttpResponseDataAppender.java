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

package at.tugraz.ist.akm.webservice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import my.org.apache.http.HttpResponse;
import my.org.apache.http.entity.ContentProducer;
import my.org.apache.http.entity.EntityTemplate;

import org.json.JSONObject;

public class HttpResponseDataAppender {

	public void appendHttpResponseData(HttpResponse httpResponse,
			final JSONObject data) {
		appendHttpResponseData(httpResponse,
				WebServerConfig.HTTP.CONTENT_TYPE_JSON, data.toString());
	}

	public void appendHttpResponseData(HttpResponse httpResponse,
			final String contentType, final String data) {

		httpResponse.setEntity(new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream);
				writer.write(data);
				writer.flush();
				writer.close();
			}
		}));
		httpResponse.setHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE,
				contentType);
	}

	public void appendHttpResponseMediaType(HttpResponse httpResponse,
			final String mediaType, final byte[] data) {

		httpResponse.setEntity(new EntityTemplate(new ContentProducer() {
			@Override
			public void writeTo(OutputStream outstream) throws IOException {
				outstream.write(data);
			}
		}));

		httpResponse
				.addHeader(WebServerConfig.HTTP.KEY_CONTENT_TYPE, mediaType);
	}

}
