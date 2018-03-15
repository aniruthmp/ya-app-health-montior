package io.pivotal.appmonitor.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.pivotal.appmonitor.model.Instance;
import io.pivotal.appmonitor.util.AppConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "app")
public class App implements Serializable {

    @Id
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
    private Date createdAt;
    private String appName;
    private String health = AppConstants.down;
    private int instanceCount = 0;
    private List<Instance> instanceList = new ArrayList<>();
}
