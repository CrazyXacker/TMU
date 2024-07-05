package xyz.tmuapp.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MultipartUtil {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36";
    private static final String LINE = "\r\n";

    private final HttpURLConnection httpConn;
    private final String boundary;
    protected String charset;
    protected OutputStream outputStream;
    protected PrintWriter writer;
    public boolean cancel = false;
    protected boolean lastItemIsFile = false;

    public MultipartUtil(String requestURL, String charset) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", USER_AGENT);
        setupWriter();
    }

    public PrintWriter setupWriter() throws IOException {
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        return writer;
    }

    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE);
        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE);
        writer.append(LINE);
        writer.append(value).append(LINE);
        writer.flush();
        lastItemIsFile = false;
    }

    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
        writer.append("Content-Transfer-Encoding: binary").append(LINE);
        writer.append(LINE);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while (!cancel && (bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        if (cancel) {
            writer.close();
            return;
        }

        writer.append(LINE);
        writer.flush();
        lastItemIsFile = true;
    }

    public void addFileStreamPart(String fieldName, String fileName, InputStream inputStream) throws IOException {
        writer.append("--" + boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE);
        writer.append("Content-Transfer-Encoding: binary").append(LINE);
        writer.append(LINE);
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while (!cancel && (bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        inputStream.close();
        if (cancel) {
            writer.close();
            return;
        }

        writer.append(LINE);
        writer.flush();
        lastItemIsFile = true;
    }

    public void addHeaderField(String name, String value) {
        httpConn.setRequestProperty(name, value);
    }

    public String finish() throws IOException {
        String response;
        writer.flush();
        writer.append("--" + boundary + "--").append(LINE);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = httpConn.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            response = result.toString(this.charset);
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return response;
    }
}