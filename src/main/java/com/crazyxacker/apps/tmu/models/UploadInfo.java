package com.crazyxacker.apps.tmu.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class UploadInfo {
    private String title;
    private String link;
    private long uploadedAt;

    public String getReadableUploadedAt() {
        return new Timestamp(uploadedAt).toString();
    }
}
