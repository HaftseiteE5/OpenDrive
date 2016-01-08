package com.opendrive.android.request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.opendrive.android.common.Constant;
import com.opendrive.android.common.Utils;
import com.opendrive.android.datamodel.CreateFileResultData;
import com.opendrive.android.datamodel.FileData;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class Request {

    public static final String TAG = "Request";

    public String httpPost(String url, ArrayList<NameValuePair> postData) throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        httppost.setEntity(new UrlEncodedFormEntity(postData));
        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);

        if (response.getStatusLine().getStatusCode() != 200) {
            return Constant.ErrorMessage;
        }
        return new String(EntityUtils.toByteArray(response.getEntity()), "UTF8");
    }

    public Bitmap getFileIcon(String url, ArrayList<NameValuePair> postData, String fileID) throws IOException {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        httppost.setEntity(new UrlEncodedFormEntity(postData));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);

        if (response.getStatusLine().getStatusCode() != 200) {
            return null;
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inDither = true;
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        byte[] responseBody = EntityUtils.toByteArray(response.getEntity());
        Utils.saveIcon(fileID + ".jpg", responseBody);
        Bitmap iconBitmap = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length, opt);
        System.gc();
        return iconBitmap;
    }

    public String uploadImage(String url, String imageFullPath, CreateFileResultData createFileResultData, String destFileName, String accessDirId) throws IOException {

        String strBoundary = "---------------------------7da31c351a0420";
        String extraReturnLine = "\r\n";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(120000);
        connection.setUseCaches(true);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + strBoundary);

        connection.connect();

        strBoundary = "-----------------------------7da31c351a0420";

        BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"action\"" + extraReturnLine + extraReturnLine + "upload_file_chunk" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"session_id\"" + extraReturnLine + extraReturnLine + Constant.SessionID + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"is_pro\"" + extraReturnLine + extraReturnLine + "" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"share_user_id\"" + extraReturnLine + extraReturnLine + "" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"share_id\"" + extraReturnLine + extraReturnLine + "" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"access_dir_id\"" + extraReturnLine + extraReturnLine + "" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"file_id\"" + extraReturnLine + extraReturnLine + createFileResultData.getID() + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"temp_location\"" + extraReturnLine + extraReturnLine + createFileResultData.getTempLocation() + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"chunk_offset\"" + extraReturnLine + extraReturnLine + "0" + extraReturnLine).getBytes());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"chunk_size\"" + extraReturnLine + extraReturnLine + new File(imageFullPath).length() + extraReturnLine).getBytes());
        Log.i("ImageUpload", "File Length >>>> " + new File(imageFullPath).length());

        bos.write((strBoundary + extraReturnLine).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"file_data\"; filename=\"" + destFileName + "\"" + extraReturnLine + "Content-Type: image/jpeg" + extraReturnLine + extraReturnLine).getBytes());
        FileInputStream fis = new FileInputStream(imageFullPath);
        BufferedInputStream bis = new BufferedInputStream(fis);

        int len = 0;
        int totalLength = 0;
        byte[] buf = new byte[1024];

        while ((len = bis.read(buf)) != -1) {
            bos.write(buf, 0, len);
            totalLength += len;
        }

        Log.i("ImageUpload", "Total Length >>>> " + totalLength);

        bos.write((extraReturnLine + strBoundary + "--" + extraReturnLine).getBytes());
        bos.flush();
        bis.close();
        fis.close();
        bos.close();

        int serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();

        if (200 == serverResponseCode) {
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            String responseXml = "";
            while (null != (line = lnr.readLine())) {
                responseXml += line + "\n";
            }

            Log.i("Server", "Response received:\n" + responseXml);

            return responseXml;
        } else {
            Log.i("Server", "Response received:\n" + serverResponseMessage.toString());
            return Constant.ErrorMessage;
        }
    }

    public boolean downloadFile(String url, ArrayList<NameValuePair> postData, String destFileName, FileData fileData) throws IOException {

        File destFile = new File(destFileName);
        if (destFile.exists()){
            return true;
        }

        File dirs = new File(destFile.getParent());
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        if (!destFile.createNewFile()) {
            return false;
        }

        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(url);
        //httppost.setHeader("Accept-Encoding", "gzip");

        httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                //AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);
                if (!request.containsHeader("Accept-Encoding")) {
                    Log.d(TAG, "Download filem add header Accept-Encoding: gzip");
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });

        httppost.setEntity(new UrlEncodedFormEntity(postData));

        HttpResponse response = httpclient.execute(httppost);

        if (response.getStatusLine().getStatusCode() != 200) {
            return false;
        }

        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        Log.d(TAG, "Response for download file has contentEncoding header = " + response.getEntity().getContentEncoding());
        Log.d(TAG, "Response's all headers: " + Arrays.toString(response.getAllHeaders()));

        InputStream is = response.getEntity().getContent();

        if (contentEncoding != null){
            Log.d(TAG, "encoding: " + response.getFirstHeader("Content-Encoding"));
        }
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            Log.d(TAG, "response gzip");
            is = new GZIPInputStream(is);
        }

        Log.d(TAG, "read input stream");

        FileOutputStream fos = new FileOutputStream(destFile, true);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fos.flush();

        fos.close();
        is.close();

        destFile.setLastModified(Long.parseLong(fileData.getDateModified()) * 1000);

        return true;
    }

}
