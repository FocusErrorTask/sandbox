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

package at.tugraz.ist.akm.webservice.requestprocessor;

import java.io.IOException;

import my.org.apache.http.HttpException;
import my.org.apache.http.HttpResponse;
import my.org.apache.http.RequestLine;
import my.org.apache.http.protocol.HttpContext;
import my.org.apache.http.protocol.HttpRequestHandlerRegistry;
import android.content.Context;
import at.tugraz.ist.akm.io.FileReader;
import at.tugraz.ist.akm.io.xml.XmlNode;
import at.tugraz.ist.akm.trace.LogClient;

public class FileRequestProcessor extends AbstractHttpRequestProcessor
{

    private LogClient mLog = new LogClient(this);


    public FileRequestProcessor(final Context context, final XmlNode config,
            final HttpRequestHandlerRegistry registry)
    {
        super(context, config, registry);

    }


    @Override
    synchronized public void handleRequest(RequestLine requestLine, String requestData,
            HttpResponse httpResponse, HttpContext httpContext)
            throws HttpException, IOException
    {
        mLog.debug("handle request <" + requestLine.getUri() + ">");

        String uri = requestLine.getUri();
        final FileInfo fileInfo = mUri2FileInfo.get(uri);
        if (fileInfo == null)
        {
            mLog.info("no mapping found for uri <" + uri + ">");
            return;
        }

        FileReader reader = new FileReader(mContext, fileInfo.mFile);
        final String data = reader.read();
        reader.close();
        reader = null;

        mResponseDataAppender.appendHttpResponseData(httpResponse,
                fileInfo.mContentType, data);
    }


    @Override
    public void close() throws IOException
    {
        super.close();
        mLog = null;
    }
}
