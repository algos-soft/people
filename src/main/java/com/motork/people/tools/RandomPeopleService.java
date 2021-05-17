package com.motork.people.tools;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomPeopleService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private OkHttpClient okHttpClient;

    private JsonAdapter<UserResponse> jsonAdapter;

    Random random = new Random();


    @PostConstruct
    private void init() {
        okHttpClient = new OkHttpClient();
        Moshi moshi = new Moshi.Builder().build();
        jsonAdapter = moshi.adapter(UserResponse.class);
    }


    public String generatePeople(int number) throws Exception {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://randomuser.me/api/").newBuilder();
        String url = urlBuilder.build().toString();

        String s1;
        String s2;
        String s3;
        String s4;
        String s5;
        String s6;

        String filename=number + "_persons.csv";
        Path path = Paths.get(SystemUtils.getUserHome().toString(), filename);
        FileWriter out = new FileWriter(path.toFile());
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);

        for (int i = 0; i < number; i++) {

            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String respString = response.body().string();
                UserResponse apiResponse = jsonAdapter.fromJson(respString);
                if (apiResponse.results.size() > 0) {
                    Result result = apiResponse.results.get(0);

                    s1=result.email;
                    s2=result.name.last;
                    s3=result.name.first;
                    s4=randomCode();
                    s5=result.location.city + ", " + result.location.state;
                    s6=randomDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    printer.printRecord(s1, s2, s3, s4, s5, s6);

                    log.info(i + 1 + " - "+s1+" ("+s2+" "+s3+")");
                }
            }

        }

        out.close();

        return filename;

    }


    static class UserResponse {
        public List<Result> results;
    }

    static class Result {
        public Name name;
        public String email;
        public Location location;
    }

    static class Name {
        public String first;
        public String last;
    }

    static class Location {
        public String city;
        public String state;
    }



    private String randomCode() {
        return random.ints(48, 122 + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(16)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString().toUpperCase();

    }

    private static LocalDate randomDate() {
        LocalDate startInclusive = LocalDate.now().minusYears(1);
        LocalDate endExclusive = LocalDate.now();
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = ThreadLocalRandom
                .current()
                .nextLong(startEpochDay, endEpochDay);

        return LocalDate.ofEpochDay(randomDay);
    }



}
