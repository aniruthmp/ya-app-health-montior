package io.pivotal.yapper.util;

public interface AppConstants {
    public String instanceId = "instanceId";
    public String up = "UP";
    public String down = "DOWN";
    public String unknown = "UNKNOWN";
    public String out_of_service = "OUT_OF_SERVICE";

    public String linked_hashmap = "java.util.LinkedHashMap";

    public String http = "http";
    public String https = "https";
    public String health_uri = "/health";

    public String slack_sent = "{\"message\": \"OK sent slack message\"}";
    public String slack_not_sent = "{\"message\": \"Error in sending Slack message\"}";
    public String slack_url_empty = "{\"message\": \"Slack message not sent. Slack URL is empty.\"}";
    public String slack_diabled = "{\"message\": \"Slack notification disabled.\"}";

    public String mail_sent = "{\"message\": \"OK sent mail\"}";
    public String mail_not_sent = "{\"message\": \"Error in sending mail\"}";
    public String mail_url_empty = "{\"message\": \"Email not sent. Slack URL is empty.\"}";
    public String mail_diabled = "{\"message\": \"Email notification disabled.\"}";
    public String mail_subject = "Yapper Auto-generate email. Do not reply";
}
