package com.ericsson.captiveportal;

import java.io.IOException;
import fi.iki.elonen.NanoHTTPD;
public class HelloServer extends NanoHTTPD {

    public void run() {
        try {
            this.start(5000,false);
        } catch (IOException e) {

        }
    }

    public HelloServer() {
        super("192.168.43.1",8080);
    }

    @Override
    public NanoHTTPD.Response serve(IHTTPSession session) {

        String msg = "<html><body><h1>Flying Access Point</h1>\n";

        msg += "<h2>Captive Portal</h2>\n";

        msg += "<h3>No Internet</h3>\n";

        msg += "</body></html>\n";

        return newFixedLengthResponse(msg);
    }
}