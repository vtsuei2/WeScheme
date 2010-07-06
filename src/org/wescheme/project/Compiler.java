package org.wescheme.project;

import javax.servlet.http.HttpServlet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;

public class Compiler extends HttpServlet
{
	private static final long serialVersionUID = 6867416066840862239L;
	
	private static final String compilationServletURL = "http://go.cs.brown.edu:8000/servlets/standalone.ss";
	
	public interface CompilationResult {	
		boolean isBad();
		String getCompiledCode();
		String getErrorMessage();
	}
	
	public static class GoodCompilationResult implements CompilationResult {
		private String compiledCode;
		public GoodCompilationResult(String compiledCode) {
			this.compiledCode = compiledCode;
		}
		public boolean isBad() { return false; }
		public String getCompiledCode() { return this.compiledCode; }
		public String getErrorMessage() { return null; }
	}
	
	public static class BadCompilationResult implements CompilationResult {
		private String errorMessage;
		public BadCompilationResult(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		public boolean isBad() { return true; }
		public String getCompiledCode() { throw new UnsupportedOperationException(); }
		public String getErrorMessage() { return this.errorMessage; }
	}
	
	
	
	public static ObjectCode compile(SourceCode src) {
		CompilationResult result = Compiler.compile(src.getName(), src.toString());
		if (result.isBad()) {
			throw new RuntimeException(result.getErrorMessage());
		} else {
			return new ObjectCode(result.getCompiledCode(), new HashSet<String>(), false);
		}
	}
	
	/*
	 * compile: SourceCode -> ObjectCode
	 * Compiles the source code, using an external compilation server, and returns the compiled code.
	 */
	public static CompilationResult compile(String programName, String programSource){
		System.out.println("compile");
		try {
			URL url = new URL(compilationServletURL);
					
			String data = "name=" + URLEncoder.encode(programName, "UTF-8") +
					"&program=" + URLEncoder.encode(programSource, "UTF-8");
					
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
				
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				System.out.println("good thing happened");
				// FIXME: no error trapping when the compile goes bad!
				String compiledCode = readStream(conn.getInputStream());
				return new GoodCompilationResult(compiledCode);		
			} else {
				System.out.println("bad thing happened: "+ conn.getResponseCode());
				String errorMessage = readStream(conn.getErrorStream());//readStream(conn.getInputStream());
				String otherMessage = readStream(conn.getInputStream());
				System.out.println("errorMessage: "  + errorMessage);
				System.out.println("otherMessage: " + otherMessage);
				System.out.println("response message: " + conn.getResponseMessage());
				return new BadCompilationResult(errorMessage);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	private static String readStream(InputStream stream) {
		BufferedInputStream bs = new BufferedInputStream(stream);
		int nextChar;
		StringBuilder builder = new StringBuilder();
		try {
			while ((nextChar = bs.read()) != -1) {
				builder.append((char) nextChar);
			}
			return builder.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
