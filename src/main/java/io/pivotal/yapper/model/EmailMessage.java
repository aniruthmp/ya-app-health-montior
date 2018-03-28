package io.pivotal.yapper.model;

import io.pivotal.yapper.util.AppConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {
    private String appName;
    private String health = AppConstants.down;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
    private Date reportedAt;
    private Map<String, Object> details;

    public String toJson() {
        String json = toString();
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            json = gson.toJson(this);
        } catch (Exception ex){}
        return json;
    }
}
