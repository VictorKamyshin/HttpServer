package ru.mail.park;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;


/**
 * Created by Пользователь on 26.03.2017.
 */
public class HttpResponse {

    public static final Integer OK = 200;

    public static final Integer FORBRIDDEN = 403;

    public static final Integer NOTFOUND = 404;

    public static final Integer BADREQUEST = 400;

    public static final Integer NOTALLOWED = 405;


    private Integer status;
    private String date;
    private Integer contentLength;
    private String contentType;
    private String connection;
    private String server;

    private byte[] body;

    private String requestMethod;

    public HttpResponse(){
        server = "VictorServer";
        connection = "close";
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDate() {
        date = new Date().toString();
    }

    public void setRequestMethod(String requestMethod) {
        if(requestMethod.equals("GET")||(requestMethod.equals("HEAD"))) {
            this.requestMethod = requestMethod;
            status=OK;
        } else {
            System.out.println(requestMethod);
            this.requestMethod = requestMethod;
            status=NOTALLOWED;
        }
    }

    private void setContentType(String fileName) {
        if(fileName == null){
            contentType="text/html";
            return;
        }

        if(fileName.endsWith(".html")){
            contentType="text/html";
        }

        if(fileName.endsWith(".css")){
            contentType="text/css";
        }

        if(fileName.endsWith(".js")){
            contentType="application/javascript";
        }

        if(fileName.endsWith(".jpeg")){
            contentType="image/jpeg";
        }

        if(fileName.endsWith(".jpg")){
            contentType="image/jpeg";
        }

        if(fileName.endsWith(".png")){
            contentType="image/png";
        }

        if(fileName.endsWith(".gif")){
            contentType="image/gif";
        }

        if(fileName.endsWith(".swf")){
            contentType="application/x-shockwave-flash";
        }

        if(fileName.endsWith(".ico")){
            contentType="image/ico";
        }

    }

    public void setBodyByPath(String pathString, String fileName) {

        setContentType(fileName);

        final File targetFile = new File(pathString+'/'+fileName);

        if(targetFile.isDirectory()){
            try{
                final Path path = Paths.get(pathString+fileName+"index.html");
                this.body = Files.readAllBytes(path);
                contentLength=body.length;
                status=OK;
            } catch(IOException e) {
                e.printStackTrace();
                status=FORBRIDDEN;
                body = null;
                contentLength=0;
            }
        } else {
            if(fileName.contains("..")){
                status=FORBRIDDEN;
                body = null;
                contentLength=0;
                return;
            }

            final Path path = Paths.get(pathString+'/'+fileName);
            try {
                this.body = Files.readAllBytes(path);
                contentLength = body.length;
            } catch (IOException e) {
                e.printStackTrace();
                body = null;
                contentLength = 0;
                status = NOTFOUND;
            }

        }
    }

    public byte[] getAsBytes() throws RuntimeException {
        final StringBuilder builder = new StringBuilder("HTTP/1.1 ");

        if(status.equals(NOTFOUND)){
            builder.append("404 NOT FOUND");
            builder.append("\r\n");
        } else if(status.equals(OK)){
            builder.append("200 OK");
            builder.append("\r\n");
        } else if(status.equals(FORBRIDDEN)){
            builder.append("403 FORBRIDDEN");
            builder.append("\r\n");
        } else {
            builder.append("405 NOT ALLOWED");
            builder.append("\r\n");
        }

        addHeader(builder,"Date",date);
        addHeader(builder, "Server",server);
        addHeader(builder, "Connection",connection);
        addHeader(builder, "Content-Type", contentType);
        addHeader(builder, "Content-Length", contentLength.toString());

        builder.append("\r\n");

        final byte[] headersByte = builder.toString().getBytes();

        if("GET".equals(requestMethod)||("POST".equals(requestMethod))) {
            if(body!=null) {
                return concat(headersByte, body);
            } else {
                return headersByte;
            }
        } else {
            System.out.println(builder.toString());
            return builder.toString().getBytes();
        }
    }

    private byte[] concat(byte[] a, byte[] b) {
        final int aLen = a.length;
        final int bLen = b.length;
        final byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private void addHeader(StringBuilder builder, String headerName, String header) {
        if(header != null){
            builder.append(headerName);
            builder.append(": ");
            builder.append(header);
            builder.append("\r\n");
        }
    }
}
