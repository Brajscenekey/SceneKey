package com.scenekey.aws_service;


import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mindiii on 1/3/18.
 */


public abstract class Aws_Web_Service  {

    Response response;

    public Aws_Web_Service updateKeyPoint(final int Keypoints, final String userID) {

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... params) {
                OkHttpClient.Builder b = new OkHttpClient.Builder();
                b.readTimeout(15, TimeUnit.SECONDS);
                b.writeTimeout(10, TimeUnit.SECONDS);
                b.connectTimeout(5, TimeUnit.SECONDS);
                OkHttpClient client = b.build();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\n\"method\":\"PUT\",\n\"action\":\"updateKeyPoints\",\n\"userid\":"+userID+",\n\"keyPoints\":"+Keypoints+"\n}");
                Request request = new Request.Builder()
                        .url("https://0kh929t4ng.execute-api.us-west-1.amazonaws.com/dev/users")

                        .put(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build();

                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(final Response response) {
                onResponseUpdate(response);
                super.onPostExecute(response);
            }
        }.execute();

        return this;
    }

    public Aws_Web_Service searchTag(final String tag){

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... params) {
                OkHttpClient.Builder b = new OkHttpClient.Builder();
                b.readTimeout(15, TimeUnit.SECONDS);
                b.writeTimeout(10, TimeUnit.SECONDS);
                b.connectTimeout(5, TimeUnit.SECONDS);
                OkHttpClient client = b.build();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\n  \"method\":\"GET\",\n  \"action\":\"search\",\n  \"term\": \""+tag+"\"\n}");
                Request request = new Request.Builder()
                        .url("https://0kh929t4ng.execute-api.us-west-1.amazonaws.com/dev/tags")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "21c6d18e-f07b-6f53-970c-da89933adf2b")
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(final Response response) {
                onResponseUpdate(response);
                super.onPostExecute(response);
            }
        }.execute();

        return this;
    }



    public Aws_Web_Service addTag(final String tag){

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... params) {

                OkHttpClient.Builder b = new OkHttpClient.Builder();
                b.readTimeout(15, TimeUnit.SECONDS);
                b.writeTimeout(10, TimeUnit.SECONDS);
                b.connectTimeout(5, TimeUnit.SECONDS);
                OkHttpClient client = b.build();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\n  \"method\": \"POST\",\n  \"action\": \"create\",\n  \"name\": \""+tag+"\"\n}");
                Request request = new Request.Builder()
                        .url("https://0kh929t4ng.execute-api.us-west-1.amazonaws.com/dev/tags")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "a4c9c330-86c6-8b9c-471d-291e82f4a385")
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return response;
            }

            @Override
            protected void onPostExecute(final Response response) {
                onResponseUpdate(response);
                //{"fieldCount": 0,      "affectedRows": 1,"insertId": 32853,    "serverStatus": 2,"warningCount": 0,         "message": "","protocol41": true,          "changedRows": 0        }
                super.onPostExecute(response);
            }
        }.execute();

        return this;
    }




    public Aws_Web_Service addTagToevent(final String tags ,final  String eventId){

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... params) {

                OkHttpClient.Builder b = new OkHttpClient.Builder();
                b.readTimeout(15, TimeUnit.SECONDS);
                b.writeTimeout(10, TimeUnit.SECONDS);
                b.connectTimeout(5, TimeUnit.SECONDS);
                OkHttpClient client = b.build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\n  \"method\": \"POST\",\n  \"action\": \"mapEventToTag\",\n  \"tagId\": "+tags+",\n  \"eventId\": "+eventId+"\n}");
                Request request = new Request.Builder()
                        .url("https://0kh929t4ng.execute-api.us-west-1.amazonaws.com/dev/tags")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "96860caa-1c56-f14a-2211-7ec936feb5ea")
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return response;
            }

            @Override
            protected void onPostExecute(final Response response) {
                onResponseUpdate(response);
                super.onPostExecute(response);
            }
        }.execute();

        return this;
    }


    //TODO handling null response at all screen of onResponseUpdate
    public   abstract Response onResponseUpdate(Response response);
}
