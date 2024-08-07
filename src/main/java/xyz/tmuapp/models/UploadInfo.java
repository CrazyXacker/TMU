package xyz.tmuapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.tmuapp.utils.ArrayUtils;
import xyz.tmuapp.utils.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class UploadInfo {
    private String title;
    private List<String> titles;
    private List<String> links;
    private List<String> tags;
    private String description;
    private long uploadedAt;

    public String getReadableUploadedAt() {
        return new Timestamp(uploadedAt).toString();
    }

    public String getLinksCommaSeparated() {
        return StringUtils.join(", ", getLinks());
    }

    public String getLinksNewLineSeparated() {
        return StringUtils.join("\n", getLinks());
    }

    public String getFirstLink() {
        return Optional.ofNullable(links)
                .filter(ArrayUtils::isNotEmpty)
                .map(list -> list.get(0))
                .orElse("");
    }
}
