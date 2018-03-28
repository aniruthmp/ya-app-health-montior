package io.pivotal.yapper.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import javax.persistence.Embeddable;
import java.util.Map;

@Data
@NoArgsConstructor
@Embeddable
public class ActuatorHealth {
    private String status;

    @Transient
    private transient Map<String, Object> details;

    @JsonAnyGetter
    public Map<String, Object> getDetails() {
        return this.details;
    }

    @Override
    public String toString() {
        return getStatus() + " " + getDetails();
    }

}
