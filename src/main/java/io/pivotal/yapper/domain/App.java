package io.pivotal.yapper.domain;

import io.pivotal.yapper.model.ActuatorHealth;
import io.pivotal.yapper.model.Instance;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Table(name = "app")
@Entity
public class App implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss")
    private Date createdAt;
    private String appName;

    @Embedded
    private ActuatorHealth health;
    private int instanceCount = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "app_instances", joinColumns = @JoinColumn(name = "app_id"))
    private Set<Instance> instances = new HashSet<>();

}
