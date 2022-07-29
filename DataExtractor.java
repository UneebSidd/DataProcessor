import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtractor {


    public static List<List<Object>> getData(String spreadSheetId, String sheetName, String rangeDataToRead) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        Sheets sheet = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), requestInitializer).setApplicationName("Data set").build();


        List<List<Object>> data = sheet.spreadsheets().values()
                .get(spreadSheetId, sheetName + "!" + rangeDataToRead)
                .execute().getValues();

        return data;

    }



    public static void dataParser(List<List<Object>> dataList)
    {
        for(int i=0; i<dataList.size(); i++)
        {
            parseData(dataList.get(i));
        }

    }


    private static void parseData(List<Object> list) {
        long value;

        // to check if there is any occurrences of at least one digit.
        Pattern pattern = Pattern.compile("[0-9]");
        //to look for any occurrence of any character or characters in a string
        Pattern alphaPattern = Pattern.compile("[abcdefghijklmnopqrstuvwxyz]", Pattern.CASE_INSENSITIVE);

        if(list.size()<5)
        {
            list.add("");
        }

        for (int i = 0; i < list.size(); i++) {
                //if we have a match in a given string store it in a Matcher Object.
            Matcher matcher = pattern.matcher(list.get(i).toString());
            Matcher charMatcher = alphaPattern.matcher(list.get(i).toString());


           try {
               //column 3(phone number) or column 5(zipcode)
               if (i == 2 || i == 4) {
                            //if column 3 or 5 has no value or if they are not numeric
                   if (list.get(i).toString().isEmpty() || charMatcher.find()) {
                        //set value to zero
                       list.set(i, 0);
                   }

                   else {

                       value = Long.parseLong(list.get(i).toString());
                       list.set(i, value);
                   }

               } else if (i == 0 || i == 1 || i == 3) {

                   //if column 1,2,or 4 contains an empty string or numbers
                   if (list.get(i).toString().isEmpty() || matcher.find()) {
                       list.set(i, "null");
                   }

               }

           } catch (NumberFormatException e)
           {
               list.set(i,0);
           }


        }


    }

    public static String convertToJson(List<Object> data) throws IOException {
        String firstName="";
        String lastName="";
        long phone = 0;
        String state = "";
        long zip = 0;

                 firstName = data.get(0).toString();
                 lastName = data.get(1).toString();
                 phone = Long.parseLong(data.get(2).toString());
                 state = data.get(3).toString();
                 zip = Integer.parseInt(data.get(4).toString());


            CustomerInfo customerInfo = new CustomerInfo(firstName,lastName,phone,state,zip);


                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(customerInfo);


        return json;
    }


    public static void validateSchema(String jsonString) throws FileNotFoundException {

        File schemaFile = new File("/Users/uneebsiddiqui/Desktop/OAS/Java/src/main/resources/schemaFile.json");
        JSONTokener schemaData = new JSONTokener(new FileInputStream(schemaFile));
        JSONObject jsonSchema = new JSONObject(schemaData);

        try {

            FileWriter fileWriter = new FileWriter("/Users/uneebsiddiqui/Desktop/OAS/Java/src/main/resources/file1.json");
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("file not found");
        }

        File jsonData = new File("/Users/uneebsiddiqui/Desktop/OAS/Java/src/main/resources/file1.json");
        JSONTokener jsonDataFile = new JSONTokener(new FileInputStream(jsonData));
        JSONObject jsonObject = new JSONObject(jsonDataFile);
//
        Schema schemaValid = SchemaLoader.load(jsonSchema);
        schemaValid.validate(jsonObject);

        System.out.println("done");

    }






}
