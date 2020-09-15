package com.gft.mapfre.uploadfiles.documentum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;


public class RestOperations {

  public static final String DOCUMENTUM = "http://wportalinterno.pre.mapfre.net/D2-REST/repositories/dmprep1";
  public static final String AUTHORIZATION = basicAuth("app-dgtprootsgd", "9Tm83TnA");
  public static String OBJECT_ID = "";
  /*public static String URL_TO_DOWLOAD = "";
  public static final String DOCUMENTUM = "http://webservices.desa.mapfre.net/D2MESP-Rest/repositories/dmdesa2";
	public static final String AUTHORIZATION = basicAuth("app-dgtprootsgd", "7uKLrXflx");*/

  public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

    HttpClient client = HttpClient.newHttpClient();

    System.out.println("***** OBJECT CREATION *****");
    HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(DOCUMENTUM + "/object-creation"))
        .version(HttpClient.Version.HTTP_2)
        .POST(BodyPublishers.ofString("{\n"
            + "  \"properties\": {\n"
            + "    \"r_object_type\": \"mpesp_do_reclamaciones\",\n"
            + "    \"object_name\": \"Test Reclamaciones\",\n"
            + "    \"title\": \"TITULO\",\n"
            + "    \"mpesp_atr_subtipo\": \"Comu_Recl\",\n"
            + "    \"mpesp_atr_codigo_interno\": \"\",\n"
            + "    \"mpesp_atr_fecha_doc\": \"2020-07-22\",\n"
            + "    \"mpesp_atr_app_origen\": \"ODEON\",\n"
            + "    \"mpesp_atr_medio_origen\": \"MFP\",\n"
            + "    \"mpesp_atr_caso\": \"000000-2020\"\n"
            + "  }\n"
            + "}"))
        .headers("Authorization", AUTHORIZATION, "Content-Type", "application/vnd.emc.documentum+json")
        .build();

    HttpResponse<String> pageResponse = client.send(request, BodyHandlers.ofString());
    System.out.println("response status code: " + pageResponse.statusCode());
    //System.out.println("Page response headers: " + pageResponse.headers());
    OBJECT_ID = new Gson().fromJson(pageResponse.body(), JsonObject.class).getAsJsonObject("properties").get("r_object_id").getAsString();
    System.out.println("response r_object_id: " + OBJECT_ID);
    //addContent(client).thenCompose(unused -> readContent(client)).thenCompose(stringHttpResponse -> deleteObject(client)).join();
    addContent(client).join();
  }

  private static void addContentSync(HttpClient client) throws IOException, InterruptedException {
    HttpRequest request;
    BodyPublisher bodyPublisher = null;
    try {
      bodyPublisher = BodyPublishers
          .ofFile(Path.of("C:\\GFT\\Project\\Mapfre\\POCUploadFiles\\gatling-tests\\gatling\\user-files\\resources\\mapfre\\adjuntos\\dummy_150.data"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    request = HttpRequest.newBuilder()
        .uri(URI.create(DOCUMENTUM + "/objects/" + OBJECT_ID + "/contents"))
        .POST(bodyPublisher)
        .headers("Authorization", AUTHORIZATION, "Content-Type", "application/vnd.emc.documentum+json")
        .build();
    System.out.println(
        "***** START ADD CONTENT at " + SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(Calendar.getInstance().getTime())
            + "*****");
    HttpResponse<String> pageResponse = client.send(request, BodyHandlers.ofString());
    System.out.println(
        "***** END ADD CONTENT at " + SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(Calendar.getInstance().getTime())
            + "*****");
    System.out.println("response status code: " + pageResponse.statusCode());
    JsonObject properties = new Gson().fromJson(pageResponse.body(), JsonObject.class).getAsJsonObject("properties");
    System.out.println("response r_object_id: " + properties.get("r_object_id").getAsString());
    System.out.println("response parent_id: " + properties.get("parent_id").getAsString());

  }

  private static CompletableFuture<Void> addContent(HttpClient client) {
    HttpRequest request;
    BodyPublisher bodyPublisher = null;
    try {
      bodyPublisher = BodyPublishers
          .ofFile(Path.of("C:\\GFT\\Project\\Mapfre\\POCUploadFiles\\gatling-tests\\gatling\\user-files\\resources\\mapfre\\adjuntos\\dummy_150.data"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    request = HttpRequest.newBuilder()
        .uri(URI.create(DOCUMENTUM + "/objects/" + OBJECT_ID + "/contents"))
        .POST(bodyPublisher)
        .headers("Authorization", AUTHORIZATION, "Content-Type", "application/vnd.emc.documentum+json")
        .build();
    System.out.println(
        "***** START ADD CONTENT at " + SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(Calendar.getInstance().getTime())
            + "*****");
    return client.sendAsync(request, BodyHandlers.ofString())
        .thenAccept(pageResponse -> {
          System.out.println(
              "***** END ADD CONTENT at " + SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(Calendar.getInstance().getTime())
                  + "*****");
          System.out.println("response status code: " + pageResponse.statusCode());
          JsonObject properties = new Gson().fromJson(pageResponse.body(), JsonObject.class).getAsJsonObject("properties");
          System.out.println("response r_object_id: " + properties.get("r_object_id").getAsString());
          System.out.println("response parent_id: " + properties.get("parent_id").getAsString());
        });
  }

  private static CompletableFuture<HttpResponse<String>> readContent(HttpClient client) {

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(DOCUMENTUM + "/objects/" + OBJECT_ID + "/preview-urls"))
        .GET()
        .headers("Authorization", AUTHORIZATION, "Content-Type", "application/vnd.emc.documentum+json")
        .build();

    return client.sendAsync(request, BodyHandlers.ofString())
        .thenApply(pageResponse -> {
          System.out.println("***** READ CONTENT *****");
          System.out.println("response status code: " + pageResponse.statusCode());
          JsonObject properties = new Gson().fromJson(pageResponse.body(), JsonObject.class).getAsJsonObject("properties");
          System.out.println("response r_object_id: " + properties.get("r_object_id").getAsString());
          System.out.println("response parent_id: " + properties.get("parent_id").getAsString());
          System.out.println("response full_content_size: " + properties.get("full_content_size").getAsString());
          return pageResponse;
        });
  }

  private static CompletableFuture<HttpResponse<String>> deleteObject(HttpClient client) {
    HttpRequest request;
    request = HttpRequest.newBuilder()
        .uri(URI.create(DOCUMENTUM + "/objects/" + OBJECT_ID))
        .DELETE()
        .headers("Authorization", AUTHORIZATION, "Content-Type", "application/vnd.emc.documentum+json")
        .build();

    return client.sendAsync(request, BodyHandlers.ofString())
        .thenApply(pageResponse -> {
          System.out.println("***** DELETE OBJECT *****");
          System.out.println("response status code: " + pageResponse.statusCode());
          return pageResponse;
        });
  }

  private static String basicAuth(String username, String password) {
    return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
  }

}
