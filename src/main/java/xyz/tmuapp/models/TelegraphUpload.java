package xyz.tmuapp.models;

import lombok.Data;
import xyz.tmuapp.utils.TelegraphConstants;

@Data
public class TelegraphUpload {
    private String src;

    public String getSrcUrl() {
        return TelegraphConstants.BASE_URL + src;
    }
}
