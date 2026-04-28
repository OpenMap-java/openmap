package com.bbn.openmap.image.wms;

import java.util.Arrays;
import java.util.Collection;

import com.bbn.openmap.util.http.HttpConnection;

public class DefaultFeatureInfoResponse implements FeatureInfoResponse {

	private StringBuffer out;
	private String contentType;

	public void setOutput(String contentType, StringBuffer out) {
		this.out = out;
		this.contentType = contentType;

		appendHeader();
	}

	public void flush() {
		appendFooter();
	}

	public Collection<String> getInfoFormats() {
		return Arrays.asList(HttpConnection.CONTENT_HTML,
				HttpConnection.CONTENT_PLAIN);
	}

	public void output(LayerFeatureInfoResponse layerFeatureInfoResponse) {
		layerFeatureInfoResponse.output(contentType, out);
	}
	
	protected void write(String s){
		out.append(s);
	}
	
	protected String getContentType(){
		return contentType;
	}

	protected void appendHeader() {
		if (getContentType().equals(HttpConnection.CONTENT_HTML)) {
			write("<html><head>\n");
			write("<meta http-equiv=\"content-type\"\n");
			write("      content=\"text/html; charset=UTF-8\">\n");
			write("</head><body>\n");
		} else if (getContentType().equals(HttpConnection.CONTENT_JSON)) {
			write("{\n");
			write("  \"type\": \"FeatureCollection\",\n");
			write("  \"features\": [\n");
		}
	}

	protected void appendFooter() {
		if (getContentType().equals(HttpConnection.CONTENT_HTML)) {
			write("</body></html>");
		} else if (getContentType().equals(HttpConnection.CONTENT_JSON)) {
			write("  ]\n");
			write("}\n");
		}
	}

}
