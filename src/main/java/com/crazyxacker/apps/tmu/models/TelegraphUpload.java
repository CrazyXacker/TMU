package com.crazyxacker.apps.tmu.models;

import com.crazyxacker.apps.tmu.utils.TelegraphConstants;
import lombok.Data;

@Data
public class TelegraphUpload {
    private String src;

    public String getSrcUrl() {
        return TelegraphConstants.BASE_URL + src;
    }
}
